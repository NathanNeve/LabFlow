package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.entity.hematology.Testcategorie;
import com.thomasmore.blc.labflow.service.TestCategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/")
public class TestCategorieController {
    @Autowired
    private TestCategorieService testCategorieService;

    // Test categorie aanmaken (C)
    @PostMapping("/createtestcategorie")
    public void createTestCategorie(@RequestBody Testcategorie testcategorie) {
        try {
            testCategorieService.createTestcategorie(testcategorie);
        } catch (Exception e) {
            throw new UniqueConstraintViolationException(e.getMessage());
        }
    }

    // Test Categorie bekijken (R)
    @GetMapping("/testcategorieen")
    public List<Testcategorie> readTestCategorieen() {
        return testCategorieService.allTestcategorie();
    }

    // Test categorie aanpassen (U)
    @PutMapping("/testCategorieen/{id}")
    public ResponseEntity<Testcategorie> updateTestCategorie(@PathVariable Long id, @RequestBody Testcategorie updateTestCategorie) {
        return testCategorieService.updateTestCategorie(id, updateTestCategorie);
    }

    // Test categorie verwijderen (D)
    @DeleteMapping("/testcategorie/{id}")
    public ResponseEntity<Integer> deleteTestCategorie(@PathVariable Long id) {
        return testCategorieService.deleteTestCategorie(id);
    }

    // Test categorie zoek functie
    @GetMapping("/testCategorieen")
    public List<Testcategorie> searchTestCategorieen(@RequestParam String partName) {
        return testCategorieService.searchTestCategorie(partName);
    }
}
