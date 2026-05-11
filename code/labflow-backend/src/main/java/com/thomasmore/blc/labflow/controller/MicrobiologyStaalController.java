package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.dto.MicrobiologyStaalCreateRequest;
import com.thomasmore.blc.labflow.dto.MicrobiologyStaalUpdateRequest;
import com.thomasmore.blc.labflow.dto.MicrobiologyStaalTestsRequest;
import com.thomasmore.blc.labflow.dto.MicrobiologyTestResponse;
import com.thomasmore.blc.labflow.dto.MicrobiologyVoedingsbodemsConfirmRequest;
import com.thomasmore.blc.labflow.entity.microbiology.Staal;
import com.thomasmore.blc.labflow.entity.microbiology.StaalType;
import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTypeRepository;
import com.thomasmore.blc.labflow.service.MicrobiologyStaalService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/microbiology")
public class MicrobiologyStaalController {

    @Autowired
    private MicrobiologyStaalService microbiologyStaalService;

    @Autowired
    @Qualifier("microbiologyStaalTypeRepository")
    private StaalTypeRepository staalTypeRepository;

    @GetMapping("/staal")
    public Page<Staal> getPaginatedStalen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String date
    ) {
        return microbiologyStaalService.findStalen(page, size, search, date);
    }

    @GetMapping("/staal/{id}")
    public ResponseEntity<Staal> getStaal(@PathVariable Long id) {
        return microbiologyStaalService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tests")
    public ResponseEntity<List<MicrobiologyTestResponse>> listTests(@RequestParam Long staalTypeId) {
        return ResponseEntity.ok(microbiologyStaalService.findTestsWithVoedingsbodemsByStaalTypeId(staalTypeId));
    }

    @PostMapping("/staal")
    public ResponseEntity<Staal> create(@RequestBody MicrobiologyStaalCreateRequest body) {
        try {
            return microbiologyStaalService.create(body);
        } catch (UniqueConstraintViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/staal/{id}/tests")
    public ResponseEntity<Staal> saveTests(@PathVariable Long id, @RequestBody MicrobiologyStaalTestsRequest body) {
        try {
            return microbiologyStaalService.saveStaalTests(id, body);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/staal/{id}/tests")
    public ResponseEntity<Void> clearTests(@PathVariable Long id) {
        try {
            microbiologyStaalService.clearStaalTests(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/staal/{id}/voedingsbodems")
    public ResponseEntity<List<Voedingsbodem>> listVoedingsbodems(@PathVariable Long id) {
        try {
            if (!microbiologyStaalService.getById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(microbiologyStaalService.findPossibleVoedingsbodems(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/staal/{id}/voedingsbodems/confirm")
    public ResponseEntity<Void> confirmVoedingsbodems(
            @PathVariable Long id,
            @RequestBody MicrobiologyVoedingsbodemsConfirmRequest body
    ) {
        try {
            microbiologyStaalService.confirmVoedingsbodems(id, body);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/staal-types")
    public List<StaalType> listStaalTypes() {
        return staalTypeRepository.findAll();
    }

    @PutMapping("/staal/{id}")
    public ResponseEntity<Staal> update(
            @PathVariable Long id,
            @RequestBody MicrobiologyStaalUpdateRequest body
    ) {
        try {
            return microbiologyStaalService.update(id, body);
        } catch (UniqueConstraintViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/staal/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return microbiologyStaalService.delete(id);
    }
}
