package com.example.Account.service;


import com.example.Account.domain.AccountUser;
import com.example.Account.domain.Transaction;
import com.example.Account.domain.cAccount;
import com.example.Account.dto.TransactionDto;
import com.example.Account.exception.AccountException;
import com.example.Account.repository.AccountRepository;
import com.example.Account.repository.AccountUserRepository;
import com.example.Account.repository.TransactionRepository;
import com.example.Account.type.AccountStatus;
import com.example.Account.type.ErrorCode;
import com.example.Account.type.TransactionResultType;
import com.example.Account.type.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.Account.type.TransactionResultType.F;
import static com.example.Account.type.TransactionResultType.S;
import static com.example.Account.type.TransactionType.CANCEL;
import static com.example.Account.type.TransactionType.USE;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount){
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(()-> new AccountException(ErrorCode.USER_NOT_FOUND));

        cAccount account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(()->new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));


        validateUseBalance(user,account,amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(saveAndGetTransaction(USE,S,account,amount));
    }

    private void validateUseBalance(AccountUser user, cAccount account, Long amount){
        if(!Objects.equals(user.getId(),account.getAccountUser().getId())){

            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        if(account.getAccountStatus() != AccountStatus.IN_USE){
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        if(account.getBalance() <amount){
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }



    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        cAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
        saveAndGetTransaction(USE,F,account, amount);
    }

    private Transaction saveAndGetTransaction(
            TransactionType transactionType,
            TransactionResultType transactionResultType,
            cAccount account, Long amount) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactionAt(LocalDateTime.now())
                        .build());
    }

    @Transactional
    public TransactionDto cancelBalance(
            String transactionId,
            String accountNumber,Long amount){
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(()->new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));
        cAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateCancelBalance(transaction, account,amount);

        account.cancelBalance(amount);

        return TransactionDto.fromEntity(
                saveAndGetTransaction(CANCEL,S,account,amount)
        );

    }

    private static void validateCancelBalance( Transaction transaction, cAccount account,Long amount) {
        if(!Objects.equals(transaction.getAccount().getId(),account.getId())){
            throw new AccountException(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH);
        }
        if(!Objects.equals(transaction.getAmount(), amount)){
            throw new AccountException(ErrorCode.CANCEL_MUST_FULLY);
        }
        if(transaction.getTransactionAt().isBefore(LocalDateTime.now().minusYears(1))){
            throw new AccountException(ErrorCode.TOO_OLD_ORDER_TO_CANCEL);
        }
    }

    @Transactional
    public void saveFailedCancelTransaction(String accountNumber, Long amount){
        cAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
        saveAndGetTransaction(CANCEL,F,account, amount);
    }

    public TransactionDto queryTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(()->new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));
        return TransactionDto.fromEntity(transaction);
    }
}
