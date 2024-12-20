package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.entity.Staal;
import com.thomasmore.blc.labflow.entity.StaalTest;
import com.thomasmore.blc.labflow.entity.Test;
import com.thomasmore.blc.labflow.entity.Testcategorie;
import com.thomasmore.blc.labflow.repository.StaalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/*
* ZPL code:
*
* Deze code die onderaan gegenereerd wordt, is een Zebra Programming Language code. Dit is een taal die gebruikt wordt
* om Zebra printers aan te sturen. Dit zijn allemaal kleine individuele commando's die de printer begrijpt om acties
* uit te voeren. Onderaan vind je een link naar een handige website om deze commando's op te zoeken en te begrijpen wat
* ze doen. De 2de link is een kleine editor waarin je kan spelen met de commando's en live kan zien wat het resultaat is.
*
* commando online documentatie: https://labelary.com/docs.html
* commando previewer: https://labelary.com/viewer.html
*
* */

@Service
public class PrinterService {
    @Autowired
    private StaalRepository staalRepository;

    public ResponseEntity<String> printLabel(Long staalId, int amountOfCopies) {
        // probeer zpl label code te genereren
        try {
            // kijk of het aantal kopieën groter is dan 0
            if (amountOfCopies < 0) {
                return ResponseEntity.badRequest().body("Amount of copies must be greater than 0");
            }

            // staal ophalen
            Staal staal = staalRepository.findById(staalId);

            // geboortedatum formatter
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedGeboorte = staal.getPatientGeboorteDatum().format(formatter);

            // alle testcategorieën ophalen
            Set<Testcategorie> testcategorieSet = staal.getRegisteredTests().stream().map(StaalTest::getTest)
                    .map(Test::getTestcategorie)
                    .filter(testcategorie -> testcategorie.getId() != 7)
                    .collect(Collectors.toSet());

            // formateer geslacht
            char geslacht = staal.getPatientGeslacht();
            String formattedGeslacht;
            if (geslacht == 'M') {
                formattedGeslacht = "Man";
            } else {
                formattedGeslacht = "Vrouw";
            }

            String zplCode = "";

            // een volledige uitvoering van het label maal het aantal kopieën
            for (int i = 0; i < amountOfCopies; i++) {
                // basis label met patiënt info
                zplCode += "^XA\n" +
                        "^PW450\n" +
                        "^LL250\n" +
                        "^FO10,15^GB430,230,3^FS\n" +
                        "^FO20,25^A0N,30,30^FD" + staal.getPatientVoornaam() + "^FS\n" +
                        "^FO200,25^A0N,30,30^FD" + staal.getPatientAchternaam() + "^FS\n" +
                        "^FO20,65^A0N,25,25^FD" + "Geboorte: " + formattedGeboorte + "^FS\n" +
                        "^FO20,105^A0N,25,25^FD" + "Geslacht: " + formattedGeslacht + "^FS\n" +
                        // barcode
                        "^FO90,140^BY3^BCN,60,,,,A^FD" + staal.getStaalCode() + "^FS\n" +
                        "^XZ\n";

                // label per categorie
                for (Testcategorie testcategorie : testcategorieSet) {
                    zplCode += "^XA\n" +
                            "^PW450\n" +
                            "^LL250\n" +
                            "^FO10,15^GB430,230,3^FS\n" +
                            "^FO20,25^A0N,30,30^FD" + staal.getPatientVoornaam() + "^FS\n" +
                            "^FO200,25^A0N,30,30^FD" + staal.getPatientAchternaam() + "^FS\n" +
                            "^FO20,65^A0N,25,25^FD" + "Geboorte: " + formattedGeboorte + "^FS\n" +
                            "^FO20,105^A0N,25,25^FD" + "Geslacht: " + formattedGeslacht + "^FS\n" +
                            "^FO390,65^A0N,30,30^FR^FWR^FD" + testcategorie.getNaam() + "^FS\n" +
                            "^FO355,65^A0N,30,30^FR^FWR^FD" + testcategorie.getKleurnaam() + "^FS\n" +
                            // Bacode
                            "^FO90,140^BY3^BCN,60,,,,A^FD" + staal.getStaalCode() + "^FS\n" +
                            "^XZ\n";
                }
            }

            return ResponseEntity.ok(zplCode);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error while printing label");
        }
    }
}
