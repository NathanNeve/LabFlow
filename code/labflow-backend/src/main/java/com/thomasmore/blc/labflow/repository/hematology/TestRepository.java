package com.thomasmore.blc.labflow.repository.hematology;

import com.thomasmore.blc.labflow.entity.hematology.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Integer> {
    public Test findById(Long id);

    public void delete(Test test);

    public Test findByTestCode(String testCode);

    public List<Test> findAllByOrderByTestCodeAsc();

    // sorteer eerst X (notitie toevoegen) en dan nummers oplopend
    @Query("SELECT t FROM Test t ORDER BY " +
            "CASE WHEN t.testCode LIKE 'X%' THEN 0 ELSE 1 END ASC, " +
            "CAST(SUBSTRING(t.testCode, CASE WHEN t.testCode LIKE 'X%' THEN 2 ELSE 1 END) AS INTEGER) ASC")
    public List<Test> findAllSortedByTestCode();

    // voor geen testcategorieën te kunnen verwijderen die gelinked zijn aan een test
    public boolean existsByTestcategorieId(Long testcategorieId);

    // voor geen eenheden te kunnen verwijderen die gelinked zijn aan een test
    public boolean existsByEenheidId(Long eenheidId);
}
