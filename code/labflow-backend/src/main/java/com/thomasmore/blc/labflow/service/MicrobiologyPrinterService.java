package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.entity.microbiology.Staal;
import com.thomasmore.blc.labflow.entity.microbiology.StaalTestVoedingsbodem;
import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;
import com.thomasmore.blc.labflow.repository.microbiology.StaalRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTestVoedingsbodemRepository;

import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional("microbiologyTransactionManager")
public class MicrobiologyPrinterService {

    @Autowired
    @Qualifier("microbiologyStaalRepository")
    private StaalRepository staalRepository;

    @Autowired
    @Qualifier("microbiologyStaalTestVoedingsbodemRepository")
    private StaalTestVoedingsbodemRepository staalTestVoedingsbodemRepository;

    public ResponseEntity<String> printLabel(Long staalId, int amountOfCopies, List<Long> voedingsbodemFilterIds) {
        try {
            if (amountOfCopies < 0) {
                return ResponseEntity.badRequest().body("Amount of copies must be greater than 0");
            }

            Staal staal = staalRepository.findById(staalId)
                    .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + staalId));
            Hibernate.initialize(staal.getStaalType());

            if (staal.getPatientGeboorteDatum() == null) {
                return ResponseEntity.badRequest().body("Patient geboortedatum ontbreekt");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedGeboorte = staal.getPatientGeboorteDatum().toLocalDate().format(formatter);

            char geslacht = staal.getPatientGeslacht();
            String formattedGeslacht;
            if (geslacht == 'M') {
                formattedGeslacht = "Man";
            } else if (geslacht == 'V') {
                formattedGeslacht = "Vrouw";
            } else {
                formattedGeslacht = "X";
            }

            List<StaalTestVoedingsbodem> links = staalTestVoedingsbodemRepository.findByStaalId(staalId);
            Set<Voedingsbodem> voedingsbodems = new LinkedHashSet<>();
            for (StaalTestVoedingsbodem link : links) {
                Hibernate.initialize(link.getVoedingsbodem());
                voedingsbodems.add(link.getVoedingsbodem());
            }
            List<Voedingsbodem> sortedVbs = voedingsbodems.stream()
                    .sorted(Comparator.comparing(Voedingsbodem::getNaam, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();

            boolean onlySpecificVbs = voedingsbodemFilterIds != null && !voedingsbodemFilterIds.isEmpty();
            if (onlySpecificVbs) {
                Set<Long> filterSet = new LinkedHashSet<>(voedingsbodemFilterIds);
                sortedVbs = sortedVbs.stream()
                        .filter(vb -> vb.getId() != null && filterSet.contains(vb.getId()))
                        .toList();
                if (sortedVbs.isEmpty()) {
                    return ResponseEntity.badRequest().body("Geen geldige voedingsbodems voor labelgeneratie");
                }
            }

            String zplCode = "";

            for (int i = 0; i < amountOfCopies; i++) {
                if (!onlySpecificVbs) {
                    zplCode += "^XA\n"
                            + "^PW450\n"
                            + "^LL250\n"
                            + "^FO10,15^GB430,230,3^FS\n"
                            + "^FO20,25^A0N,30,30^FD" + nullSafe(staal.getPatientVoornaam()) + "^FS\n"
                            + "^FO200,25^A0N,30,30^FD" + nullSafe(staal.getPatientAchternaam()) + "^FS\n"
                            + "^FO20,65^A0N,25,25^FD" + "Geboorte: " + formattedGeboorte + "^FS\n"
                            + "^FO20,105^A0N,25,25^FD" + "Geslacht: " + formattedGeslacht + "^FS\n"
                            + "^FO90,140^BY3^BCN,60,,,,A^FD" + staal.getStaalCode() + "^FS\n"
                            + "^XZ\n";
                }

                for (Voedingsbodem vb : sortedVbs) {
                    String naam = vb.getNaam() != null ? vb.getNaam() : "";
                    zplCode += "^XA\n"
                            + "^PW450\n"
                            + "^LL250\n"
                            + "^FO10,15^GB430,230,3^FS\n"
                            + "^FO20,25^A0N,30,30^FD" + nullSafe(staal.getPatientVoornaam()) + "^FS\n"
                            + "^FO200,25^A0N,30,30^FD" + nullSafe(staal.getPatientAchternaam()) + "^FS\n"
                            + "^FO20,65^A0N,25,25^FD" + "Geboorte: " + formattedGeboorte + "^FS\n"
                            + "^FO20,105^A0N,25,25^FD" + "Geslacht: " + formattedGeslacht + "^FS\n"
                            + "^FO390,65^A0N,30,30^FR^FWR^FD" + naam + "^FS\n"
                            + "^FO355,65^A0N,30,30^FR^FWR^FD" + "^FS\n"
                            + "^FO90,140^BY3^BCN,60,,,,A^FD" + staal.getStaalCode() + "^FS\n"
                            + "^XZ\n";
                }
            }

            return ResponseEntity.ok(zplCode);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error while printing label");
        }
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
