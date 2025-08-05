package com.thomasmore.blc.labflow.controller;

import com.itextpdf.text.DocumentException;
import com.thomasmore.blc.labflow.entity.Staal;
import com.thomasmore.blc.labflow.repository.StaalRepository;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/pdf")
public class PdfGeneratorController {
    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private StaalService staalService;
    @Autowired
    private StaalRepository staalRepository;

    @GetMapping("/generatelabel/{id}")
    public ResponseEntity<byte[]> generateLabelPdf(@PathVariable Long id) {
        Optional<Staal> staalOpt = staalService.readById(id);

        if (staalOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Staal staal = staalOpt.get();
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

        Optional<Staal> staalOpt = staalService.readById(id);
        if (staalOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Staal staal = staalOpt.get();
        byte[] pdfBytes;

        // probeer genereren van pdf
        try {
            pdfBytes = pdfGeneratorService.generateResultsPdf(staal);
        } catch (DocumentException e) {
            return ResponseEntity.internalServerError().build();
        }

        // stel documentnaam samen
        String filename = "resultaten_" + staal.getPatientAchternaam() + "_" + staal.getPatientVoornaam() + ".pdf";

        // maak header voor http voor api
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("X-Filename", filename);  // Custom header

        // STATUS UPDATE
        // staal is 'klaar' testen en waarden zijn geregistreerd, pdf is afgedrukt
        staal.setStatus(Staal.Status.KLAAR);
        staalRepository.save(staal);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
