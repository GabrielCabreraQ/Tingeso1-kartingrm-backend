package com.kartingrm.Controllers;

import com.kartingrm.Entities.ClientEntity;
import com.kartingrm.Entities.SpecialDaysEntity;
import com.kartingrm.Services.SpecialDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialdays")
@CrossOrigin("*")
public class SpecialDayController {

    @Autowired
    private SpecialDayService specialDayService;

    @GetMapping("/")
    public ResponseEntity<List<SpecialDaysEntity>> getAllSpecialDays() {
        List<SpecialDaysEntity> specialDays = specialDayService.getSpecialDays();
        return ResponseEntity.ok(specialDays);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialDaysEntity> getSpecialDayById(@PathVariable Long id) {
        SpecialDaysEntity specialDay = specialDayService.getSpecialDayById(id);
        return ResponseEntity.ok(specialDay);
    }

    @PostMapping("/")
    public ResponseEntity<SpecialDaysEntity> saveSpecialDay(@RequestBody SpecialDaysEntity specialDay) {
        SpecialDaysEntity savedSpecialDay = specialDayService.saveSpecialDay(specialDay);
        return ResponseEntity.ok(savedSpecialDay);
    }

    @PutMapping("/")
    public ResponseEntity<SpecialDaysEntity> updateSpecialDay(@RequestBody SpecialDaysEntity specialDay) {
        SpecialDaysEntity updatedSpecialDay = specialDayService.updateSpecialDay(specialDay);
        return ResponseEntity.ok(updatedSpecialDay);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecialDay(@PathVariable Long id) throws Exception {
        specialDayService.deleteSpecialDay(id);
        return ResponseEntity.noContent().build();
    }

}
