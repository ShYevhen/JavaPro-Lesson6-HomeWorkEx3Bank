package net.ukr.shyevhen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "ExchangeRates")
@NamedQueries({ @NamedQuery(name = "Exchange.all", query = "SELECT e FROM ExchangeRate e"),
		@NamedQuery(name = "Exchange.get", query = "SELECT e FROM ExchangeRate e WHERE e.fromCur = :from AND e.toCur = :to") })

public class ExchangeRate {
	@Id
	@GeneratedValue
	private long id;
	@Column(name = "from_currency", nullable = false)
	private Currency fromCur;
	@Column(name = "to_currency", nullable = false)
	private Currency toCur;
	@Column(nullable = false)
	private BigDecimal exchange;
	@OneToMany(mappedBy = "exchangeR", cascade = CascadeType.ALL)
	private List<ExchTransaction> transactions = new ArrayList<>();

	public ExchangeRate(Currency fromCur, Currency toCur, BigDecimal exchange) {
		super();
		this.fromCur = fromCur;
		this.toCur = toCur;
		this.exchange = exchange;
	}

	public ExchangeRate() {
		super();
	}

	public Currency getFromCur() {
		return fromCur;
	}

	public void setFromCur(Currency fromCur) {
		this.fromCur = fromCur;
	}

	public Currency getToCur() {
		return toCur;
	}

	public void setToCur(Currency toCur) {
		this.toCur = toCur;
	}

	public BigDecimal getExchange() {
		return exchange;
	}

	public void setExchange(BigDecimal exchange) {
		this.exchange = exchange;
	}

	public long getId() {
		return id;
	}

	public List<ExchTransaction> getTransactions() {
		return transactions;
	}

	public void addTransaction(ExchTransaction tr) {
		if (!transactions.contains(tr)) {
			transactions.add(tr);
		}
	}

	@Override
	public String toString() {
		return "ExchangeRate [id=" + id + ", fromCur=" + fromCur + ", toCur=" + toCur + ", exchange=" + exchange + "]";
	}

	public enum Currency {
		UAH, EUR, USD
	}
}
