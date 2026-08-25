package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.StaalTestVoedingsbodem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("microbiologyStaalTestVoedingsbodemRepository")
public interface StaalTestVoedingsbodemRepository extends JpaRepository<StaalTestVoedingsbodem, Long> {
    @Query("select stvb from StaalTestVoedingsbodem stvb where stvb.staalTest.staal.id = :staalId")
    List<StaalTestVoedingsbodem> findByStaalId(@Param("staalId") Long staalId);

    @Modifying(clearAutomatically = true)
    @Query("delete from StaalTestVoedingsbodem stvb where stvb.staalTest.staal.id = :staalId")
    void deleteByStaalId(@Param("staalId") Long staalId);

    boolean existsByVoedingsbodem_Id(Long voedingsbodemId);
}
