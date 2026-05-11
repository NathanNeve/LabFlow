package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.TestVoedingsbodem;
import com.thomasmore.blc.labflow.entity.microbiology.TestVoedingsbodemId;
import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("microbiologyTestVoedingsbodemRepository")
public interface TestVoedingsbodemRepository extends JpaRepository<TestVoedingsbodem, TestVoedingsbodemId> {

    boolean existsByTest_IdAndVoedingsbodem_Id(Long testId, Long voedingsbodemId);

    @Query("""
            select tv.voedingsbodem.naam
            from TestVoedingsbodem tv
            where tv.test.id = :testId
            order by tv.voedingsbodem.naam
            """)
    List<String> findVoedingsbodemNamesByTestId(@Param("testId") Long testId);

    @Query("""
            select distinct tv.voedingsbodem
            from TestVoedingsbodem tv
            where tv.test.id in :testIds
            """)
    List<Voedingsbodem> findDistinctVoedingsbodemsByTestIds(@Param("testIds") List<Long> testIds);
}
