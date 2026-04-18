package com.thomasmore.blc.labflow.controller;

import com.thomasmore.blc.labflow.entity.auth.Rol;
import com.thomasmore.blc.labflow.service.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RolController {
    @Autowired
    private RolService rolService;

    // create
    @PostMapping("/createrol")
    public void create(@RequestBody Rol rol) {
        rolService.create(rol);
    }

    // read
    @GetMapping("/rollen")
    public List<Rol> read() {
        return rolService.read();
    }

    // update
    @PutMapping("/updaterol/{id}")
    public ResponseEntity<Rol> update(@PathVariable Long id, @RequestBody Rol rol) {
        return rolService.update(id, rol);
    }

    // delete
    @DeleteMapping("/deleterol/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return rolService.delete(id);
    }

}
