package com.example.demo.service;

import com.example.demo.model.CreditCard;
import com.example.demo.model.Redemption;
import com.example.demo.util.JsonDataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

@Service
public class CreditCardService {
    private final JsonDataManager dataManager;

    public CreditCardService(JsonDataManager dataManager) {
        this.dataManager = dataManager;
        this.creditCards = dataManager.loadData();
        resetRedemptions(); // Call reset immediately after loading data
    }

    private List<CreditCard> creditCards;

    public List<CreditCard> getAllCreditCards() {
        int currentCalendarYear = LocalDate.now().getYear();
        for (CreditCard card : creditCards) {
            double cardTotalThisYear = 0.0;
            if (card.getRedemptions() != null) {
                for (Redemption redemption : card.getRedemptions()) {
                    if (!redemption.getCompletedPeriodsThisYear().isEmpty()) {
                        // The set should only contain entries for the current year due to resetRedemptions logic
                        cardTotalThisYear += redemption.getAmount() * redemption.getCompletedPeriodsThisYear().size();
                    }
                }
            }
            card.setCurrentYearTotal(cardTotalThisYear);
        }
        return creditCards;
    }

    public Optional<CreditCard> getCreditCardById(String id) {
        return creditCards.stream().filter(card -> card.getId().equals(id)).findFirst();
    }

    public CreditCard addCreditCard(CreditCard creditCard) {
        creditCard.setId(UUID.randomUUID().toString());
        if (creditCard.getRedemptions() != null) {
            creditCard.getRedemptions().forEach(redemption -> {
                redemption.setId(UUID.randomUUID().toString());
                redemption.setCompletedPeriodsThisYear(new HashSet<>()); // Initialize the new set
            });
        }
        creditCards.add(creditCard);
        dataManager.saveData(creditCards);
        return creditCard;
    }

    public Optional<CreditCard> updateCreditCard(String id, CreditCard updatedCard) {
        return getCreditCardById(id).map(card -> {
            card.setName(updatedCard.getName());
            card.setDescription(updatedCard.getDescription());
            if (updatedCard.getRedemptions() != null) {
                updatedCard.getRedemptions().forEach(redemption -> {
                    if (redemption.getCompletedPeriodsThisYear() == null) {
                        redemption.setCompletedPeriodsThisYear(new HashSet<>()); // Initialize for new redemptions in update
                    }
                });
            }
            card.setRedemptions(updatedCard.getRedemptions());
            dataManager.saveData(creditCards);
            return card;
        });
    }

    public boolean deleteCreditCard(String id) {
        boolean removed = creditCards.removeIf(card -> card.getId().equals(id));
        if (removed) {
            dataManager.saveData(creditCards);
        }
        return removed;
    }

    public Optional<Redemption> addRedemption(String cardId, Redemption redemption) {
        return getCreditCardById(cardId).map(card -> {
            redemption.setId(UUID.randomUUID().toString());
            redemption.setCompletedPeriodsThisYear(new HashSet<>()); // Initialize for new redemption
            card.getRedemptions().add(redemption);
            dataManager.saveData(creditCards);
            return redemption;
        });
    }

    public boolean deleteRedemption(String cardId, String redemptionId) {
        Optional<CreditCard> cardOptional = getCreditCardById(cardId);
        if (cardOptional.isPresent()) {
            boolean removed = cardOptional.get().getRedemptions().removeIf(red -> red.getId().equals(redemptionId));
            if (removed) {
                dataManager.saveData(creditCards);
            }
            return removed;
        }
        return false;
    }

    /**
     * Generates a unique identifier for a redemption within a specific period (month, quarter, half-year, year).
     * This is used to track completion and prevent double-counting within the same period.
     * @param redemption The redemption item.
     * @param date The date for which to generate the period identifier.
     * @return A string identifier (e.g., "2026-M10", "2026-Q4", "2026-H2", "2026-Y").
     */
    private String getPeriodIdentifier(Redemption redemption, LocalDate date) {
        int year = date.getYear();
        return switch (redemption.getFrequency()) {
            case MONTHLY -> String.format("%d-M%02d", year, date.getMonthValue());
            case QUARTERLY -> String.format("%d-Q%d", year, (date.getMonthValue() - 1) / 3 + 1);
            case BIANNUAL -> String.format("%d-H%d", year, (date.getMonthValue() <= 6 ? 1 : 2));
            case YEARLY -> String.format("%d-Y", year);
        };
    }

    public Optional<Redemption> updateRedemptionStatus(String cardId, String redemptionId, boolean checked) {
        return getCreditCardById(cardId)
                .flatMap(card -> card.getRedemptions().stream()
                        .filter(red -> red.getId().equals(redemptionId))
                        .findFirst()
                        .map(red -> {
                            red.setChecked(checked);
                            if (checked) {
                                red.setLastCheckedDate(LocalDate.now());
                                String periodId = getPeriodIdentifier(red, LocalDate.now());
                                red.getCompletedPeriodsThisYear().add(periodId);
                            } else {
                                red.setLastCheckedDate(null); // Clear last checked date if unchecked
                                String periodId = getPeriodIdentifier(red, LocalDate.now());
                                red.getCompletedPeriodsThisYear().remove(periodId);
                            }
                            dataManager.saveData(creditCards);
                            return red;
                        }));
    }

    public void resetRedemptions() {
        LocalDate now = LocalDate.now();
        boolean dataChanged = false;
        int currentCalendarYear = now.getYear();

        // --- Step 1: Handle Year Rollover for all Cards ---
        // Determine the year that 'completedPeriodsThisYear' is currently tracking.
        // We assume all redemptions in `completedPeriodsThisYear` are for the same year.
        int lastTrackedYearInPeriods = -1;
        for (CreditCard card : creditCards) {
            for (Redemption redemption : card.getRedemptions()) {
                if (!redemption.getCompletedPeriodsThisYear().isEmpty()) {
                    String anyPeriodId = redemption.getCompletedPeriodsThisYear().iterator().next();
                    lastTrackedYearInPeriods = Integer.parseInt(anyPeriodId.substring(0, 4));
                    break; 
                }
            }
            if (lastTrackedYearInPeriods != -1) {
                break;
            }
        }

        // If the current calendar year is greater than the year we were tracking periods for
        if (lastTrackedYearInPeriods != -1 && currentCalendarYear > lastTrackedYearInPeriods) {
            // It's a new year - time to save summaries and clear for new tracking
            for (CreditCard card : creditCards) {
                double previousYearTotalForCard = 0.0;
                for (Redemption redemption : card.getRedemptions()) {
                    // Sum amounts for all completed periods from the *previous* year
                    for (String periodId : redemption.getCompletedPeriodsThisYear()) {
                        if (Integer.parseInt(periodId.substring(0, 4)) == lastTrackedYearInPeriods) {
                             previousYearTotalForCard += redemption.getAmount();
                        }
                    }
                }
                // Save this card's total for the previous year
                card.getYearlyReimbursementSummaries().put(lastTrackedYearInPeriods, previousYearTotalForCard);
                dataChanged = true;

                // Clear `completedPeriodsThisYear` for all redemptions on this card for the new year
                for (Redemption redemption : card.getRedemptions()) {
                    if (!redemption.getCompletedPeriodsThisYear().isEmpty()) {
                        redemption.getCompletedPeriodsThisYear().clear();
                        // Also clear lastCheckedDate if not null, as yearly periods are done
                        redemption.setLastCheckedDate(null);
                        dataChanged = true;
                    }
                }
            }
        }

        // --- Step 2: Handle Regular Period Resets (Monthly, Quarterly, Biannual, Yearly Checkboxes) ---
        // This is for unchecking redemptions due to period rollovers within the *current* active tracking year.
        // It's separate from saving the annual summary.
        for (CreditCard card : creditCards) {
            for (Redemption redemption : card.getRedemptions()) {
                boolean shouldResetCheckbox = false;

                if (redemption.isChecked() && redemption.getLastCheckedDate() != null) {
                    LocalDate lastChecked = redemption.getLastCheckedDate();

                    // Only apply intra-year reset logic if lastCheckedDate is from the *current tracking year*.
                    // If it's from a previous year, the year rollover logic (Step 1) should have handled it.
                    if (lastChecked.getYear() == currentCalendarYear) {
                        switch (redemption.getFrequency()) {
                            case MONTHLY:
                                shouldResetCheckbox = now.getMonthValue() > lastChecked.getMonthValue();
                                break;
                            case QUARTERLY:
                                int currentQuarter = (now.getMonthValue() - 1) / 3;
                                int lastCheckedQuarter = (lastChecked.getMonthValue() - 1) / 3;
                                shouldResetCheckbox = currentQuarter > lastCheckedQuarter;
                                break;
                            case BIANNUAL:
                                int currentHalfYear = now.getMonthValue() <= 6 ? 0 : 1;
                                int lastCheckedHalfYear = lastChecked.getMonthValue() <= 6 ? 0 : 1;
                                shouldResetCheckbox = currentHalfYear > lastCheckedHalfYear;
                                break;
                            case YEARLY:
                                // Yearly redemptions are ONLY reset by the year rollover logic (Step 1).
                                // No intra-year reset.
                                shouldResetCheckbox = false;
                                break;
                        }
                    } else {
                        // If lastCheckedDate is from a previous year, and it wasn't cleared by year rollover yet,
                        // ensure it's unchecked and its completedPeriods are cleared now.
                        shouldResetCheckbox = true; // Force uncheck
                        if (!redemption.getCompletedPeriodsThisYear().isEmpty()) {
                            redemption.getCompletedPeriodsThisYear().clear();
                            dataChanged = true;
                        }
                    }
                }

                if (shouldResetCheckbox && redemption.isChecked()) {
                    redemption.setChecked(false);
                    redemption.setLastCheckedDate(null); // Clear last checked date after reset
                    dataChanged = true;
                }
            }
        }

        if (dataChanged) {
            dataManager.saveData(creditCards);
        }
    }

    /**
     * Calculates the total reimbursement amount for the current calendar year.
     * @return The total reimbursement amount for the current year.
     */
    public double getCurrentYearReimbursementTotal() {
        double total = 0.0;
        int currentCalendarYear = LocalDate.now().getYear();

        for (CreditCard card : creditCards) {
            if (card.getRedemptions() != null) {
                for (Redemption redemption : card.getRedemptions()) {
                    // Sum the amount for each period recorded in the completedPeriodsThisYear set.
                    // The set itself will only contain entries for the current year due to the resetRedemptions logic.
                    total += redemption.getAmount() * redemption.getCompletedPeriodsThisYear().size();
                }
            }
        }
        return total;
    }
}
