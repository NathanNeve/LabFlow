package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.service.MicrobiologyPrinterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/microbiology/printer")
public class MicrobiologyPrinterController {

    @Autowired
    private MicrobiologyPrinterService microbiologyPrinterService;

    @GetMapping("/labels/{staalId}/{amountOfCopies}")
    public ResponseEntity<String> generateLabel(@PathVariable Long staalId, @PathVariable int amountOfCopies) {
        return microbiologyPrinterService.printLabel(staalId, amountOfCopies);
    }
}
