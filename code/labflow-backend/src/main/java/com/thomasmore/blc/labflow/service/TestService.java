package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.entity.hematology.Referentiewaarde;
import com.thomasmore.blc.labflow.entity.hematology.Test;
import com.thomasmore.blc.labflow.repository.hematology.ReferentiewaardeRepository;
import com.thomasmore.blc.labflow.repository.hematology.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional("hematologyTransactionManager")
public class TestService {
    @Autowired
    private TestRepository testRepository;
    @Autowired
    private ReferentiewaardeRepository referentiewaardeRepository;


    // create
    public void create(Test test) {
        if (testRepository.findByTestCode(test.getTestCode()) == null) {
            testRepository.save(test);
        } else {
            throw new UniqueConstraintViolationException("Test code already exists");
        }
    }

    // read
    public List<Test> read() {
        return testRepository.findAllSortedByTestCode();
    }

    // update
    public ResponseEntity<Test> update(Long id, Test test) {
        Test updateTest = testRepository.findById(id);
        if (updateTest != null) {

            updateTest.setNaam(test.getNaam());
            updateTest.setTestCode(test.getTestCode());
            updateTest.setEenheid(test.getEenheid());
            updateTest.setTestcategorie(test.getTestcategorie());

            // setten van gelinkte test op null/ontkoppelen (anders krijgen we duplicate referentiewaarden)
            if (updateTest.getReferentiewaardes() != null) {
                Set<Referentiewaarde> referentieWaarden = updateTest.getReferentiewaardes();
                for (Referentiewaarde rw : referentieWaarden) {
                    Referentiewaarde referentieWaarde = referentiewaardeRepository.findById(rw.getId());
                    referentieWaarde.setTest(null);
                }
            }

            // setten van referentiewaarden gelinked aan test
            Set<Referentiewaarde> updatedReferentiewaardes = test.getReferentiewaardes();
            if (updatedReferentiewaardes != null) {
                for (Referentiewaarde rw : updatedReferentiewaardes) {
                    rw.setTest(updateTest);
                }
            }

            updateTest.setReferentiewaardes(updatedReferentiewaardes);

            testRepository.save(updateTest);
            return new ResponseEntity<>(updateTest, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    // delete
    public ResponseEntity<Integer> delete(Long id) {
        Test deleteTest = testRepository.findById(id);
        if (deleteTest != null) {
            testRepository.delete(deleteTest);
            return new ResponseEntity<>(testRepository.findAll().size(), HttpStatus.OK);
        }
        return new ResponseEntity<>(testRepository.findAll().size(), HttpStatus.NOT_FOUND);
    }
}