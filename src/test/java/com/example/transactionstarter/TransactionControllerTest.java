package com.example.transactionstarter;

import com.example.transactionstarter.transaction.entity.Transaction;
import com.example.transactionstarter.transaction.entity.TransactionStatus;
import com.example.transactionstarter.transaction.entity.TransactionType;
import com.example.transactionstarter.transaction.entity.Currency;
import com.example.transactionstarter.transaction.repository.TransactionRepository;
import com.example.transactionstarter.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionControllerTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    void shouldCreateTransactionSuccessfully() {
        Transaction transaction = new Transaction(
                "TXN001",
                "CUST001",
                new BigDecimal("500.00"),
                Currency.INR,
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        Transaction saved = transactionService.createTransaction(transaction);

        assertNotNull(saved);
        assertEquals("TXN001", saved.getTransactionId());
        assertEquals("CUST001", saved.getCustomerId());
        assertEquals(new BigDecimal("500.00"), saved.getAmount());
        assertEquals(TransactionStatus.PENDING, saved.getStatus());
    }

    @Test
    void shouldRejectDuplicateTransactionId() {
        Transaction first = new Transaction(
                "TXN001",
                "CUST001",
                new BigDecimal("500.00"),
                Currency.INR,
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        transactionService.createTransaction(first);

        Transaction duplicate = new Transaction(
                "TXN001",
                "CUST002",
                new BigDecimal("800.00"),
                Currency.INR,
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        assertThrows(
                RuntimeException.class,
                () -> transactionService.createTransaction(duplicate)
        );
    }

    @Test
    void shouldGetExistingTransaction() {
        Transaction transaction = new Transaction(
                "TXN002",
                "CUST001",
                new BigDecimal("1000.00"),
                Currency.INR,
                TransactionType.TRANSFER,
                TransactionStatus.PENDING
        );

        transactionService.createTransaction(transaction);

        Transaction result = transactionService
                .getTransaction("TXN002")
                .orElse(null);

        assertNotNull(result);
        assertEquals("TXN002", result.getTransactionId());
    }

    @Test
    void shouldReturnEmptyForNonExistingTransaction() {
        assertTrue(
                transactionService.getTransaction("DOES-NOT-EXIST").isEmpty()
        );
    }

    @Test
    void shouldAllowPendingToCompleted() {
        Transaction transaction = new Transaction(
                "TXN003",
                "CUST001",
                new BigDecimal("250.00"),
                Currency.INR,
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        transactionService.createTransaction(transaction);

        Transaction updated = transactionService
                .updateStatus("TXN003", "COMPLETED");

        assertEquals(TransactionStatus.COMPLETED, updated.getStatus());
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        Transaction transaction = new Transaction(
                "TXN004",
                "CUST001",
                new BigDecimal("250.00"),
                Currency.INR,
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        transactionService.createTransaction(transaction);
        transactionService.updateStatus("TXN004", "COMPLETED");

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.updateStatus("TXN004", "FAILED")
        );
    }

    @Test
    void shouldFindTransactionsByCustomerId() {
        Transaction transaction = new Transaction(
                "TXN005",
                "CUST100",
                new BigDecimal("750.00"),
                Currency.INR,
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        transactionService.createTransaction(transaction);

        assertEquals(
                1,
                transactionService.getCustomerTransactions("CUST100").size()
        );
    }
}