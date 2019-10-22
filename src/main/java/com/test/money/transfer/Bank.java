package com.test.money.transfer;

import java.util.HashMap;
import java.util.Map;

public class Bank implements IBank {

	private Map<String, Account> accounts = new HashMap<>();
	private int counter = 0;
	
	public boolean setAccount(String name, Double balance) {
		if (accounts.containsKey(name)) {
			return false;
		}
		Account account = new Account(counter++, balance);
		accounts.put(name, account);
		return true;
	}
	
	public void transfer(String from, String to, Double sum) {
		Account fromAcc = accounts.get(from);
		Account toAcc = accounts.get(to);
		if (fromAcc.getId() < toAcc.getId()) {
			fromAcc.writeLock();
			toAcc.writeLock();
			if (fromAcc.transfer(-sum)) {
				toAcc.transfer(sum);
			}
			toAcc.writeUnlock();
			fromAcc.writeUnlock();
		} else {
			toAcc.writeLock();
			fromAcc.writeLock();
			if (fromAcc.transfer(-sum)) {
				toAcc.transfer(sum);
			}
			fromAcc.writeUnlock();
			toAcc.writeUnlock();
		}
		
	}

	public String state() {
		return accounts.toString();
	}

	public Double getBalance(String name) {
		Account account = accounts.get(name);
		account.readLock();
		Double balance = account.getBalance();
		account.readUnlock();
		return balance;
	}
	
}
