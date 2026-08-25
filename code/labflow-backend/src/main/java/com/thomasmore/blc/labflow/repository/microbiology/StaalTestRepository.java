package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.StaalTest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("microbiologyStaalTestRepository")
public interface StaalTestRepository extends JpaRepository<StaalTest, Long> {
    @Query("select st from StaalTest st where st.staal.id = :staalId")
    List<StaalTest> findByStaalId(@Param("staalId") Long staalId);

    @Query("select st.test.id from StaalTest st where st.staal.id = :staalId")
    List<Long> findTestIdsByStaalId(@Param("staalId") Long staalId);

    @Modifying(clearAutomatically = true)
    @Query("delete from StaalTest st where st.staal.id = :staalId")
    void deleteByStaalId(@Param("staalId") Long staalId);

    boolean existsByTest_Id(Long testId);
}
