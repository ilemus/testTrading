package com.lemus.trading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.HttpsURLConnection;

public class TradingDummy implements TradingApi {
    private static final String PRICE_REQUEST_FORMAT = "https://api.iextrading.com/1.0/stock/%s/price";
    
    private double balance = 0.0;
    private double buyingPower = 0.0;
    
    List<Order> transactions = new ArrayList<Order>();
    List<Position> portfolio = new ArrayList<Position>();
    BlockingQueue<Order> orders = new LinkedBlockingQueue<Order>();
    
    public TradingDummy() {
        startTradingProcessor();
    }
    
    private Thread tradingThread;
    public void startTradingProcessor() {
        if (tradingThread != null && tradingThread.isAlive()) return;
        tradingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Order order = orders.take();
                        double currentPrice = price(order.symbol);
                        // TODO consider hours, volume
                        if (order.price == 0)
                            order.price = price(order.symbol);
                        else if (currentPrice <= order.price)
                            order.price = currentPrice;
                        else
                            continue;
                        
                        transactions.add(order);
                        portfolio.add(new Position(order));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            
        });
        tradingThread.start();
    }
    
    public void stopTradingProcessor() {
        tradingThread.interrupt();
    }
    
    private static double parsePrice(HttpsURLConnection conn) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String input = br.readLine();
            br.close();
            return Double.parseDouble(input);
        } catch (IOException e) {
            e.printStackTrace();
            return -1.0;
        }
    }

    @Override
    public double price(String symbol) {
        URL url;
        try {
            url = new URL(String.format(PRICE_REQUEST_FORMAT, symbol));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return -1.0;
        }
        try {
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            return parsePrice(conn);
        } catch (IOException e) {
            e.printStackTrace();
            return -1.0;
        }
    }

    @Override
    public void marketBuy(String symbol, int shares) {
        // TODO Auto-generated method stub
        Order order = new Order(symbol, shares);
        orders.offer(order);
    }

    @Override
    public void marketSell(String symbol, int shares) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void limitBuy(String symbol, double price, int shares) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void limitSell(String symbol, double price, int shares) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<Order> getPendingOrders() {
        List<Order> os = new ArrayList<Order>();
        for (Order order : orders) {
            os.add(order);
        }
        return os;
    }

    @Override
    public List<Order> getTransactions() {
        return transactions;
    }

    @Override
    public void addMoney(double amount) {
        balance += amount;
        buyingPower += amount;
    }

    @Override
    public synchronized double balance() {
        return balance;
    }
    
    @Override
    public synchronized double buyingPower() {
        return buyingPower;
    }
}
