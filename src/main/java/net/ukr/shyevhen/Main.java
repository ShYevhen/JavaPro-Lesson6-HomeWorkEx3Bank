package net.ukr.shyevhen;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import net.ukr.shyevhen.ExchangeRate.Currency;

public class Main {
	private static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPABank");
		EntityManager em = emf.createEntityManager();
		startConf(em);
		try {
			while (true) {
				try {
					System.out.print("Main menu\r\n1: Clients\r\n2: Transaction\r\n");
					System.out.print("3: Accounts\r\n4: Exchange rates\r\nexit: Enter\r\n->");
					String choose = sc.nextLine();
					if (choose.equals("1")) {
						clientsMenu(em);
					} else if (choose.equals("2")) {
						transactionMenu(em);
					} else if (choose.equals("3")) {
						accountMenu(em);
					} else if (choose.equals("4")) {
						exchangeMenu(em);
					} else {
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} finally {
			em.close();
			emf.close();
		}
	}

	private static void startConf(EntityManager em) {
		em.getTransaction().begin();
		try {
			Client c = new Client("Stiven", "Smiter");
			em.persist(c);
			ExchangeRate exRate = new ExchangeRate(Currency.UAH, Currency.USD, new BigDecimal("0.5"));
			em.persist(exRate);
			exRate = new ExchangeRate(Currency.UAH, Currency.EUR, new BigDecimal("0.25"));
			em.persist(exRate);
			exRate = new ExchangeRate(Currency.EUR, Currency.USD, new BigDecimal("2"));
			em.persist(exRate);
			exRate = new ExchangeRate(Currency.EUR, Currency.UAH, new BigDecimal("4"));
			em.persist(exRate);
			exRate = new ExchangeRate(Currency.USD, Currency.EUR, new BigDecimal("0.5"));
			em.persist(exRate);
			exRate = new ExchangeRate(Currency.USD, Currency.UAH, new BigDecimal("2"));
			em.persist(exRate);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			System.err.println(e);
		}
	}

	private static void clientsMenu(EntityManager em) {
		while (true) {
			try {
				System.out.println("Client menu");
				System.out.print(
						"1: Clients list\r\n2: Add client\r\n3: Change client\r\n4: Work with client accounts\r\nexit: Enter\r\n->");
				String choose = sc.nextLine();
				if (choose.equals("1")) {
					List<Client> clientList = em.createNamedQuery("Client.all", Client.class).getResultList();
					for (Client client : clientList) {
						System.out.println(client);
					}
				} else if (choose.equals("2")) {
					addClient(em);
				} else if (choose.equals("3")) {
					changeClient(em);
				} else if (choose.equals("4")) {
					clientAccounts(em);
				} else {
					break;
				}
			} catch (NumberFormatException e) {
				System.err.println(e);
			}
		}
	}

	private static void addClient(EntityManager em) {
		System.out.println("Create new client");
		System.out.print("Input client name\r\n->");
		String name = sc.nextLine();
		System.out.print("Input client surname\r\n->");
		String surname = sc.nextLine();
		em.getTransaction().begin();
		try {
			Client c = new Client(name, surname);
			em.persist(c);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			System.err.println(e);
		}

	}

	private static void changeClient(EntityManager em) throws NumberFormatException {
		System.out.println("Change client");
		System.out.print("Input client id\r\n->");
		long id = Long.parseLong(sc.nextLine());
		Client c = em.find(Client.class, id);
		em.getTransaction().begin();
		try {
			System.out.print("Input new surname\r\n->");
			String surname = sc.nextLine();
			c.setSurname(surname);
			em.merge(c);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			System.err.println(e);
		}
	}

	private static void clientAccounts(EntityManager em) throws NumberFormatException {
		System.out.print("Input client id\r\n->");
		long id = Long.parseLong(sc.nextLine());
		while (true) {
			System.out.print(
					"1: client accounts\r\n2: Put money\r\n3: Transfer money\r\n4: Get all money\r\nexit: Enter\r\n->");
			String choose = sc.nextLine();
			if (choose.equals("1")) {
				List<Account> accountList = em.find(Client.class, id).getAccounts();
				for (Account account : accountList) {
					System.out.println(account);
				}
			} else if (choose.equals("2")) {
				putMoney(em, id);
			} else if (choose.equals("3")) {
				transferMoney(em, id);
			} else if (choose.equals("4")) {
				getAllMoney(em, id);
			} else {
				break;
			}

		}
	}

	private static void putMoney(EntityManager em, long id) {
		try {
			System.out.println("Select rate");
			System.out.print("1: UAH\r\n2: USD\r\n3: EUR\r\n->");
			String choose = sc.nextLine();
			Account acc = null;
			Query query = em.createNamedQuery("Account.client");
			query.setParameter("clientId", id);
			Currency cur = null;
			if (choose.equals("1")) {
				cur = Currency.valueOf("UAH");
			} else if (choose.equals("2")) {
				cur = Currency.valueOf("USD");
			} else if (choose.equals("3")) {
				cur = Currency.valueOf("EUR");
			}
			query.setParameter("currency", cur);
			System.out.print("Input money\r\n->");
			BigDecimal money = sc.nextBigDecimal();
			sc.nextLine();
			putTransaction(em, acc, query, money, cur, id);
		} catch (NonUniqueResultException | NoSuchElementException | IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public static void putTransaction(EntityManager em, Account acc, Query query, BigDecimal money, Currency cur,
			long id) {
		try {
			acc = (Account) query.getSingleResult();
			acc.setMoney(acc.getMoney().add(money));
		} catch (NoResultException e) {
			acc = new Account(money, cur, em.find(Client.class, id));
		}
		em.getTransaction().begin();
		try {
			em.persist(acc);
			AccTransaction accTr = new AccTransaction(em.find(Client.class, id), money, acc, true);
			em.persist(accTr);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
		}
	}

	private static void transferMoney(EntityManager em, long id) {
		System.out.print("Select rate\r\n1: UAH to EUR\r\n2: UAH to USD\r\n3: USD to EUR\r\n");
		System.out.print("4: USD to UAH\r\n5: EUR to USD\r\n6: EUR to UAH\r\n->");
		String choose = sc.nextLine();
		Query queryFrom = em.createNamedQuery("Account.client", Account.class);
		queryFrom.setParameter("clientId", id);
		Query queryTo = em.createNamedQuery("Account.client", Account.class);
		queryTo.setParameter("clientId", id);
		Query queryEx = em.createNamedQuery("Exchange.get", ExchangeRate.class);
		transferQueryFromTo(choose, queryFrom, queryTo);
		transferQueryEx(choose, queryEx);
		Account accFrom = (Account) queryFrom.getSingleResult();
		Account accTo = transferGetAccTo(em, choose, queryTo, id);
		ExchangeRate ex = (ExchangeRate) queryEx.getSingleResult();
		System.out.println("You have " + accFrom.getMoney() + " " + accFrom.getCurrency());
		System.out.print("Input how much money you want to transfer\r\n->");
		BigDecimal money = sc.nextBigDecimal();
		sc.nextLine();
		if (money.compareTo(accFrom.getMoney()) > 0) {
			System.out.println("You don't have enough money");
			return;
		}
		saveTransfer(em, accFrom, accTo, ex, money, id);
	}

	private static void transferQueryFromTo(String choose, Query queryFrom, Query queryTo) {
		if (choose.equals("1")) {
			queryFrom.setParameter("currency", Currency.valueOf("UAH"));
			queryTo.setParameter("currency", Currency.valueOf("EUR"));
		} else if (choose.equals("2")) {
			queryFrom.setParameter("currency", Currency.valueOf("UAH"));
			queryTo.setParameter("currency", Currency.valueOf("USD"));
		} else if (choose.equals("3")) {
			queryFrom.setParameter("currency", Currency.valueOf("USD"));
			queryTo.setParameter("currency", Currency.valueOf("EUR"));
		} else if (choose.equals("4")) {
			queryFrom.setParameter("currency", Currency.valueOf("USD"));
			queryTo.setParameter("currency", Currency.valueOf("UAH"));
		} else if (choose.equals("5")) {
			queryFrom.setParameter("currency", Currency.valueOf("EUR"));
			queryTo.setParameter("currency", Currency.valueOf("USD"));
		} else if (choose.equals("6")) {
			queryFrom.setParameter("currency", Currency.valueOf("EUR"));
			queryTo.setParameter("currency", Currency.valueOf("UAH"));
		}
	}

	private static void transferQueryEx(String choose, Query queryEx) {
		if (choose.equals("1")) {
			queryEx.setParameter("from", Currency.valueOf("UAH"));
			queryEx.setParameter("to", Currency.valueOf("EUR"));
		} else if (choose.equals("2")) {
			queryEx.setParameter("from", Currency.valueOf("UAH"));
			queryEx.setParameter("to", Currency.valueOf("USD"));
		} else if (choose.equals("3")) {
			queryEx.setParameter("from", Currency.valueOf("USD"));
			queryEx.setParameter("to", Currency.valueOf("EUR"));
		} else if (choose.equals("4")) {
			queryEx.setParameter("from", Currency.valueOf("USD"));
			queryEx.setParameter("to", Currency.valueOf("UAH"));
		} else if (choose.equals("5")) {
			queryEx.setParameter("from", Currency.valueOf("EUR"));
			queryEx.setParameter("to", Currency.valueOf("USD"));
		} else if (choose.equals("6")) {
			queryEx.setParameter("from", Currency.valueOf("EUR"));
			queryEx.setParameter("to", Currency.valueOf("UAH"));
		}
	}

	private static Account transferGetAccTo(EntityManager em, String choose, Query queryTo, long id) {
		Account accTo = null;
		try {
			accTo = (Account) queryTo.getSingleResult();
		} catch (NoResultException e) {
			if (accTo == null) {
				Currency cur = null;
				if (choose.equals("1") || choose.equals("3")) {
					cur = Currency.EUR;
				} else if (choose.equals("2") || choose.equals("5")) {
					cur = Currency.USD;
				} else if (choose.equals("4") || choose.equals("6")) {
					cur = Currency.UAH;
				}
				accTo = new Account(new BigDecimal("0"), cur, em.find(Client.class, id));
			}
		}
		return accTo;
	}

	private static void saveTransfer(EntityManager em, Account accFrom, Account accTo, ExchangeRate ex,
			BigDecimal money, long id) {
		em.getTransaction().begin();
		try {
			accFrom.setMoney(accFrom.getMoney().subtract(money));
			em.persist(accFrom);
			AccTransaction accTr = new AccTransaction(em.find(Client.class, id), money, accFrom, false);
			em.persist(accTr);
			ExchTransaction exTr = new ExchTransaction(em.find(Client.class, id), money, ex);
			em.persist(exTr);
			money = money.multiply(ex.getExchange());
			accTo.setMoney(accTo.getMoney().add(money));
			em.persist(accTo);
			accTr = new AccTransaction(em.find(Client.class, id), money, accTo, true);
			em.persist(accTr);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		}
	}

	private static void getAllMoney(EntityManager em, long id) {
		Client c = em.find(Client.class, id);
		if (c == null) {
			return;
		}
		em.getTransaction().begin();
		BigDecimal totalMoney = new BigDecimal("0");
		try {
			totalMoney = getAllFromAcc(em, c, totalMoney);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
		}
		System.out.println("You get " + totalMoney + " UAH");
	}

	private static BigDecimal getAllFromAcc(EntityManager em, Client c, BigDecimal totalMoney) throws Exception {
		Query query = em.createNamedQuery("Exchange.get", ExchangeRate.class);
		query.setParameter("to", Currency.valueOf("UAH"));
		AccTransaction tr = null;
		for (Account acc : c.getAccounts()) {
			ExchTransaction trEx = null;
			tr = new AccTransaction(c, acc.getMoney(), acc, false);
			if (acc.getCurrency().toString().equals("UAH")) {
				totalMoney = totalMoney.add(acc.getMoney());
			} else {
				if (acc.getCurrency().toString().equals("USD")) {
					query.setParameter("from", Currency.valueOf("USD"));
				} else if (acc.getCurrency().toString().equals("EUR")) {
					query.setParameter("from", Currency.valueOf("EUR"));
				}
				ExchangeRate ex = (ExchangeRate) query.getSingleResult();
				totalMoney = totalMoney.add(acc.getMoney().multiply(ex.getExchange()));
				trEx = new ExchTransaction(c, acc.getMoney(), ex);
			}
			acc.setMoney(new BigDecimal("0"));
			em.persist(acc);
			em.persist(tr);
			if (trEx != null) {
				em.persist(trEx);
			}
		}
		return totalMoney;
	}

	private static void transactionMenu(EntityManager em) throws NumberFormatException {
		while (true) {
			try {
				System.out.println("Transaction menu");
				System.out.print(
						"1: Transaction list\r\n2: Client transaction\r\n3: Client account transaction\r\n4: Client exchange transaction\r\nexit: Enter\r\n->");
				String choose = sc.nextLine();
				if (choose.equals("1")) {
					List<Transaction> transactionList = em.createNamedQuery("Transaction.all", Transaction.class)
							.getResultList();
					for (Transaction tr : transactionList) {
						System.out.println(tr);
					}
				} else if (choose.equals("2") || choose.equals("3") || choose.equals("4")) {
					transactionClient(em, choose);
				} else {
					break;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	private static void transactionClient(EntityManager em, String choose) {
		System.out.print("Input client id\r\n->");
		long id = Long.parseLong(sc.nextLine());
		if (choose.equals("2")) {
			Query query = em.createNamedQuery("Transaction.client", Transaction.class);
			query.setParameter("clientId", id);
			List<Transaction> transactionList = query.getResultList();
			for (Transaction tr : transactionList) {
				System.out.println(tr);
			}
		} else if (choose.equals("3")) {
			Query query = em.createNamedQuery("Transaction.acc", AccTransaction.class);
			query.setParameter("clientId", id);
			List<AccTransaction> transactionList = query.getResultList();
			for (AccTransaction tr : transactionList) {
				System.out.println(tr);
			}
		} else if (choose.equals("4")) {
			Query query = em.createNamedQuery("Transaction.exch", ExchTransaction.class);
			query.setParameter("clientId", id);
			List<ExchTransaction> transactionList = query.getResultList();
			for (ExchTransaction tr : transactionList) {
				System.out.println(tr);
			}
		}
	}

	private static void accountMenu(EntityManager em) {
		while (true) {
			try {
				System.out.println("Account menu");
				System.out.print(
						"1: Account list\r\n2: Client account\r\n3: Work with client accounts\r\nexit: Enter\r\n->");
				String choose = sc.nextLine();
				if (choose.equals("1")) {
					List<Account> accountList = em.createNamedQuery("Account.all", Account.class).getResultList();
					for (Account acc : accountList) {
						System.out.println(acc);
					}
				} else if (choose.equals("2")) {
					accountClient(em);
				} else if (choose.equals("3")) {
					clientAccounts(em);
				} else {
					break;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	private static void accountClient(EntityManager em) {
		System.out.print("Input client id\r\n->");
		long id = Long.parseLong(sc.nextLine());
		Query query = em.createNamedQuery("Account.clientAcc", Account.class);
		query.setParameter("clientId", id);
		List<Account> accountList = query.getResultList();
		for (Account acc : accountList) {
			System.out.println(acc);
		}
	}

	private static void exchangeMenu(EntityManager em) {
		while (true) {
			try {
				System.out.println("Exchange menu");
				System.out.print("1: Exchange rates\r\n2: Change exchange rate\r\nexit: Enter\r\n->");
				String choose = sc.nextLine();
				if (choose.equals("1")) {
					List<ExchangeRate> exList = em.createNamedQuery("Exchange.all", ExchangeRate.class).getResultList();
					for (ExchangeRate ex : exList) {
						System.out.println(ex);
					}
				} else if (choose.equals("2")) {
					exchangeChange(em);
				} else {
					break;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	private static void exchangeChange(EntityManager em) {
		System.out.print("Input id exchage rate\r\n->");
		long id = Long.parseLong(sc.nextLine());
		ExchangeRate ex = em.find(ExchangeRate.class, id);
		System.out.println("Old exchange rate " + ex.getFromCur() + "->" + ex.getToCur() + ": " + ex.getExchange());
		System.out.print("Input new value\r\n->");
		em.getTransaction().begin();
		try {
			BigDecimal value = sc.nextBigDecimal();
			sc.nextLine();
			ex.setExchange(value);
			em.persist(ex);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			System.err.println(e);
		}
	}

}
