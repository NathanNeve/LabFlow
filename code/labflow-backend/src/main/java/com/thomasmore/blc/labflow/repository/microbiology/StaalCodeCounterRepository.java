package com.thomasmore.blc.labflow.repository.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.StaalCodeCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("microbiologyStaalCodeCounterRepository")
public interface StaalCodeCounterRepository extends JpaRepository<StaalCodeCounter, Integer> {

    /**
     * Atomically increments (or initializes) the counter for the given year and returns the new value.
     *
     * Requires SQLite 3.35+ for RETURNING support.
     */
    @Transactional
    @Query(
            value = """
                    INSERT INTO staal_code_counter(year, last_number)
                    VALUES (?1, 1)
                    ON CONFLICT(year)
                    DO UPDATE SET last_number = last_number + 1
                    RETURNING last_number
                    """,
            nativeQuery = true
    )
    Long incrementAndGet(Integer year);
}

