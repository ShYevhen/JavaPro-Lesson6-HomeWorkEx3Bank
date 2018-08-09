package net.ukr.shyevhen;

import java.math.BigDecimal;
import javax.persistence.*;

@Entity
@Table(name = "Transactions")
@DiscriminatorValue(value = "Exchange")
@NamedQuery(name = "Transaction.exch", query = "SELECT t FROM ExchTransaction t WHERE t.clientId.id = :clientId")
public class ExchTransaction extends Transaction {
	private BigDecimal exMoney;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exchange_rate")
	private ExchangeRate exchangeR;
	private BigDecimal money;

	public ExchTransaction(Client client, BigDecimal exMoney, ExchangeRate exchangeR) {
		super(client);
		this.exMoney = exMoney;
		this.exchangeR = exchangeR;
		this.money = exMoney.multiply(exchangeR.getExchange());
		this.exchangeR.addTransaction(this);
	}

	public ExchTransaction(Client client) {
		super(client);
	}

	public ExchTransaction() {
		super();
	}

	public BigDecimal getExMoney() {
		return exMoney;
	}

	public void setExMoney(BigDecimal exMoney) {
		this.exMoney = exMoney;
		if (exchangeR != null) {
			money = exMoney.multiply(exchangeR.getExchange());
		}
	}

	public ExchangeRate getExchange() {
		return exchangeR;
	}

	public void setExchange(ExchangeRate exchangeR) {
		this.exchangeR = exchangeR;
		if (exMoney != null) {
			money = exMoney.multiply(exchangeR.getExchange());
		}
		this.exchangeR.addTransaction(this);
	}

	public BigDecimal getMoney() {
		return money;
	}

	@Override
	public String toString() {
		return "ExchTansaction [exMoney=" + exMoney + ", money=" + money + ", " + exchangeR + "]";
	}

}
