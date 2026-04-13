package com.thomasmore.blc.labflow.repository.hematology;

import com.thomasmore.blc.labflow.entity.hematology.Testcategorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCategorieRepository extends JpaRepository<Testcategorie, Integer>  {
    public Testcategorie findById(Long id);

    public List<Testcategorie> findAllByNaamIsStartingWith(String naam);

    public void delete(Testcategorie testcategorie);
}
