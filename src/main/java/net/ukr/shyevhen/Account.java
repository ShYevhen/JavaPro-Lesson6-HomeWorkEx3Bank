package net.ukr.shyevhen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import net.ukr.shyevhen.ExchangeRate.Currency;

@Entity
@Table(name = "Accounts")
@NamedQueries({ @NamedQuery(name = "Account.all", query = "SELECT a FROM Account a"),
		@NamedQuery(name = "Account.client", query = "SELECT a FROM Account a WHERE a.client.id = :clientId AND a.currency = :currency"),
		@NamedQuery(name = "Account.clientAcc", query = "SELECT a FROM Account a WHERE a.client.id = :clientId ") })
public class Account {
	@Id
	@GeneratedValue
	private long id;
	@Column(nullable = false)
	private BigDecimal money;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Currency currency;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id")
	private Client client;
	@OneToMany(mappedBy = "accountId", cascade = CascadeType.ALL)
	private List<AccTransaction> transactions = new ArrayList<>();

	public Account(BigDecimal money, Currency currency, Client client) {
		super();
		this.money = money;
		this.currency = currency;
		this.client = client;
		client.addAccounts(this);
	}

	public Account() {
		super();
	}

	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
		this.client.addAccounts(this);
	}

	public long getId() {
		return id;
	}

	public List<AccTransaction> getTransactions() {
		return transactions;
	}

	public void addTransaction(AccTransaction tr) {
		if (!transactions.contains(tr)) {
			transactions.add(tr);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Account [id=" + id + ", money=" + money + ", currency=" + currency + ", " + client + "]";
	}

}
