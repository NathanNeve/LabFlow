package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.entity.StaalTest;
import com.thomasmore.blc.labflow.entity.StaalTestId;
import com.thomasmore.blc.labflow.repository.StaalTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class StaalTestService {
    @Autowired
    private StaalTestRepository staalTestRepository;

    // read staal test
    public StaalTest readStaalTest(Long testId, Long staalId) {
        return staalTestRepository.findByStaal_IdAndTest_Id(staalId, testId);
    }

    // update the staal test
    public ResponseEntity<StaalTest> updateStaalTest(Long staalId, Long testId, StaalTest staalTest) {
        StaalTest existingStaalTest = staalTestRepository.findByStaal_IdAndTest_Id(staalId, testId);
        if (existingStaalTest != null) {
            // Update the fields
            existingStaalTest.setResult(staalTest.getResult());
            existingStaalTest.setNote(staalTest.getNote());
            existingStaalTest.setFailed(staalTest.getFailed());

            // Save the updated entity
            staalTestRepository.save(existingStaalTest);
            return ResponseEntity.ok(existingStaalTest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
