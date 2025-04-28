package com.kartingrm.Controllers;


import com.kartingrm.Entities.PricingEntity;
import com.kartingrm.Services.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@CrossOrigin("*")
public class PricingController {
    @Autowired
    private PricingService pricingService;

    @GetMapping("/")
    public ResponseEntity<List<PricingEntity>> getAllPricing() {
        return ResponseEntity.ok(pricingService.getAllPricing());
    }

    @GetMapping("/last")
    public ResponseEntity<PricingEntity> getLastPricing() {
        return ResponseEntity.ok(pricingService.getLastPricing());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PricingEntity> getPricingById(@PathVariable Long id) {
        return ResponseEntity.ok(pricingService.getPricingById(id));
    }

    @PostMapping("/")
    public ResponseEntity<PricingEntity> savePricing(@RequestBody PricingEntity pricing) {
        return ResponseEntity.ok(pricingService.savePricing(pricing));
    }

    @PutMapping("/")
    public ResponseEntity<PricingEntity> updatePricing(@RequestBody PricingEntity pricing) {
        return ResponseEntity.ok(pricingService.updatePricing(pricing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePricing(@PathVariable Long id) {
        pricingService.deletePricing(id);
        return ResponseEntity.noContent().build();
    }
}