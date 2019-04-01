package com.lemus.trading;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.HttpsURLConnection;

public class TradingDummy implements TradingApi {
    private static final String PRICE_REQUEST_FORMAT = "https://api.iextrading.com/1.0/stock/%s/price";
    private boolean loggedIn = false;
    private double balance = 0.0;
    private double buyingPower = 0.0;
    
    List<Order> transactions = new ArrayList<Order>();
    Map<String, Position> portfolio = new HashMap<String, Position>();
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
                        else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(1000);
                                        orders.offer(order);
                                    } catch (InterruptedException e) {
                                        orders.offer(order);
                                    }
                                }
                            }).start();
                            continue;
                        }
                        
                        transactions.add(order);
                        if (portfolio.containsKey(order.symbol)) {
                            Position o = portfolio.get(order.symbol);
                            if (order.shares <= 0) {
                                o.shares += order.shares;
                                // TODO: SHORTING BEHAVIOR???
                                if (o.shares == 0) portfolio.remove(order.symbol);
                                continue;
                            }
                            double actualPrice = (o.price * o.shares + order.price * order.shares) / (order.shares + o.shares);
                            o.price = actualPrice;
                            o.shares += order.shares;
                        } else {
                            portfolio.put(order.symbol, new Position(order));
                        }
                    } catch (InterruptedException e) {
                        break;
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

    private void savePortfolio() throws IOException {
        File file = new File("portfolio.dat");
        if (!file.exists()) file.createNewFile();

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        // Transactions
        for (Order order : transactions) {
            bw.write(order.toString());
            bw.newLine();
        }

        // Portfolio
        bw.write("*");
        bw.newLine();

        for (Map.Entry<String, Position> pos : portfolio.entrySet()) {
            bw.write(pos.getValue().toString());
            bw.newLine();
        }

        // Pending orders
        bw.write("*");
        bw.newLine();

        for (Order order : orders) {
            bw.write(order.toString());
            bw.newLine();
        }

        // End of file
        bw.write("*");
        bw.newLine();

        bw.close();
    }

    private void loadPortfolio() throws IOException {
        File file = new File("portfolio.dat");
        if (!file.exists()) return;

        FileReader fr = new FileReader(file.getAbsoluteFile());
        BufferedReader br = new BufferedReader(fr);
        String line;

        // Transactions
        while ((line = br.readLine()) != null) {
            if (line.startsWith("*")) break;

            Order order = new Order(line);
            transactions.add(order);
        }
        // Portfolio
        while ((line = br.readLine()) != null) {
            if (line.startsWith("*")) break;

            Position pos = new Position(line);
            portfolio.put(pos.symbol, pos);
        }
        // Pending orders
        while ((line = br.readLine()) != null) {
            if (line.startsWith("*")) break;

            Order order = new Order(line);
            orders.add(order);
        }
    }

    @Override
    public boolean login() {
        try {
            loadPortfolio();
        } catch(IOException e) {
            e.printStackTrace();
        }
        loggedIn = true;
        return true;
    }

    @Override
    public void logout() {
        try {
            savePortfolio();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loggedIn = false;
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
        if (!loggedIn) {
            System.out.println("Not logged in!");
            return;
        }
        Order order = new Order(symbol, shares);
        orders.offer(order);
    }

    @Override
    public void marketSell(String symbol, int shares) {
        // TODO Auto-generated method stub
         if (!loggedIn) {
            System.out.println("Not logged in!");
            return;
        }       
    }

    @Override
    public void limitBuy(String symbol, double price, int shares) {
        // TODO Auto-generated method stub
        if (!loggedIn) {
            System.out.println("Not logged in!");
            return;
        }
        Order order = new Order(symbol, shares);
        order.price = price;
        orders.offer(order);
    }

    @Override
    public void limitSell(String symbol, double price, int shares) {
        // TODO Auto-generated method stub
        if (!loggedIn) {
            System.out.println("Not logged in!");
            return;
        }
    }

    @Override
    public List<Order> getPendingOrders() {
        if (!loggedIn) {
            System.out.println("Not logged in!");
            return null;
        }
        List<Order> os = new ArrayList<Order>();
        for (Order order : orders) {
            os.add(order);
        }
        return os;
    }

    @Override
    public List<Order> getTransactions() {
        if (!loggedIn) {
            System.out.println("Not logged in!");
            return null;
        }
        return transactions;
    }

    @Override
    public List<Position> getPortfolio() {
        if (!loggedIn) {
            System.out.println("Not logged in!");
            return null;
        }
        List<Position> result = new ArrayList<Position>();
        for (Map.Entry<String, Position> entry : portfolio.entrySet()) {
            result.add(entry.getValue());
        }

        return result;
    }

    @Override
    public void addMoney(double amount) {
        if (!loggedIn) {
            System.out.println("Not logged in!");
            return;
        }
        balance += amount;
        buyingPower += amount;
    }

    @Override
    public synchronized double balance() {
        if (!loggedIn) {
            System.out.println("Not logged in!");
            return 0.0;
        }
        return balance;
    }
    
    @Override
    public synchronized double buyingPower() {
        if (!loggedIn) {
            System.out.println("Not logged in!");
            return 0.0;
        }
        return buyingPower;
    }
}
