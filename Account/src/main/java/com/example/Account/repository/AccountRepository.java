package com.example.Account.repository;

import com.example.Account.domain.AccountUser;
import com.example.Account.domain.cAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<cAccount, Long> {

    Optional<cAccount> findFirstByOrderByIdDesc();

    Integer countByAccountUser(AccountUser accountUser);

    Optional<cAccount> findByAccountNumber(String AccountNumber);

    List<cAccount> findByAccountUser(AccountUser accountUser);
}
