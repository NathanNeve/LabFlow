package com.thomasmore.blc.labflow.repository.hematology;

import com.thomasmore.blc.labflow.entity.hematology.StaalTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaalTestRepository extends JpaRepository<StaalTest, Integer> {

    // I want to get a 'staalTest' based on the staalid and testid
    StaalTest findByStaal_IdAndTest_Id(Long staalId, Long testId);
}
