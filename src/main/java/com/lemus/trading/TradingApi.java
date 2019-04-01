package com.lemus.trading;

import java.util.List;

public abstract interface TradingApi {
    public boolean login();
    public void logout();

    public double price(String symbol);
    public void limitBuy(String symbol, double price, int shares);
    public void marketBuy(String symbol, int shares);
    public void limitSell(String symbol, double price, int shares);
    public void marketSell(String symbol, int shares);
    
    public List<Order> getPendingOrders();
    public List<Order> getTransactions();
    public List<Position> getPortfolio();
    
    public void addMoney(double amount);
    public double buyingPower();
    public double balance();
    
    static class Order {
        String symbol;
        int shares;
        double price;
        public Order(String serialized) {
            String sub = serialized.substring(1, serialized.length() - 1);
            String[] split = sub.split(", ");
            this.symbol = split[0];
            this.shares = Integer.parseInt(split[1]);
            this.price = Double.parseDouble(split[2]);
        }

        public Order(String symbol, int shares) {
            this.symbol = symbol;
            this.shares = shares;
        }
        
        public String toString() {
            return String.format("{%s, %d, %f}", this.symbol, this.shares, this.price);
        }
    }
    
    static class Position {
        String symbol;
        int shares;
        double price;
        public Position(String serialized) {
            String sub = serialized.substring(1, serialized.length() - 1);
            String[] split = sub.split(", ");
            this.symbol = split[0];
            this.shares = Integer.parseInt(split[1]);
            this.price = Double.parseDouble(split[2]);
        }
        public Position(Order o) {
            this.symbol = o.symbol;
            this.shares = o.shares;
            this.price = o.price;
        }
        
        public String toString() {
            return String.format("{%s, %d, %f}", this.symbol, this.shares, this.price);
        }
    }
}
