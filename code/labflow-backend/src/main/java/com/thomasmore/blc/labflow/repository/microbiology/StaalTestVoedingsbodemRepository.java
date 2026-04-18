package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.StaalTestVoedingsbodem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("microbiologyStaalTestVoedingsbodemRepository")
public interface StaalTestVoedingsbodemRepository extends JpaRepository<StaalTestVoedingsbodem, Long> {
}
