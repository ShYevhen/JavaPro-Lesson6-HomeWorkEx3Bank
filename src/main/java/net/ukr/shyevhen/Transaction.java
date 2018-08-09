package net.ukr.shyevhen;

import javax.persistence.*;

@Entity
@Table(name = "Transactions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transaction_type", discriminatorType = DiscriminatorType.STRING)
@NamedQueries({ @NamedQuery(name = "Transaction.all", query = "SELECT t FROM Transaction t"),
		@NamedQuery(name = "Transaction.client", query = "SELECT t FROM Transaction t WHERE t.clientId.id = :clientId") })
public class Transaction {
	@Id
	@GeneratedValue
	private long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id")
	private Client clientId;

	public Transaction(Client clientId) {
		super();
		this.clientId = clientId;
		clientId.addTransaction(this);
	}

	public Transaction() {
		super();
	}

	public long getId() {
		return id;
	}

	public Client getClientId() {
		return clientId;
	}

	public void setClient(Client clientId) {
		this.clientId = clientId;
		clientId.addTransaction(this);
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
		Transaction other = (Transaction) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Transaction [id=" + id + ", " + clientId + "]";
	}

}
