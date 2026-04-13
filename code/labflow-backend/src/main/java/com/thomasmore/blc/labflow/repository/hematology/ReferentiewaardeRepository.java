package com.thomasmore.blc.labflow.repository.hematology;

import com.thomasmore.blc.labflow.entity.hematology.Referentiewaarde;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferentiewaardeRepository extends JpaRepository<Referentiewaarde, Integer>  {
    public Referentiewaarde findById(Long id);
    public void delete(Referentiewaarde referentiewaarde);

    void removeReferentiewaardeById(Long id);


}
