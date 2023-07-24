package com.example.Account.dto;

import com.example.Account.type.TransactionResultType;
import com.example.Account.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryTransactionResponse {


    private String accountNumber;
    private TransactionResultType transactionResult;
    private TransactionType transactionType;
    private String transactionId;
    private Long amount;
    private LocalDateTime transactedAt;




    public static QueryTransactionResponse from(TransactionDto transactionDto) {
        return  QueryTransactionResponse.builder()
                .accountNumber(transactionDto.getAccountNumber())
                .transactionType(transactionDto.getTransactionType())
                .transactionResult(transactionDto.getTransactionResultType())
                .transactionId(transactionDto.getTransactionId())
                .amount(transactionDto.getAmount())
                .transactedAt(transactionDto.getTransactionAt())
                .build();
    }
}
