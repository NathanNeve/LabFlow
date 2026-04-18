package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.entity.auth.Rol;
import com.thomasmore.blc.labflow.repository.auth.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional("authTransactionManager")
public class RolService {
    @Autowired
    private RolRepository rolRepository;

    // create
    public void create(Rol rol) {
        rolRepository.save(rol);
    }

    // read
    public List<Rol> read() {
        return rolRepository.findAll();
    }

    // update
    public ResponseEntity<Rol> update(Long id, Rol rol) {
        Rol updateRol = rolRepository.findById(id).orElse(null);
        if (updateRol != null) {
            updateRol.setNaam(rol.getNaam());
            rolRepository.save(updateRol);
            return new ResponseEntity<>(updateRol, org.springframework.http.HttpStatus.OK);
        }
        return new ResponseEntity<>(org.springframework.http.HttpStatus.NOT_FOUND);
    }

    // delete
    public ResponseEntity<Integer> delete(Long id) {
        Rol deleteRol = rolRepository.findById(id).orElse(null);
        if (deleteRol != null) {
            rolRepository.delete(deleteRol);
            return new ResponseEntity<>(rolRepository.findAll().size(), org.springframework.http.HttpStatus.OK);
        }
        return new ResponseEntity<>(rolRepository.findAll().size(), org.springframework.http.HttpStatus.NOT_FOUND);
    }
}
