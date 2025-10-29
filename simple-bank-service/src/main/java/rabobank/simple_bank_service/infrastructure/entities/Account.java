package rabobank.simple_bank_service.infrastructure.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "account")
public class Account {

    @Id
    private String id;

    private String accountDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    private BigDecimal balance;

    private String currency;

    @OneToOne(mappedBy = "account")
    private Card card;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Ledger> ledgerEntries;

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}
