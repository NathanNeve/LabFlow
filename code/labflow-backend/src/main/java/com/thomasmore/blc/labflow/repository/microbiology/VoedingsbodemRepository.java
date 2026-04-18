package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("microbiologyVoedingsbodemRepository")
public interface VoedingsbodemRepository extends JpaRepository<Voedingsbodem, Long> {

    Optional<Voedingsbodem> findByNaam(String naam);
}
