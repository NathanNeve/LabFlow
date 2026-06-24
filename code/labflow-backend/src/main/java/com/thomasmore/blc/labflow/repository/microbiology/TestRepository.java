package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.Test;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("microbiologyTestRepository")
public interface TestRepository extends JpaRepository<Test, Long> {

    Optional<Test> findByTestCode(String testCode);

    List<Test> findByStaalType_Id(Long staalTypeId);
}
