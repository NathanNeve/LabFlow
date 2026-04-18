package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.TestVoedingsbodem;
import com.thomasmore.blc.labflow.entity.microbiology.TestVoedingsbodemId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("microbiologyTestVoedingsbodemRepository")
public interface TestVoedingsbodemRepository extends JpaRepository<TestVoedingsbodem, TestVoedingsbodemId> {
}
