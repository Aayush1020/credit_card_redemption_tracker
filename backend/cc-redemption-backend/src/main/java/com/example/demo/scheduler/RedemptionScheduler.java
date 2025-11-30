package com.example.demo.scheduler;

import com.example.demo.service.CreditCardService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedemptionScheduler {

    private final CreditCardService creditCardService;

    public RedemptionScheduler(CreditCardService creditCardService) {
        this.creditCardService = creditCardService;
    }

    @Scheduled(cron = "0 0 0 * * *") // Run every day at midnight
    public void resetRedemptionsDaily() {
        creditCardService.resetRedemptions();
        System.out.println("Redemptions reset nightly.");
    }
}
