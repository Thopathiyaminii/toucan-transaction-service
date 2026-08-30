# Customer Transaction Service

## 1. Problem Understanding

This project implements a small transaction-processing REST service using Java and Spring Boot.

The service supports four operations:

1. Create a transaction
2. Get a transaction by Transaction ID
3. Update the status of a transaction
4. Get all transactions for a Customer ID

Transactions are stored using Spring Data JPA with an embedded H2 database.

## 2. Assumptions

No candidate-specific variant was provided in my invitation at the time of implementation.

Therefore, the following rules are my implementation assumptions:

- Supported currencies: INR, USD, EUR
- Supported transaction types: PAYMENT, REFUND, TRANSFER
- Maximum transaction amount: 100000.00
- New transactions always start with PENDING status

If a candidate-specific variant is provided, these rules can be adjusted accordingly.

## 3. Validation Rules

The API validates the following fields when creating a transaction:

- Transaction ID is required and cannot be blank.
- Customer ID is required and cannot be blank.
- Amount is required and must be between 0.01 and 100000.00.
- Currency is required and must be one of the supported currencies.
- Transaction type is required and must be one of the supported types.
- Transaction ID must be unique.
- Initial status is controlled by the application and is set to PENDING.

Invalid requests return a 400 Bad Request response.

## 4. Status Transition Rules

The following status transitions are allowed:

- PENDING -> COMPLETED
- PENDING -> FAILED
- COMPLETED -> REFUNDED

FAILED and REFUNDED are treated as final states.

Other status transitions are rejected because they could result in an inconsistent transaction lifecycle.

## 5. API Endpoints

### Create Transaction

`POST /api/transactions`

Example request:

```json
{
  "transactionId": "TXN001",
  "customerId": "CUST001",
  "amount": 500.00,
  "currency": "INR",
  "transactionType": "PAYMENT"
}