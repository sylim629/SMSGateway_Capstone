package com.example.leanne.getsms;

import java.util.ArrayList;

class BankNumbers {
    private ArrayList<String> bankNumberArrayList = new ArrayList<String>();

    private static final BankNumbers bankNumbers = new BankNumbers();

    static BankNumbers getInstance() {
        return bankNumbers;
    }

    ArrayList<String> getBankNumberArrayList() {
        return bankNumberArrayList;
    }

    void setBankNumberArrayList(String number) {
        bankNumberArrayList.add(number);
    }
}
