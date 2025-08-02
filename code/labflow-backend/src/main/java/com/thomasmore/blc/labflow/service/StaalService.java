package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.entity.Staal;
import com.thomasmore.blc.labflow.entity.StaalTest;
import com.thomasmore.blc.labflow.entity.Test;
import com.thomasmore.blc.labflow.entity.User;
import com.thomasmore.blc.labflow.repository.StaalRepository;
import com.thomasmore.blc.labflow.repository.TestRepository;
import com.thomasmore.blc.labflow.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class StaalService {
    @Autowired
    private StaalRepository staalRepository;

    @Autowired
    private TestRepository testRepository;
    @Autowired
    private UserRepository userRepository;

    // Create
    public void createStaal(Staal staal) {
        // lopen door elke test
        if (staalRepository.findByStaalCode(staal.getStaalCode()) == null) {
            for (StaalTest registeredTest : staal.getRegisteredTests()) {
                // testobject ophalen en koppelen met staal
                Test test = testRepository.findByTestCode(registeredTest.getTest().getTestCode());
                registeredTest.setTest(test);
            }
            staal.setStatus(Staal.Status.AANGEMAAKT);
            staalRepository.save(staal);
        } else {
            throw new UniqueConstraintViolationException("Staalcode already exists");
        }

    }

    // Read all
    public List<Staal> read() {
        return staalRepository.findAllByOrderByStaalCodeDesc();
    }

    // Read Paginated Stalen
    public Page<Staal> readAmount(int page, int size, String search, String status, String dateStr) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("staalCode").descending());

        Specification<Staal> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            // lambda functie om specificaties mee te bouwen
            // root representeert wat we queryen, in dit geval staal
            // query kan gebruikt worden om bv te joinen op een andere klasse
            // cb 'criteriabuilder' gebruiken we om expressies en predicates mee te bouwen
            spec = spec.and((root, query, cb) -> {
                Path<?> staalCodePath = root.get("staalCode");

                if (staalCodePath.getJavaType().equals(String.class)) {
                    // If staalCode is a String, use LIKE for partial search
                    return cb.like(cb.lower(root.get("staalCode")), "%" + search.toLowerCase() + "%");
                } else if (staalCodePath.getJavaType().equals(Long.class)) {
                    // If staalCode is a Long, try to parse and search exactly
                    try {
                        Long searchAsLong = Long.parseLong(search);
                        return cb.equal(root.get("staalCode"), searchAsLong);
                    } catch (NumberFormatException e) {
                        // Return false condition to avoid error (no match)
                        return cb.disjunction(); // Equivalent to `false`
                    }
                } else {
                    // Unsupported type
                    return cb.disjunction();
                }
            });
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (dateStr != null && !dateStr.isBlank()) {
            try {
                LocalDate date = LocalDate.parse(dateStr);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);

                spec = spec.and((root, query, cb) ->
                        cb.between(root.get("aanmaakDatum"), startOfDay, endOfDay));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format: " + dateStr);
            }
        }

        return staalRepository.findAll(spec, pageable);
    }

    // Update
    public ResponseEntity<Staal> update(Long id, Staal staal) {
        Staal existingStaal = staalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + id));
        User user = userRepository.findById(staal.getUser().getId());
        if (existingStaal != null) {

            // Update the fields
            existingStaal.setStaalCode(staal.getStaalCode());
            existingStaal.setLaborantNaam(staal.getLaborantNaam());
            existingStaal.setUser(user);
            existingStaal.setLaborantRnummer(staal.getLaborantRnummer());
            existingStaal.setPatientAchternaam(staal.getPatientAchternaam());
            existingStaal.setPatientVoornaam(staal.getPatientVoornaam());
            existingStaal.setPatientGeboorteDatum(staal.getPatientGeboorteDatum());
            existingStaal.setPatientGeslacht(staal.getPatientGeslacht());

            // loopen door elke test en de link leggen tussen test en staal
            for (StaalTest registeredTest : staal.getRegisteredTests()) {
                Test test = testRepository.findByTestCode(registeredTest.getTest().getTestCode());

                if (test != null) {
                    registeredTest.setTest(test);
                    registeredTest.setStaal(existingStaal);
                    existingStaal.getRegisteredTests().add(registeredTest);
                } else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            // Save the updated Staal entity, along with the StaalTest associations
            Staal updatedStaal = staalRepository.save(existingStaal);
            return new ResponseEntity<>(updatedStaal, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Return NOT_FOUND if the Staal entity doesn't exist
        }
    }

    // Delete
    public ResponseEntity<Integer> delete(Long id) {
        Staal existingStaal = staalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + id));
        if (existingStaal != null) {
            staalRepository.delete(existingStaal);
            return new ResponseEntity<>(staalRepository.findAll().size(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(staalRepository.findAll().size(), HttpStatus.NOT_FOUND);
        }
    }

    // Get by id
    public Optional<Staal> readById(Long id) {
        return staalRepository.findById(id);
    }

    // Get staal by staalcode
    public Staal readByStaalCode(Long staalCode) {
        return staalRepository.findByStaalCode(staalCode);
    }

    // increment van de grootste testcode voor aanmaken nieuwe test
    public String newStaalCode() {

        String grootsteStaalCode = staalRepository.findLargestStaalCode();
        String huidigJaar = String.valueOf(Year.now().getValue());
        String nieuweStaalCode;

        if (grootsteStaalCode != null && grootsteStaalCode.startsWith(huidigJaar)) {
            // verwijderen van eerste 4 karakters (het jaartal) en increment met 1 "YYYY" / "XXXXXX"+1
            int increment = Integer.parseInt(grootsteStaalCode.substring(4)) + 1;
            // https://www.w3schools.com/java/ref_string_format.asp
            // zorgt ervoor dat het incremented deel altijd 6 getallen groot is
            nieuweStaalCode = huidigJaar + String.format("%06d", increment);
        } else {
            // In het geval van een nieuw jaar, starten we vanaf 1
            nieuweStaalCode = huidigJaar + "000001";
        }
        return nieuweStaalCode;
    }

    // patch staalstatus
    public ResponseEntity<Staal> patchStatus(String status, Long id) {
        Staal toPatchStaal = staalRepository.findByStaalCode(id);
        if (toPatchStaal != null) {
            // converten van status pathvariabele naar instance van Status object
            Staal.Status newStatus = Staal.Status.valueOf(status.toUpperCase());
            toPatchStaal.setStatus(newStatus);
            staalRepository.save(toPatchStaal);
            return new ResponseEntity<>(toPatchStaal, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // returns een lijst van unieke statussen die aan een staal gekoppeld kunnen worden
    public List<Staal.Status> getstatus() {
        return staalRepository.findDistinctStaalStatus();
    }
}
