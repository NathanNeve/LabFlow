package com.thomasmore.blc.labflow.controller;

import com.itextpdf.text.DocumentException;
import com.thomasmore.blc.labflow.repository.microbiology.StaalRepository;
import com.thomasmore.blc.labflow.service.MicrobiologyPdfGeneratorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/microbiology/pdf")
public class MicrobiologyPdfController {

    @Autowired
    private MicrobiologyPdfGeneratorService microbiologyPdfGeneratorService;

    @Autowired
    @Qualifier("microbiologyStaalRepository")
    private StaalRepository staalRepository;

    @GetMapping("/generatelabel/{id}")
    public ResponseEntity<byte[]> generateLabelPdf(
            @PathVariable Long id,
            @RequestParam(required = false) String voedingsbodemIds
    ) {
        try {
            List<Long> filterIds = parseVoedingsbodemIds(voedingsbodemIds);
            byte[] pdfBytes = microbiologyPdfGeneratorService.generateLabelPdf(id, filterIds);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "label.pdf");
            headers.add("X-Filename", "filename=\"Labels_microbiology_" + id + ".pdf\"");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (DocumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/generateresults/{id}")
    public ResponseEntity<byte[]> generateResultsPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = microbiologyPdfGeneratorService.generateResultsPdf(id);
            var staalOpt = staalRepository.findById(id);
            if (staalOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            var staal = staalOpt.get();
            String filename = "resultaten_" + staal.getPatientAchternaam() + "_" + staal.getPatientVoornaam() + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add("X-Filename", "filename=\"" + filename + "\"");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (DocumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
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
