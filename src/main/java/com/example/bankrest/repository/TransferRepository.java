// src/main/java/com/example/bankrest/repository/TransferRepository.java

package com.example.bankrest.repository;

import com.example.bankrest.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для операций с сущностью Transfer (таблица transfers).
 */
@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

}
