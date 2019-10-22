package com.test.money.transfer;

public interface IBank {

	public void transfer(String from, String to, Double sum);
	
	public Double getBalance(String name);
	
	public boolean setAccount(String name, Double balance);
}
