package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.StaalTestVoedingsbodemLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("microbiologyStaalTestVoedingsbodemLogRepository")
public interface StaalTestVoedingsbodemLogRepository extends JpaRepository<StaalTestVoedingsbodemLog, Long> {
}
