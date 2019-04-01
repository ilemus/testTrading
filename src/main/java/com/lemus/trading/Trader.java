package com.lemus.trading;

import java.util.HashMap;
import java.util.Scanner;

public class Trader {
    private TradingApi mApi = null;
    public static void main(String[] args) {
        Trader trader = new Trader();
        trader.start();
    }
    
    public void start() {
        mApi = new TradingDummy();
        Scanner input = new Scanner(System.in);
        System.out.println("Options:"
            + "\n\t- login\n\t- logout\n\t- quit"
            + "\n\t- l\tlist current portfolio"
            + "\n\t- p\n\t- mb <symbol> <shares>\n\t- lb <symbol> <price> <shares>\n\t- ms <symbol> <shares>\n\t- ls <symbol> <price> <shares>\n\t- price <symbol>");
        String line;

        while((line = input.nextLine()) != null) {
            try {
                if (!processCommand(line)) break;
            } catch (Exception e) {
                System.err.println("Format error in command");
            }
        }

        input.close();
    }

    private enum Command {
        LOGIN,
        LOGOUT,
        LIST,
        PENDING,
        MARKET_BUY,
        LIMIT_BUY,
        MARKET_SELL,
        LIMIT_SELL,
        PRICE
    }

    private HashMap<String, Command> commands = new HashMap<String, Command>(){{
        put("login", Command.LOGIN);
        put("logout", Command.LOGOUT);
        put("quit", Command.LOGOUT);
        put("l", Command.LIST);
        put("p", Command.PENDING);
        put("mb", Command.MARKET_BUY);
        put("lb", Command.LIMIT_BUY);
        put("ms", Command.MARKET_SELL);
        put("ls", Command.LIMIT_SELL);
        put("price", Command.PRICE);
        }};
    private boolean processCommand(String command) {
        String[] split = command.split(" ");
        if (split.length <= 0) {
            return true;
        }

        Command cmd = commands.get(split[0]);
        if (cmd == null) return true;
        switch (cmd) {
            case LOGIN:
                mApi.login();
                break;
            case LOGOUT:
                mApi.logout();
                ((TradingDummy) mApi).stopTradingProcessor();
                return false;
            case LIST:
                System.out.println(mApi.getPortfolio());
                break;
            case MARKET_BUY:
                mApi.marketBuy(split[1], Integer.parseInt(split[2]));
                break;
            case LIMIT_BUY:
                mApi.limitBuy(split[1], Double.parseDouble(split[2]), Integer.parseInt(split[3]));
                break;
            case PRICE:
                System.out.println(mApi.price(split[1]));
                break;
            case PENDING:
                System.out.println(mApi.getPendingOrders());
                break;
        }
        return true;
    }
}
