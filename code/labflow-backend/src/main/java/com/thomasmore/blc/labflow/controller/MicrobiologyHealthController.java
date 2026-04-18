package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.repository.microbiology.StaalRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/microbiology")
public class MicrobiologyHealthController {

    @Autowired
    @Qualifier("microbiologyStaalRepository")
    private StaalRepository microbiologyModulePlaceholderRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        long count = microbiologyModulePlaceholderRepository.count();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "persistenceUnit", "microbiology",
                "placeholderRows", count
        ));
    }
}
