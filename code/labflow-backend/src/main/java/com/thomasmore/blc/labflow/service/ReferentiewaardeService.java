package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.entity.hematology.Referentiewaarde;
import com.thomasmore.blc.labflow.repository.hematology.ReferentiewaardeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional("hematologyTransactionManager")
public class ReferentiewaardeService {
    @Autowired
    private ReferentiewaardeRepository referentiewaardeRepository;

    // create
    public void create(Referentiewaarde referentiewaarde) {
        referentiewaardeRepository.save(referentiewaarde);
    }

    // read
    public List<Referentiewaarde> read() {
        // Verwijderen van alle referentiewaardes met geen gekoppelde test
        List<Referentiewaarde> referentiewaardes = referentiewaardeRepository.findAll();
        for (Referentiewaarde rf : referentiewaardes) {
            if (rf.getTest() == null) {
                referentiewaardeRepository.delete(rf);
            }
        }

        return referentiewaardeRepository.findAll();
    }

    // update
    public ResponseEntity<Referentiewaarde> update(Long id, Referentiewaarde referentiewaarde) {
        Referentiewaarde updateReferentiewaarde = referentiewaardeRepository.findById(id);
        if (updateReferentiewaarde != null) {
            updateReferentiewaarde.setTest(referentiewaarde.getTest());
            updateReferentiewaarde.setWaarde(referentiewaarde.getWaarde());
            referentiewaardeRepository.save(updateReferentiewaarde);
            return new ResponseEntity<>(updateReferentiewaarde, org.springframework.http.HttpStatus.OK);
        }
        return new ResponseEntity<>(org.springframework.http.HttpStatus.NOT_FOUND);
    }

    // delete
    public ResponseEntity<Integer> delete(Long id) {
        Referentiewaarde deleteReferentiewaarde = referentiewaardeRepository.findById(id);
        if (deleteReferentiewaarde != null){
            referentiewaardeRepository.delete(deleteReferentiewaarde);
            return new ResponseEntity<>(referentiewaardeRepository.findAll().size(), org.springframework.http.HttpStatus.OK);
        }
        return new ResponseEntity<>(referentiewaardeRepository.findAll().size(), org.springframework.http.HttpStatus.NOT_FOUND);
    }

    // getbyid
    public Referentiewaarde getreferentiewaarde(Long id) {
        return referentiewaardeRepository.findById(id);
    }

}
