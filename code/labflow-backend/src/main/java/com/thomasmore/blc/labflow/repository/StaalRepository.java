package com.thomasmore.blc.labflow.repository;

import com.thomasmore.blc.labflow.entity.Staal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaalRepository extends JpaRepository<Staal, Long>, JpaSpecificationExecutor<Staal> {

    public void delete(Staal staal);

    public List<Staal> findAllByOrderByStaalCodeDesc();

    // verkrijgen grootste staalcode voor het aanmaken van een nieuwe staal
    @Query("SELECT MAX(s.staalCode) FROM Staal s")
    String findLargestStaalCode();

    // verkrijg staal op basis van staalcode
    public Staal findByStaalCode(Long staalCode);

    // verkrijgen unieke statussen
    @Query("SELECT DISTINCT(s.status) FROM Staal s")
    List<Staal.Status> findDistinctStaalStatus();
}
