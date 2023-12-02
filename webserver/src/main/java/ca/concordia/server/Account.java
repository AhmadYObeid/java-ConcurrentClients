package ca.concordia.server;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class Account {
    private int balance;
    private  int id;
    private final Lock lock = new ReentrantLock();


    public Account(int balance, int id) {
        this.balance = balance;
        this.id = id;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void setID(int IDD) {
         id = IDD;
    }

    public void setBalance(int Balanceee) {
        balance = Balanceee;
    }
    public int getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(int amount) {
        lock.lock();
        try {
            balance -= amount;
        } finally {
            lock.unlock();
        }
    }

    public void deposit(int amount) {
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }

}