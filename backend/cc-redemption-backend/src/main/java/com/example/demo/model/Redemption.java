package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Redemption {
    private String id;
    private String name;
    private double amount;
    private Frequency frequency;
    private boolean checked;
    private java.time.LocalDate lastCheckedDate;
    private java.util.Set<String> completedPeriodsThisYear = new java.util.HashSet<>();

    public enum Frequency {
        MONTHLY,
        QUARTERLY,
        BIANNUAL,
        YEARLY
    }
}
