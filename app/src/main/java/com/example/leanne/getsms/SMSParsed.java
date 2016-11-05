package com.example.leanne.getsms;

public class SMSParsed {
    private String date;
    private String money;

    public SMSParsed(String date, String money) {
        this.date = date;
        this.money = money;
    }

    public String getDate() {
        return date;
    }

    public String getMoney() {
        return money;
    }
}
