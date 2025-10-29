package rabobank.simple_bank_service.infrastructure.inbound.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rabobank.simple_bank_service.domain.model.TransferRequest;
import rabobank.simple_bank_service.domain.model.WithdrawRequestDTO;
import rabobank.simple_bank_service.application.in.TransactionPort;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionPort transactionPort;

    @PostMapping("/transfer")
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
    public ResponseEntity<?> withdraw(@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
                                            @RequestBody WithdrawRequestDTO req) throws Exception {
        var txId = transactionPort.withdraw(idempotencyKey, req.getAccountId(), req.getAmount());
        return ResponseEntity.accepted().body(java.util.Map.of("transactionId", txId));
    }
}
