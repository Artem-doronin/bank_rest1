package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Boolean existsByCardNumber(String cardNumber);

    List<Card> findByOwner(User user);

    @Query("SELECT c FROM Card c WHERE owner.id = :userId " +
            "AND (:cardNumber IS NULL OR c.cardNumber LIKE %:cardNumber%)")
    Page<Card> findByUserIdAndCardNumber(@Param("userId") Long userId,
                                         @Param("cardNumber") String cardNumber,
                                         Pageable pageable);

}
