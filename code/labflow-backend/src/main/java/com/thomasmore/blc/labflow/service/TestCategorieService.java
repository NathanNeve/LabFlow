package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.entity.hematology.Testcategorie;
import com.thomasmore.blc.labflow.repository.hematology.TestCategorieRepository;
import com.thomasmore.blc.labflow.repository.hematology.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional("hematologyTransactionManager")
public class TestCategorieService {
    @Autowired
    private TestCategorieRepository testCategorieRepository;
    @Autowired
    private TestRepository testRepository;

    // Create
    public void createTestcategorie(Testcategorie testcategorie) {
        testCategorieRepository.save(testcategorie);
    }

    // Read
    // all
    public List<Testcategorie> allTestcategorie() {
        return testCategorieRepository.findAll();
    }
    // search
    public List<Testcategorie> searchTestCategorie(String partName) {
        return testCategorieRepository.findAllByNaamIsStartingWith(partName);
    }

    // Update
    public ResponseEntity<Testcategorie> updateTestCategorie(Long id, Testcategorie updateTestCategorie) {
        Testcategorie testcategorie = testCategorieRepository.findById(id);
        if (testcategorie != null) {
            testcategorie.setNaam(updateTestCategorie.getNaam());
            testcategorie.setKleur(updateTestCategorie.getKleur());
            testcategorie.setKleurnaam(updateTestCategorie.getKleurnaam());
            testCategorieRepository.save(testcategorie);
            return new ResponseEntity<>(testcategorie, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete
    public ResponseEntity<Integer> deleteTestCategorie(Long id) {
        Testcategorie deleteTestcategorie = testCategorieRepository.findById(id);
        if (testRepository.existsByTestcategorieId(id)) {
            throw new IllegalStateException("Kan categorie niet verwijderen want deze is gelinked aan één of meerdere tests");
        }
        if (deleteTestcategorie != null) {
            testCategorieRepository.delete(deleteTestcategorie);
            return new ResponseEntity<>(testCategorieRepository.findAll().size(), HttpStatus.OK);
        }
        return new ResponseEntity<>(testCategorieRepository.findAll().size(), HttpStatus.NOT_FOUND);
    }
}
