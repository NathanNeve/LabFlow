package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.entity.Staal;
import com.thomasmore.blc.labflow.service.StaalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StaalController {
    @Autowired
    private StaalService staalService;

    // create
    @PostMapping("/createstaal")
    public ResponseEntity<String> createStaal(@RequestBody Staal staal) {
        staalService.createStaal(staal);
        return ResponseEntity.status(HttpStatus.CREATED).body("Staal created successfully");
    }

    // read
    @GetMapping("/stalen")
    public List<Staal> read() {
        return staalService.read();
    }

    // read paginated, default worden de eerste 25 stalen gegeven
    // optionele parameters om te filteren op staalcode, status of aanmaakdatum
    @GetMapping("/staal")
    public Page<Staal> getPaginatedStalen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date
    ) {
        return staalService.readAmount(page, size, search, status, date);
    }


    // update
    @PutMapping("/updatestaal/{id}")
    public ResponseEntity<Staal> update(@PathVariable Long id, @RequestBody Staal staal) {
        try {
            return staalService.update(id, staal);
        } catch (Exception e) {
            throw new UniqueConstraintViolationException(e.getMessage());
        }
    }

    // delete
    @DeleteMapping ("/deletestaal/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return staalService.delete(id);
    }

    // Krijg de grootste StaalCode + 1 voor een nieuwe test aan te maken
    @GetMapping("/newStaalCode")
    public String newStaalCode() { return staalService.newStaalCode(); }

    // Krijg een staal op basis van id
    @GetMapping("/staal/{code}")
    public Staal getStaal(@PathVariable Long code) {
        return staalService.readByStaalCode(code);
    }

    // Patch voor het aanpassen van een staal
    @PatchMapping("/updatestaalstatus/{status}/{id}")
    public ResponseEntity<Staal> updateStaalStatus(@PathVariable String status, @PathVariable Long id) {
        try {
            return staalService.patchStatus(status, id);
        }
        catch (Exception e) {
            throw new UniqueConstraintViolationException(e.getMessage());
        }
    }

    // get status van stalen
    @GetMapping("/getstatus")
    public List<Staal.Status> getStatus() throws Exception {
        try {
            return staalService.getstatus();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
