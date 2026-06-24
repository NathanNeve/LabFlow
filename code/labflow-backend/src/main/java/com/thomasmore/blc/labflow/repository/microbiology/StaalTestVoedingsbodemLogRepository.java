package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.StaalTestVoedingsbodemLog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("microbiologyStaalTestVoedingsbodemLogRepository")
public interface StaalTestVoedingsbodemLogRepository extends JpaRepository<StaalTestVoedingsbodemLog, Long> {

    List<StaalTestVoedingsbodemLog> findByStaalTestVoedingsbodem_IdOrderByIdAsc(Long linkId);

    @Modifying(clearAutomatically = true)
    @Query("delete from StaalTestVoedingsbodemLog l where l.staalTestVoedingsbodem.id = :linkId")
    void deleteByStaalTestVoedingsbodem_Id(@Param("linkId") Long linkId);
}
