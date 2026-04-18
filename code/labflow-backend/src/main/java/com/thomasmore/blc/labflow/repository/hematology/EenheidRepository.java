package com.thomasmore.blc.labflow.repository.hematology;

import com.thomasmore.blc.labflow.entity.hematology.Eenheid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EenheidRepository extends JpaRepository<Eenheid, Integer> {
    public Eenheid findById(Long id);

    public List<Eenheid> findAllByOrderByNaam();
}
