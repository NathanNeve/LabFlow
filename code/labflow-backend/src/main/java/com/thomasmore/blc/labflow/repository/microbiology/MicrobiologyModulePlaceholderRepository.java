package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.MicrobiologyModulePlaceholder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MicrobiologyModulePlaceholderRepository extends JpaRepository<MicrobiologyModulePlaceholder, Long> {
}
