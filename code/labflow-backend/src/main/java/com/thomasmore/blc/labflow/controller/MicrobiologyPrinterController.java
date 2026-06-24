package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.service.MicrobiologyPrinterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/microbiology/printer")
public class MicrobiologyPrinterController {

    @Autowired
    private MicrobiologyPrinterService microbiologyPrinterService;

    @GetMapping("/labels/{staalId}/{amountOfCopies}")
    public ResponseEntity<String> generateLabel(
            @PathVariable Long staalId,
            @PathVariable int amountOfCopies,
            @RequestParam(required = false) String voedingsbodemIds
    ) {
        List<Long> filterIds = parseVoedingsbodemIds(voedingsbodemIds);
        return microbiologyPrinterService.printLabel(staalId, amountOfCopies, filterIds);
    }

    private static List<Long> parseVoedingsbodemIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
