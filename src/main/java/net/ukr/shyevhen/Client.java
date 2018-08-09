package net.ukr.shyevhen;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "Clients")
@NamedQuery(name = "Client.all", query = "SELECT c FROM Client c")

public class Client {
	@Id
	@GeneratedValue
	private long id;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String surname;
	@OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
	private List<Account> accounts = new ArrayList<>();
	@OneToMany(mappedBy = "clientId", cascade = CascadeType.ALL)
	private List<Transaction> transactions = new ArrayList<>();

	public Client(String name, String surname) {
		super();
		this.name = name;
		this.surname = surname;
	}

	public Client() {
		super();
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public void addAccounts(Account acc) {
		if (!accounts.contains(acc)) {
			accounts.add(acc);
		}
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void addTransaction(Transaction tr) {
		if (!transactions.contains(tr)) {
			transactions.add(tr);
		}
	}

	@Override
	public String toString() {
		return "Client [id=" + id + ", name=" + name + ", surname=" + surname + "]";
	}

}
