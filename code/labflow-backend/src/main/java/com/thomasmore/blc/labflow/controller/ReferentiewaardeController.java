package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.entity.hematology.Referentiewaarde;
import com.thomasmore.blc.labflow.service.ReferentiewaardeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/api")
@RequestMapping("/api")
public class ReferentiewaardeController {
    @Autowired
    private ReferentiewaardeService referentiewaardeService;

    // create
    @PostMapping("/createreferentiewaarde")
    public Referentiewaarde create(@RequestBody Referentiewaarde referentiewaarde) {
        referentiewaardeService.create(referentiewaarde);
        return referentiewaardeService.getreferentiewaarde(referentiewaarde.getId());
    }

    // read
    @GetMapping("/referentiewaarden")
    public List<Referentiewaarde> read() {
        return referentiewaardeService.read();
    }

    // update
    @PutMapping("/updatereferentiewaarde/{id}")
    public ResponseEntity<Referentiewaarde> update(@PathVariable Long id, @RequestBody Referentiewaarde referentiewaarde) {
        return referentiewaardeService.update(id, referentiewaarde);
    }

    // delete
    @DeleteMapping("/deletereferentiewaarde/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return referentiewaardeService.delete(id);
    }

}
