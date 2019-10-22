package com.test.money.transfer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Executors;

import com.google.common.base.Splitter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import sun.net.httpserver.HttpServerImpl;

public class BankWebServer {

	private static HttpServer server;
	private static IBank bank = new Bank();
	
	public static void main(String[] args) throws IOException {
		server = HttpServerImpl.create(new InetSocketAddress(8888), 0);
		server.setExecutor(Executors.newFixedThreadPool(5));
		server.createContext("/createAccount", new HttpHandler() {
			
			@Override
			public void handle(HttpExchange arg0) throws IOException {
				Map<String, String> args = parseArgs(arg0.getRequestURI());
				String name = args.get("name");
				Double balance = Double.valueOf(args.get("balance"));
				String response = "Bad input args";
				if (name != null && !name.isEmpty() && balance != null) {
					boolean created = bank.setAccount(name, balance);
					if (!created) {
						response = "Account with this name already exists!";
					} else {
						response = "Created account " + name + " with balance " + bank.getBalance(name);
					}
				}
		        arg0.sendResponseHeaders(200, response.getBytes().length);
		        OutputStream os = arg0.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
			}
		});
		
		server.createContext("/transfer", new HttpHandler() {
			
			@Override
			public void handle(HttpExchange arg0) throws IOException {
				Map<String, String> args = parseArgs(arg0.getRequestURI());
				String from = args.get("from");
				String to = args.get("to");
				Double sum = Double.valueOf(args.get("sum"));
				String response = "Bad input args";
				if (from != null && !from.isEmpty() && to != null && !to.isEmpty() && sum != null) {
					bank.transfer(from, to, sum);
					response = "Transferred " + sum + " from " + from + " to " + to;
				}
		        arg0.sendResponseHeaders(200, response.getBytes().length);
		        OutputStream os = arg0.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
			}
		});
		
		server.createContext("/getBalance", new HttpHandler() {
			
			@Override
			public void handle(HttpExchange arg0) throws IOException {
				Map<String, String> args = parseArgs(arg0.getRequestURI());
				String name = args.get("name");
				String response = "Bad input args";
				if (name != null && !name.isEmpty()) {
					response = "Account " + name + " has balance " + bank.getBalance(name);
				}
		        arg0.sendResponseHeaders(200, response.getBytes().length);
		        OutputStream os = arg0.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
			}
		});
		
		server.start();
	}

	protected static Map<String, String> parseArgs(URI requestURI) {
		return Splitter.on("&").omitEmptyStrings().trimResults().withKeyValueSeparator("=").split(requestURI.toString().split("\\?")[1]);
	}
}
