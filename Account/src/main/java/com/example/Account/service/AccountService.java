package com.example.Account.service;

import com.example.Account.domain.AccountUser;
import com.example.Account.domain.cAccount;
import com.example.Account.dto.AccountDto;
import com.example.Account.exception.AccountException;
import com.example.Account.repository.AccountRepository;
import com.example.Account.repository.AccountUserRepository;
import com.example.Account.type.AccountStatus;
import com.example.Account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.Account.type.AccountStatus.IN_USE;


@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;
    /**
    * 사용자가 있는지 확인
    * 계좌의 번호를 생성하고
    * 계좌를 저장하고 , 정보를 넘긴다.
    **/
     @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
         AccountUser accountUser = accountUserRepository.findById(userId)
                 .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

        validateCreateAccount(accountUser);

        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber()))+1+"")
                .orElse("1000000000");
        cAccount account = accountRepository.save(
                cAccount.builder().accountUser(accountUser).accountStatus(IN_USE)
                        .accountNumber(newAccountNumber).balance(initialBalance).build()

        );

        return AccountDto.fromEntity(account);
    }
    private void validateCreateAccount(AccountUser accountUser){
        if(accountRepository.countByAccountUser(accountUser) == 10 ){
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
        }
    }
    @Transactional
    public cAccount getAccount(Long id) {
        if(id < 0){
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber){
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
        cAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser,account);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }
    private void validateDeleteAccount(AccountUser accountUser, cAccount account){
        if(!Objects.equals(accountUser.getId(),account.getAccountUser().getId())){
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        if(account.getAccountStatus() == AccountStatus.UNREGISTERED){
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        if(account.getBalance() >0){
            throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
        }
    }
    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(()-> new AccountException(ErrorCode.USER_NOT_FOUND));

        List<cAccount> accounts = accountRepository.findByAccountUser(accountUser);

        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }
}
