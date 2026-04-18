package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.entity.hematology.Eenheid;
import com.thomasmore.blc.labflow.repository.hematology.EenheidRepository;
import com.thomasmore.blc.labflow.repository.hematology.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@Transactional("hematologyTransactionManager")
public class EenheidService {
    @Autowired
    private EenheidRepository eenheidRepository;
    @Autowired
    private TestRepository testRepository;

    // create
    public void create(Eenheid eenheid) {
        eenheidRepository.save(eenheid);
    }

    // read
    public List<Eenheid> read() {
        return eenheidRepository.findAllByOrderByNaam();
    }

    // update
    public ResponseEntity<Eenheid> update(@PathVariable Long id, @RequestBody Eenheid eenheid) {
        Eenheid updateEenheid = eenheidRepository.findById(id);
        if (updateEenheid != null) {
            updateEenheid.setAfkorting(eenheid.getAfkorting());
            updateEenheid.setNaam(eenheid.getNaam());
            eenheidRepository.save(updateEenheid);
            return new ResponseEntity<>(updateEenheid, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // delete
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        Eenheid deleteEenheid = eenheidRepository.findById(id);
        if (testRepository.existsByEenheidId(id)) {
            throw new IllegalStateException("Kan eenheid niet verwijderen want deze is gelinked aan één of meerdere tests");
        }
        if (eenheidRepository.findById(id) != null) {
            eenheidRepository.delete(deleteEenheid);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
