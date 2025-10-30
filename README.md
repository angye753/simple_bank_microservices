# 🏦 Simple Bank Service

This project demonstrates a **microservices-based banking system** composed of three independent Spring Boot services.  
It showcases **secure authentication**, **event-driven communication using Kafka**, and **data replication between services** for scalability and resilience.

---

## 🚀 Overview

The system is designed around three microservices that together provide core banking functionalities:

- 💰 **Account Service** – Manages account balances and operations
- 🔁 **Ledger Service** – Maintains a transaction log for auditing and reporting
- 🔐 **Auth Service** – Handles authentication using JWT

---

## 🧾 Features

- **Check available balance** (Admin only): I understood per Balance, the current money available in each account.
But more information can be added thanks to the Ledger table in case more information is needed
- **Withdraw money** from an account
- **Transfer funds** between accounts in the same bank
- **Authentication & Authorization** with JWT (symmetric signing, open to asymmetric key upgrade)
- **Kafka-based data replication** for eventual consistency between services
- **Auditing via Ledger table**, enabling transaction tracing and reporting
- **Containerized environment** using Docker Compose

---

## 🧱 Architecture

- **Event-driven communication**:  
  The *write service* publishes events to Kafka topics, while the *read service* consumes them to update its own tables. 
  For a production environment it´s better to have a different database
  This design allows for independent scaling of read and write operations.

- **Database setup**:  
  Currently, a single PostgreSQL instance is used for simplicity, but the architecture supports separate databases per service for true microservice isolation.

- **Security**:  
  JWT tokens are issued and verified using **symmetric signing (HS256)**.  
  Future versions should support **asymmetric signing (RS256)** for enhanced security and scalability.

---
## How to run locally in the root folder run command: 

`Create a .env file with these variables:
POSTGRES_DB=customdb
POSTGRES_USER=customUser
POSTGRES_PASSWORD=customPassword
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/customdb
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=customPassword
JWT_SECRET="jwt secret"
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092`

After update your custom values, you can run 
`docker compose up --build
`

## Deployment -> CI/CD
Pending to implement, in order to automate deployment process.
Currently is being done manually. Available in a custom public server.
