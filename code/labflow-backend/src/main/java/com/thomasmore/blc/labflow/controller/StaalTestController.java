package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.entity.hematology.StaalTest;
import com.thomasmore.blc.labflow.service.StaalTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class StaalTestController {

    @Autowired
    private StaalTestService staalTestService;

    // read staal test
    @GetMapping("/getstaaltest/{staalId}/{testId}")
    public StaalTest readStaalTest(@PathVariable Long staalId, @PathVariable Long testId) {
        return staalTestService.readStaalTest(staalId, testId);
    }

    // update staal test
    @PutMapping("/updatestaaltest/{staalId}/{testId}")
    public void updateStaalTest(@PathVariable Long staalId, @PathVariable Long testId, @RequestBody StaalTest staalTest) {
        staalTestService.updateStaalTest(staalId, testId, staalTest);
    }

}
