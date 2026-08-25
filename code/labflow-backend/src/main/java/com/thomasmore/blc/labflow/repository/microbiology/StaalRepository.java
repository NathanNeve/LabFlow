package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.Staal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository("microbiologyStaalRepository")
public interface StaalRepository extends JpaRepository<Staal, Long>, JpaSpecificationExecutor<Staal> {

    Optional<Staal> findByStaalCode(Long staalCode);

    boolean existsByStaalCodeAndIdNot(Long staalCode, Long id);

    boolean existsByStaalType_Id(Long staalTypeId);
}
