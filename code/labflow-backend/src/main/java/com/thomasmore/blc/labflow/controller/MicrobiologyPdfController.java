package com.thomasmore.blc.labflow.controller;

import com.itextpdf.text.DocumentException;
import com.thomasmore.blc.labflow.service.MicrobiologyPdfGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/microbiology/pdf")
public class MicrobiologyPdfController {

    @Autowired
    private MicrobiologyPdfGeneratorService microbiologyPdfGeneratorService;

    @GetMapping("/generatelabel/{id}")
    public ResponseEntity<byte[]> generateLabelPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = microbiologyPdfGeneratorService.generateLabelPdf(id);
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
}
