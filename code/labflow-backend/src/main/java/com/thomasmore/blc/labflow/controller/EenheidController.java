package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.entity.hematology.Eenheid;
import com.thomasmore.blc.labflow.service.EenheidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EenheidController {
    @Autowired
    private EenheidService eenheidService;

    // create
    @PostMapping("/createeenheid")
    public void create(@RequestBody Eenheid eenheid) {
        eenheidService.create(eenheid);
    }

    // read
    @GetMapping("/readeenheid")
    public List<Eenheid> read() {
        return eenheidService.read();
    }

    // update
    @PutMapping("/updateeenheid/{id}")
    public void update(@PathVariable Long id, @RequestBody Eenheid eenheid) {
        eenheidService.update(id, eenheid);
    }

    // delete
    @DeleteMapping("/deleteeenheid/{id}")
    public void delete(@PathVariable Long id) {
        eenheidService.delete(id);
    }

}
