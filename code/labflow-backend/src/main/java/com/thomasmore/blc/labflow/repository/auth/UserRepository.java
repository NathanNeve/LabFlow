package com.thomasmore.blc.labflow.repository.auth;

import com.thomasmore.blc.labflow.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    boolean existsByVoorNaamAndAchterNaam(String voorNaam, String achterNaam);
}
