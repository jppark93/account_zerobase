package com.example.Account.repository;

import com.example.Account.domain.AccountUser;
import com.example.Account.domain.Transaction;
import com.example.Account.domain.cAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);

}
