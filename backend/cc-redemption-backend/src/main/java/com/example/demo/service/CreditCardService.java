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
        return creditCards;
    }

    public Optional<CreditCard> getCreditCardById(String id) {
        return creditCards.stream().filter(card -> card.getId().equals(id)).findFirst();
    }

    public CreditCard addCreditCard(CreditCard creditCard) {
        creditCard.setId(UUID.randomUUID().toString());
        if (creditCard.getRedemptions() != null) {
            creditCard.getRedemptions().forEach(redemption -> redemption.setId(UUID.randomUUID().toString()));
        }
        creditCards.add(creditCard);
        dataManager.saveData(creditCards);
        return creditCard;
    }

    public Optional<CreditCard> updateCreditCard(String id, CreditCard updatedCard) {
        return getCreditCardById(id).map(card -> {
            card.setName(updatedCard.getName());
            card.setDescription(updatedCard.getDescription());
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

    public Optional<Redemption> updateRedemptionStatus(String cardId, String redemptionId, boolean checked) {
        return getCreditCardById(cardId)
                .flatMap(card -> card.getRedemptions().stream()
                        .filter(red -> red.getId().equals(redemptionId))
                        .findFirst()
                        .map(red -> {
                            red.setChecked(checked);
                            if (checked) {
                                red.setLastCheckedDate(LocalDate.now());
                            } else {
                                red.setLastCheckedDate(null); // Clear last checked date if unchecked
                            }
                            dataManager.saveData(creditCards);
                            return red;
                        }));
    }

    public void resetRedemptions() {
        LocalDate now = LocalDate.now();
        boolean changed = false;
        for (CreditCard card : creditCards) {
            for (Redemption redemption : card.getRedemptions()) {
                boolean shouldReset = false;
                if (redemption.isChecked() && redemption.getLastCheckedDate() != null) {
                    LocalDate lastChecked = redemption.getLastCheckedDate();
                    switch (redemption.getFrequency()) {
                        case MONTHLY:
                            // Reset if the current month is after the month last checked, or a new year has started
                            shouldReset = now.getYear() > lastChecked.getYear() || now.getMonthValue() > lastChecked.getMonthValue();
                            break;
                        case QUARTERLY:
                            // Reset if last checked in a previous quarter
                            int currentQuarter = (now.getMonthValue() - 1) / 3;
                            int lastCheckedQuarter = (lastChecked.getMonthValue() - 1) / 3;
                            shouldReset = now.getYear() > lastChecked.getYear() || currentQuarter > lastCheckedQuarter;
                            break;
                        case BIANNUAL:
                            // Reset if the current half-year is after the half-year last checked, or a new year has started
                            int currentHalfYear = now.getMonthValue() <= 6 ? 0 : 1;
                            int lastCheckedHalfYear = lastChecked.getMonthValue() <= 6 ? 0 : 1;
                            shouldReset = now.getYear() > lastChecked.getYear() || currentHalfYear > lastCheckedHalfYear;
                            break;
                        case YEARLY:
                            // Reset if last checked in a previous year
                            shouldReset = now.getYear() > lastChecked.getYear();
                            break;
                    }
                }

                if (shouldReset && redemption.isChecked()) {
                    redemption.setChecked(false);
                    redemption.setLastCheckedDate(null); // Clear last checked date after reset
                    changed = true;
                }
            }
        }
        if (changed) {
            dataManager.saveData(creditCards);
        }
    }
}
