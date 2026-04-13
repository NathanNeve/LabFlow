package com.thomasmore.blc.labflow.repository.auth;

import com.thomasmore.blc.labflow.entity.auth.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    void delete(Rol rol);
}
