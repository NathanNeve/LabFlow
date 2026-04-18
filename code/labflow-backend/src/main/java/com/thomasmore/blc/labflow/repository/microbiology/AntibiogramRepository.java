package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.Antibiogram;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("microbiologyAntibiogramRepository")
public interface AntibiogramRepository extends JpaRepository<Antibiogram, Long> {
}
