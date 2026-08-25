package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.StaalType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("microbiologyStaalTypeRepository")
public interface StaalTypeRepository extends JpaRepository<StaalType, Long> {

    Optional<StaalType> findByNaam(String naam);

    List<StaalType> findAllByOrderByNaamAsc();
}
