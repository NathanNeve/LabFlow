package com.thomasmore.blc.labflow.controller;

import com.itextpdf.text.DocumentException;
import com.thomasmore.blc.labflow.entity.Staal;
import com.thomasmore.blc.labflow.service.PdfGeneratorService;
import com.thomasmore.blc.labflow.service.StaalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdf")
public class PdfGeneratorController {
    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private StaalService staalService;

    @GetMapping("/generatelabel/{id}")
    public ResponseEntity<byte[]> generateLabelPdf(@PathVariable Long id) {

        Staal staal = staalService.readById(id);
        byte[] pdfBytes;

        try {
            pdfBytes = pdfGeneratorService.generateLabelPdf(staal);
        } catch (DocumentException e) {
            return ResponseEntity.internalServerError().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "label.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }


    @GetMapping("/generateresults/{id}")
    public ResponseEntity<byte[]> generateResultsPdf(@PathVariable Long id) {

        Staal staal = staalService.readById(id);
        byte[] pdfBytes;

        try {
            pdfBytes = pdfGeneratorService.generateResultsPdf(staal);
        } catch (DocumentException e) {
            return ResponseEntity.internalServerError().build();
        }

        String filename = "resultaten_" + staal.getPatientAchternaam() + "_" + staal.getPatientVoornaam() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("X-Filename", filename);  // Custom header

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
