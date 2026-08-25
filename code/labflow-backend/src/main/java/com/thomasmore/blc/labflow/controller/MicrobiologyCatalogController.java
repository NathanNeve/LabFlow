package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyCatalogNameRequest;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyCatalogTestRequest;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyCatalogTestResponse;
import com.thomasmore.blc.labflow.entity.microbiology.Antibiotica;
import com.thomasmore.blc.labflow.entity.microbiology.StaalType;
import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;
import com.thomasmore.blc.labflow.service.MicrobiologyCatalogService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/microbiology")
public class MicrobiologyCatalogController {

    @Autowired
    private MicrobiologyCatalogService microbiologyCatalogService;

    @GetMapping("/antibiotica")
    public List<Antibiotica> listAntibiotica() {
        return microbiologyCatalogService.listAntibiotica();
    }

    @PostMapping("/antibiotica")
    public ResponseEntity<Antibiotica> createAntibiotica(@RequestBody MicrobiologyCatalogNameRequest body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(microbiologyCatalogService.createAntibiotica(body));
        } catch (UniqueConstraintViolationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/antibiotica/{id}")
    public ResponseEntity<Antibiotica> updateAntibiotica(
            @PathVariable Long id,
            @RequestBody MicrobiologyCatalogNameRequest body
    ) {
        try {
            return ResponseEntity.ok(microbiologyCatalogService.updateAntibiotica(id, body));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UniqueConstraintViolationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/antibiotica/{id}")
    public ResponseEntity<Void> deleteAntibiotica(@PathVariable Long id) {
        try {
            microbiologyCatalogService.deleteAntibiotica(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/voedingsbodems")
    public List<Voedingsbodem> listVoedingsbodems() {
        return microbiologyCatalogService.listVoedingsbodems();
    }

    @PostMapping("/voedingsbodems")
    public ResponseEntity<Voedingsbodem> createVoedingsbodem(@RequestBody MicrobiologyCatalogNameRequest body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(microbiologyCatalogService.createVoedingsbodem(body));
        } catch (UniqueConstraintViolationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/voedingsbodems/{id}")
    public ResponseEntity<Voedingsbodem> updateVoedingsbodem(
            @PathVariable Long id,
            @RequestBody MicrobiologyCatalogNameRequest body
    ) {
        try {
            return ResponseEntity.ok(microbiologyCatalogService.updateVoedingsbodem(id, body));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UniqueConstraintViolationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/voedingsbodems/{id}")
    public ResponseEntity<Void> deleteVoedingsbodem(@PathVariable Long id) {
        try {
            microbiologyCatalogService.deleteVoedingsbodem(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/staal-types")
    public ResponseEntity<StaalType> createStaalType(@RequestBody MicrobiologyCatalogNameRequest body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(microbiologyCatalogService.createStaalType(body));
        } catch (UniqueConstraintViolationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/staal-types/{id}")
    public ResponseEntity<StaalType> updateStaalType(
            @PathVariable Long id,
            @RequestBody MicrobiologyCatalogNameRequest body
    ) {
        try {
            return ResponseEntity.ok(microbiologyCatalogService.updateStaalType(id, body));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UniqueConstraintViolationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/staal-types/{id}")
    public ResponseEntity<Void> deleteStaalType(@PathVariable Long id) {
        try {
            microbiologyCatalogService.deleteStaalType(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/catalog/tests")
    public List<MicrobiologyCatalogTestResponse> listCatalogTests() {
        return microbiologyCatalogService.listTests();
    }

    @PostMapping("/catalog/tests")
    public ResponseEntity<MicrobiologyCatalogTestResponse> createCatalogTest(
            @RequestBody MicrobiologyCatalogTestRequest body
    ) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(microbiologyCatalogService.createTest(body));
        } catch (UniqueConstraintViolationException e) {
            throw e;
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/catalog/tests/{id}")
    public ResponseEntity<MicrobiologyCatalogTestResponse> updateCatalogTest(
            @PathVariable Long id,
            @RequestBody MicrobiologyCatalogTestRequest body
    ) {
        try {
            return ResponseEntity.ok(microbiologyCatalogService.updateTest(id, body));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UniqueConstraintViolationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/catalog/tests/{id}")
    public ResponseEntity<Void> deleteCatalogTest(@PathVariable Long id) {
        try {
            microbiologyCatalogService.deleteTest(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
