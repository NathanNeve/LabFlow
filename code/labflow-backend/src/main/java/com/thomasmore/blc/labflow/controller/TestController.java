package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.config.UniqueConstraintViolationException;
import com.thomasmore.blc.labflow.entity.hematology.Test;
import com.thomasmore.blc.labflow.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TestController {
    @Autowired
    private TestService testService;

    // create
    @PostMapping("/createtest")
    public void create(@RequestBody Test test) {
        testService.create(test);
    }

    // read
    @GetMapping("/tests")
    public List<Test> read() {
        return testService.read();
    }

    // update
    @PutMapping("/updatetest/{id}")
    public ResponseEntity<Test> update(@PathVariable Long id, @RequestBody Test test) {
        try {
            return testService.update(id, test);
        } catch (Exception e) {
            throw new UniqueConstraintViolationException(e.getMessage());
        }
    }

    // delete
    @DeleteMapping("/deletetest/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return testService.delete(id);
    }
}