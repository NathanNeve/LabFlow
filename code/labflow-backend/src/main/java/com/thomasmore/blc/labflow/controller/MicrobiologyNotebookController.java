package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyAntibiogramUpdateRequest;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyCommentaarRequest;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyGramkleuringDto;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyNotebookResponse;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyStaalTestUpdateRequest;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyVoltooidRequest;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyVoedingsbodemLogsRequest;
import com.thomasmore.blc.labflow.service.MicrobiologyNotebookService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/microbiology")
public class MicrobiologyNotebookController {

    @Autowired
    private MicrobiologyNotebookService microbiologyNotebookService;

    @GetMapping("/staal/{id}/notebook")
    public ResponseEntity<MicrobiologyNotebookResponse> getNotebook(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(microbiologyNotebookService.getNotebook(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/staal/{id}/commentaar")
    public ResponseEntity<Void> updateCommentaar(@PathVariable Long id, @RequestBody MicrobiologyCommentaarRequest body) {
        try {
            microbiologyNotebookService.updateCommentaar(id, body != null ? body.getCommentaar() : null);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/staal/{id}/voltooid/{section}")
    public ResponseEntity<Void> updateVoltooid(
            @PathVariable Long id,
            @PathVariable String section,
            @RequestBody MicrobiologyVoltooidRequest body
    ) {
        try {
            boolean voltooid = body != null && body.isVoltooid();
            microbiologyNotebookService.updateVoltooid(id, section, voltooid);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/staal/{staalId}/staal-test/{staalTestId}")
    public ResponseEntity<Void> updateStaalTest(
            @PathVariable Long staalId,
            @PathVariable Long staalTestId,
            @RequestBody MicrobiologyStaalTestUpdateRequest body
    ) {
        try {
            microbiologyNotebookService.updateStaalTest(staalId, staalTestId, body);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/staal/{id}/voedingsbodems/{linkId}/commentaar")
    public ResponseEntity<Void> updateVoedingsbodemCommentaar(
            @PathVariable Long id,
            @PathVariable Long linkId,
            @RequestBody MicrobiologyCommentaarRequest body
    ) {
        try {
            microbiologyNotebookService.updateVoedingsbodemCommentaar(
                    id, linkId, body != null ? body.getCommentaar() : null);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/staal/{id}/voedingsbodems/{linkId}/logs")
    public ResponseEntity<Void> syncVoedingsbodemLogs(
            @PathVariable Long id,
            @PathVariable Long linkId,
            @RequestBody MicrobiologyVoedingsbodemLogsRequest body
    ) {
        try {
            microbiologyNotebookService.syncVoedingsbodemLogs(id, linkId, body);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/staal/{id}/gramkleuring")
    public ResponseEntity<Void> updateGramkleuring(
            @PathVariable Long id,
            @RequestBody MicrobiologyGramkleuringDto body
    ) {
        try {
            microbiologyNotebookService.updateGramkleuring(id, body);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/staal/{id}/antibiogram")
    public ResponseEntity<Void> updateAntibiogram(
            @PathVariable Long id,
            @RequestBody MicrobiologyAntibiogramUpdateRequest body
    ) {
        try {
            microbiologyNotebookService.updateAntibiogram(id, body);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/staal/{id}/klaar")
    public ResponseEntity<Void> markStaalKlaar(@PathVariable Long id) {
        try {
            microbiologyNotebookService.markStaalKlaar(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
