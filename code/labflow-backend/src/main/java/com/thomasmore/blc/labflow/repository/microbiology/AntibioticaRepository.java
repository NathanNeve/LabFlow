package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.Antibiotica;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("microbiologyAntibioticaRepository")
public interface AntibioticaRepository extends JpaRepository<Antibiotica, Long> {

    Optional<Antibiotica> findByNaam(String naam);
}
