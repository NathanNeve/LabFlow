package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.Staal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Staal extends JpaRepository<Staal, Long> {
}
