package com.ceylabs.fintrackerbackend.repository;

import com.ceylabs.fintrackerbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data Access Layer
 * */
@Repository
public interface UserRepository extends JpaRepository<User,Long> {

//    @Query("SELECT s FROM Student s WHERE s.email = ?1")
    Optional<User> findUserByEmail(String email);
}
