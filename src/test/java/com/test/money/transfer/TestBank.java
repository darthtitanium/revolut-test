package com.test.money.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

public class TestBank {

	@Test
	public void testTransfer() throws InterruptedException, ExecutionException, BrokenBarrierException {
		Bank bank = new Bank();
		bank.setAccount("a", 100.0);
		bank.setAccount("b", 100.0);
		
		ExecutorService pool = Executors.newFixedThreadPool(5);
		CountDownLatch latch = new CountDownLatch(5);
		for (int i = 0; i < 5; i++) {
			pool.submit(() -> {
				bank.transfer("a", "b", 10.0);
				latch.countDown();
			});
		}
		latch.await();
		Assert.assertEquals(bank.getBalance("a"), 50.0, 0.1);
		Assert.assertEquals(bank.getBalance("b"), 150.0, 0.1);
		System.out.println(bank.state());
	}
	
	@Test
	public void testMultiTransfer() throws InterruptedException {
		Bank bank = new Bank();
		bank.setAccount("a", 100.0);
		bank.setAccount("b", 100.0);
		bank.setAccount("c", 100.0);
		
		ExecutorService pool = Executors.newFixedThreadPool(5);
		
		CountDownLatch latch = new CountDownLatch(5);
		
		for (int i = 0; i < 5; i++) {
			pool.execute(() -> {
				bank.transfer("a", "b", 30.0);
				bank.transfer("a", "c", 25.0);
				latch.countDown();
			});
		}
		latch.await();
		Assert.assertEquals(bank.getBalance("a"), 15.0, 0.1);
		Assert.assertEquals(bank.getBalance("b"), 160.0, 0.1);
		Assert.assertEquals(bank.getBalance("c"), 125.0, 0.1);
		System.out.println(bank.state());
	}
	
	@Test
	public void testDeadlock() throws InterruptedException {
		Bank bank = new Bank();
		bank.setAccount("a", 100.0);
		bank.setAccount("b", 100.0);
		
		ExecutorService pool = Executors.newFixedThreadPool(10);
		
		CountDownLatch latch = new CountDownLatch(500);
		
		for (int i = 0; i < 500; i++) {
			pool.execute(() -> {
				if (Math.random() < 0.5) {
					bank.transfer("a", "b", 30.0);
				} else {
					bank.transfer("b", "a", 25.0);
				}
				latch.countDown();
			});
		}
		latch.await();
		Assert.assertEquals(bank.getBalance("a") + bank.getBalance("b"), 200.0, 0.1);
		System.out.println(bank.state());
	}
	
	@Test
	public void testMultipleDrain() throws InterruptedException {
		Bank bank = new Bank();
		bank.setAccount("a", 100.0);
		bank.setAccount("b", 100.0);
		bank.setAccount("c", 100.0);
		
		ExecutorService pool = Executors.newFixedThreadPool(10);
		
		CountDownLatch latch = new CountDownLatch(500);
		
		for (int i = 0; i < 500; i++) {
			pool.execute(() -> {
				long start;
				if (Math.random() < 0.5) {
					start = System.currentTimeMillis(); 
					bank.transfer("a", "b", 1.0);
					System.out.println("b waited for lock :" + (System.currentTimeMillis() - start));
				} else {
					start = System.currentTimeMillis(); 
					bank.transfer("a", "c", 1.0);
					System.out.println("c waited for lock :" + (System.currentTimeMillis() - start));
				}
				latch.countDown();
			});
		}
		latch.await();
		Assert.assertEquals(bank.getBalance("a") + bank.getBalance("b") + bank.getBalance("c"), 300.0, 0.1);
		System.out.println(bank.state());
	}
	
	@Test
	public void testPerfTransfer() throws InterruptedException {
		Bank bank = new Bank();
		bank.setAccount("a", 1000.0);
		bank.setAccount("b", 1000.0);
		bank.setAccount("c", 1000.0);
		
		ExecutorService pool = Executors.newFixedThreadPool(10);
		List<Double> stats = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(100_000);
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100_000; i++) {
			pool.execute(() -> {
				bank.transfer("a", "b", 0.0001);
				bank.transfer("b", "c", 0.0001);
				latch.countDown();
			});
			if (i % 1000 == 0) {
				stats.add(bank.getBalance("b"));
			}
		}
		latch.await();
		long duration = System.currentTimeMillis() - start;
		System.out.println(duration + " ms for 100K transfers");
		System.out.println(stats);
		stats.sort((d1, d2) -> d1 < d2 ? -1 : (d1 > d2 ? 1 : 0));
		
		System.out.println(stats);
		Assert.assertEquals(stats.get(0), 1000.0, 0.0001);
		Assert.assertEquals(stats.get(stats.size() - 1), 1000.0001, 0.0001);
		
		Assert.assertEquals(bank.getBalance("a"), 990.0, 0.01);
		Assert.assertEquals(bank.getBalance("b"), 1000.0, 0.01);
		Assert.assertEquals(bank.getBalance("c"), 1010.0, 0.01);
		Assert.assertEquals(bank.getBalance("a") + bank.getBalance("b") + bank.getBalance("c"), 3000.0, 0.00001);
		System.out.println(bank.state());
	}
}
