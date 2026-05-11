package com.thomasmore.blc.labflow;

import com.thomasmore.blc.labflow.entity.hematology.Staal;
import com.thomasmore.blc.labflow.service.StaalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class StaalConcurrencyTests {

    @Autowired
    private StaalService staalService;

    @Test
    void createStaal_concurrently_allocatesUniqueIncreasingCodes() throws Exception {
        int n = 25;

        ExecutorService exec = Executors.newFixedThreadPool(Math.min(8, n));
        try {
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                int idx = i;
                tasks.add(() -> {
                    Staal s = new Staal();
                    s.setStaalCode(null); // server allocates
                    s.setPatientVoornaam("Test");
                    s.setPatientAchternaam("User" + idx);
                    s.setPatientGeboorteDatum(LocalDate.of(1990, 1, 1));
                    s.setPatientGeslacht('M');
                    s.setLaborantNaam("Lab");
                    s.setLaborantRnummer("R0000001");
                    s.setUserId(1L); // seeded by AuthDataLoader
                    // ensure non-null list
                    s.setRegisteredTests(new ArrayList<>());

                    Staal created = staalService.createStaal(s);
                    return created.getStaalCode();
                });
            }

            List<Future<Long>> futures = exec.invokeAll(tasks);

            Set<Long> codes = ConcurrentHashMap.newKeySet();
            for (Future<Long> f : futures) {
                codes.add(f.get());
            }

            assertEquals(n, codes.size(), "Expected all staalCodes to be unique");

            List<Long> sorted = codes.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            int year = Year.now().getValue();
            assertTrue(
                    String.valueOf(sorted.get(0)).startsWith(String.valueOf(year)),
                    "Expected staalCode to start with current year"
            );

            // strict increasing (no duplicates already checked)
            for (int i = 1; i < sorted.size(); i++) {
                assertTrue(sorted.get(i) > sorted.get(i - 1), "Expected staalCodes to be strictly increasing");
            }
        } finally {
            exec.shutdownNow();
        }
    }
}

