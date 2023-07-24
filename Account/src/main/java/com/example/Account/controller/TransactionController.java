package com.example.Account.controller;


import com.example.Account.dto.CancelBalance;
import com.example.Account.dto.QueryTransactionResponse;
import com.example.Account.dto.TransactionDto;
import com.example.Account.dto.UseBalance;
import com.example.Account.exception.AccountException;
import com.example.Account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TransactionController {
    private final TransactionService transactionService;
    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request
    ){
        TransactionDto transactionDto = transactionService.useBalance(request.getUserId(),
        request.getAccountNumber(),request.getAmount());

        try{
            return UseBalance.Response.from(transactionDto);
        }
        catch (AccountException e){
            log.error("Failed to use balance ");

            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }

    }

    @PostMapping("/transaction/cancel")
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request request
    ){

        try{
            return CancelBalance.Response.from(transactionService.cancelBalance(
                    request.getTransactionId(),
                    request.getAccountNumber(),request.getAmount()));
        }
        catch (AccountException e){
            log.error("Failed to use balance ");

            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }

    }
    @GetMapping("transaction/{transactionId}")
    public QueryTransactionResponse queryTransactionResponse(
            @PathVariable String transactionId
    ){
        return QueryTransactionResponse.from(transactionService.queryTransaction(transactionId));
    }
}
