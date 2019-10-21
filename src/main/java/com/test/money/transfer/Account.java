package com.test.money.transfer;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account {

	private int id;
	private Double balance;
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public Account(int id, Double balance) {
		this.id = id;
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "Account [id=" + id + ", balance=" + balance + "]";
	}

	public Double getBalance() {
		return balance;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean transfer(Double sum) {
		if (sum < 0 && balance < Math.abs(sum)) {
			return false;
		} else {
			balance += sum;
			return true;
		}
	}

	protected void writeLock() {
		lock.writeLock().lock();
	}
	
	protected void writeUnlock() {
		lock.writeLock().unlock();
	}

	protected void readLock() {
		lock.readLock().lock();
	}

	protected void readUnlock() {
		lock.readLock().unlock();
	}
	
}
