package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard {
    private String id;
    private String name;
    private String description;
    private List<Redemption> redemptions;
    private Map<Integer, Double> yearlyReimbursementSummaries = new java.util.HashMap<>();
    private transient double currentYearTotal;
}
