<<<<<<<< HEAD:src/main/java/com/cashai/cashaibackend/repository/UserRepository.java
package com.cashai.cashaibackend.repository;

import com.cashai.cashaibackend.model.User;
========
package com.ceylabs.fintrackerbackend.repository;

import com.ceylabs.fintrackerbackend.model.User;
>>>>>>>> 46a1152 (Refactor User components):src/main/java/com/ceylabs/fintrackerbackend/repository/UserRepository.java
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
