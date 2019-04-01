package com.lemus.trading;

public class Trader {
    public static void main(String[] args) {
        Trader trader = new Trader();
        trader.start();
    }
    
    public void start() {
        TradingDummy dummy = new TradingDummy();
        dummy.login();

        double price = dummy.price("AAPL");
        System.out.println("Current price: " + price);
        dummy.marketBuy("AAPL", 5);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Transactions: " + dummy.getTransactions() + " pending: "
                + dummy.getPendingOrders() + " portfolio: " + dummy.portfolio);
        
        dummy.stopTradingProcessor();
        dummy.logout();
    }
}
