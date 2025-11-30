package com.example.demo.controller;

import com.example.demo.model.CreditCard;
import com.example.demo.model.Redemption;
import com.example.demo.service.CreditCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/creditcards")
public class CreditCardController {

    private final CreditCardService creditCardService;

    public CreditCardController(CreditCardService creditCardService) {
        this.creditCardService = creditCardService;
    }

    @GetMapping
    public List<CreditCard> getAllCreditCards() {
        return creditCardService.getAllCreditCards();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCard> getCreditCardById(@PathVariable String id) {
        return creditCardService.getCreditCardById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public CreditCard addCreditCard(@RequestBody CreditCard creditCard) {
        return creditCardService.addCreditCard(creditCard);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditCard> updateCreditCard(@PathVariable String id, @RequestBody CreditCard creditCard) {
        return creditCardService.updateCreditCard(id, creditCard)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreditCard(@PathVariable String id) {
        if (creditCardService.deleteCreditCard(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{cardId}/redemptions")
    public ResponseEntity<Redemption> addRedemption(@PathVariable String cardId, @RequestBody Redemption redemption) {
        return creditCardService.addRedemption(cardId, redemption)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{cardId}/redemptions/{redemptionId}")
    public ResponseEntity<Void> deleteRedemption(@PathVariable String cardId, @PathVariable String redemptionId) {
        if (creditCardService.deleteRedemption(cardId, redemptionId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{cardId}/redemptions/{redemptionId}/checked")
    public ResponseEntity<Redemption> updateRedemptionStatus(@PathVariable String cardId, @PathVariable String redemptionId, @RequestParam boolean checked) {
        return creditCardService.updateRedemptionStatus(cardId, redemptionId, checked)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
