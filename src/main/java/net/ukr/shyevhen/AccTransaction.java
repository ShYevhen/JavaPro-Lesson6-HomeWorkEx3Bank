package net.ukr.shyevhen;

import java.math.BigDecimal;

import javax.persistence.*;

@Entity
@Table(name = "Transactions")
@DiscriminatorValue(value = "Account")
@NamedQuery(name = "Transaction.acc", query = "SELECT t FROM AccTransaction t WHERE t.clientId.id = :clientId")
public class AccTransaction extends Transaction {
	@Column(nullable = false)
	private BigDecimal money;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private Account accountId;
	@Column(name = "add_or_take")
	private String addOrTake;

	public AccTransaction(Client client, BigDecimal money, Account accountId, boolean addOrTake) {
		super(client);
		this.money = money;
		this.accountId = accountId;
		this.addOrTake = addOrTake == true ? "add" : "take";
		accountId.addTransaction(this);
	}

	public AccTransaction(BigDecimal money, Account accountId, boolean addOrTake) {
		super();
		this.money = money;
		this.accountId = accountId;
		this.addOrTake = addOrTake == true ? "add" : "take";
		accountId.addTransaction(this);
	}

	public AccTransaction(Client client) {
		super(client);
	}

	public AccTransaction() {
		super();
	}

	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}

	public Account getAccountId() {
		return accountId;
	}

	public void setAccount(Account accountId) {
		this.accountId = accountId;
		accountId.addTransaction(this);
	}

	public boolean isAddOrTake() {
		return addOrTake.equals("add") ? true : false;
	}

	public void setAddOrTake(boolean addOrTake) {
		this.addOrTake = addOrTake == true ? "add" : "take";
	}

	@Override
	public String toString() {
		return "AccTransaction [money=" + money + ", addOrTake=" + addOrTake + ", " + accountId + "]";
	}

}
