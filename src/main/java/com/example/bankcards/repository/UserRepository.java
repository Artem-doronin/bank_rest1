package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User > findByUsername(String username);

    @Query("select u from User u join fetch u.roles where u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

}
