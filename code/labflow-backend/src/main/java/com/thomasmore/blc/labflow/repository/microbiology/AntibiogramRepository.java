package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.Antibiogram;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("microbiologyAntibiogramRepository")
public interface AntibiogramRepository extends JpaRepository<Antibiogram, Long> {

    List<Antibiogram> findByStaal_Id(Long staalId);

    Optional<Antibiogram> findByStaal_IdAndAntibiotica_Id(Long staalId, Long antibioticaId);

    void deleteByStaal_IdAndAntibiotica_Id(Long staalId, Long antibioticaId);

    boolean existsByAntibiotica_Id(Long antibioticaId);
}
