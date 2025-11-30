package com.example.demo.util;

import com.example.demo.model.CreditCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JsonDataManager {

    @Value("${app.data.filepath:creditcards.json}")
    private String dataFilePath;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new JavaTimeModule()); // Register JavaTimeModule
    private List<CreditCard> creditCards = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadData();
    }

    public List<CreditCard> loadData() {
        File file = new File(dataFilePath);
        if (file.exists() && file.length() > 0) {
            try {
                creditCards = new ArrayList<>(Arrays.asList(objectMapper.readValue(file, CreditCard[].class)));
            } catch (IOException e) {
                e.printStackTrace();
                creditCards = new ArrayList<>();
            }
        } else {
            creditCards = new ArrayList<>();
        }
        return creditCards;
    }

    public void saveData(List<CreditCard> data) {
        try {
            objectMapper.writeValue(new File(dataFilePath), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<CreditCard> getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(List<CreditCard> creditCards) {
        this.creditCards = creditCards;
    }
}
