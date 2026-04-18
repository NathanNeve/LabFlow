package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.dto.MicrobiologyStaalUpdateRequest;
import com.thomasmore.blc.labflow.entity.microbiology.Staal;
import com.thomasmore.blc.labflow.entity.microbiology.StaalType;
import com.thomasmore.blc.labflow.repository.microbiology.StaalRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTypeRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Path;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Service
@Transactional("microbiologyTransactionManager")
public class MicrobiologyStaalService {

    @Autowired
    private StaalRepository staalRepository;

    @Autowired
    private StaalTypeRepository staalTypeRepository;

    public Page<Staal> findStalen(int page, int size, String search, String dateStr) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("staalCode").descending());

        Specification<Staal> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                Path<?> staalCodePath = root.get("staalCode");
                if (staalCodePath.getJavaType().equals(Long.class)) {
                    try {
                        Long searchAsLong = Long.parseLong(search.trim());
                        return cb.equal(root.get("staalCode"), searchAsLong);
                    } catch (NumberFormatException e) {
                        return cb.disjunction();
                    }
                }
                return cb.disjunction();
            });
        }

        if (dateStr != null && !dateStr.isBlank()) {
            try {
                LocalDate date = LocalDate.parse(dateStr);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59, 999_999_999);
                spec = spec.and((root, query, cb) ->
                        cb.between(root.get("patientGeboorteDatum"), startOfDay, endOfDay));
            } catch (DateTimeParseException e) {
                // ignore invalid date filter
            }
        }

        Page<Staal> staalPage = staalRepository.findAll(spec, pageable);
        staalPage.getContent().forEach(this::initializeForJson);
        return staalPage;
    }

    private void initializeForJson(Staal staal) {
        if (staal == null) {
            return;
        }
        Hibernate.initialize(staal.getStaalType());
    }

    public ResponseEntity<Staal> update(Long id, MicrobiologyStaalUpdateRequest body) {
        Staal existing = staalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + id));

        if (body.getStaalCode() != null
                && !body.getStaalCode().equals(existing.getStaalCode())
                && staalRepository.existsByStaalCodeAndIdNot(body.getStaalCode(), id)) {
            throw new UniqueConstraintViolationException("Staalcode already exists");
        }

        StaalType type = staalTypeRepository.findById(body.getStaalTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid staalTypeId"));

        existing.setStaalCode(body.getStaalCode());
        existing.setPatientVoornaam(body.getPatientVoornaam());
        existing.setPatientAchternaam(body.getPatientAchternaam());
        existing.setPatientGeboorteDatum(body.getPatientGeboorteDatum());
        if (body.getPatientGeslacht() != null && !body.getPatientGeslacht().isEmpty()) {
            existing.setPatientGeslacht(body.getPatientGeslacht().charAt(0));
        }
        existing.setLaborantNaam(body.getLaborantNaam());
        existing.setLaborantRnummer(body.getLaborantRnummer());
        existing.setStaalType(type);

        Staal saved = staalRepository.save(existing);
        initializeForJson(saved);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    public ResponseEntity<Void> delete(Long id) {
        Staal existing = staalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + id));
        try {
            staalRepository.delete(existing);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
