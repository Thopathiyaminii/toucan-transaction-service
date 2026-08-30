package com.example.transactionstarter.transaction.service;

import com.example.transactionstarter.transaction.entity.Transaction;
import com.example.transactionstarter.transaction.entity.TransactionStatus;
import com.example.transactionstarter.transaction.exception.DuplicateTransactionException;
import com.example.transactionstarter.transaction.exception.TransactionNotFoundException;
import com.example.transactionstarter.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(Transaction transaction) {
        if (transactionRepository.existsById(transaction.getTransactionId())) {
            throw new DuplicateTransactionException(
                    "Transaction ID already exists: " + transaction.getTransactionId());
        }

        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> getTransaction(String transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public List<Transaction> getCustomerTransactions(String customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }

    public Transaction updateStatus(String transactionId, String newStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction not found: " + transactionId));

        TransactionStatus currentStatus = transaction.getStatus();
        TransactionStatus requestedStatus;

        try {
            requestedStatus = TransactionStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid transaction status: " + newStatus);
        }

        if (!isValidStatusTransition(currentStatus, requestedStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from "
                            + currentStatus + " to " + requestedStatus);
        }

        transaction.setStatus(requestedStatus);

        return transactionRepository.save(transaction);
    }

    private boolean isValidStatusTransition(
            TransactionStatus currentStatus,
            TransactionStatus requestedStatus) {

        return switch (currentStatus) {
            case PENDING ->
                    requestedStatus == TransactionStatus.COMPLETED
                            || requestedStatus == TransactionStatus.FAILED;

            case COMPLETED ->
                    requestedStatus == TransactionStatus.REFUNDED;

            case FAILED, REFUNDED -> false;
        };
    }
}