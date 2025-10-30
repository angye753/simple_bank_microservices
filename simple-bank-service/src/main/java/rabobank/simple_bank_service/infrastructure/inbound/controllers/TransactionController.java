package rabobank.simple_bank_service.infrastructure.inbound.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rabobank.simple_bank_service.domain.model.TransferRequest;
import rabobank.simple_bank_service.domain.model.WithdrawRequestDTO;
import rabobank.simple_bank_service.application.in.TransactionPort;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "API for moving money between accounts")
public class TransactionController {

    private final TransactionPort transactionPort;

    @PostMapping("/transfer")
    @Operation(summary = "Create a new transfer request", description = "Initiates a transfer transaction.")
    @ApiResponse(responseCode = "200", description = "Transfer request processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient funds")
    public ResponseEntity<?> createTransfer(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
                                            @RequestBody TransferRequest req) throws Exception {
        var txId = transactionPort.transfer(
                idempotencyKey,
                req.getFromAccountId(),
                req.getToAccountId(),
                req.getAmount()
        );
        return ResponseEntity.accepted().body(java.util.Map.of("transactionId", txId));
    }


    @PostMapping("/withdraw")
    @Operation(summary = "Create a withdraw request", description = "Initiates a withdraw transaction.")
    @ApiResponse(responseCode = "200", description = "Withdraw request processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient funds")
    public ResponseEntity<?> withdraw(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
                                            @RequestBody WithdrawRequestDTO req) throws Exception {
        var txId = transactionPort.withdraw(idempotencyKey, req.getAccountId(), req.getAmount());
        return ResponseEntity.accepted().body(java.util.Map.of("transactionId", txId));
    }
}
