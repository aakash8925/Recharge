package com.example.recharge;
public class RechargeDetails {
    private String mobileNumber;
    private String operator;
    private String pack;
    private String date;
    private Float profitpercent;
    private Float profit;
    private String mode;

    public RechargeDetails(String mobileNumber, String operator, String pack, String date, Float profitpercent, Float profit, String mode) {
        this.mobileNumber = mobileNumber;
        this.operator = operator;
        this.pack = pack;
        this.date = date;
        this.profitpercent = profitpercent;
        this.profit = profit;
        this.mode = mode;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getOperator() {
        return operator;
    }

    public String getPack() {
        return pack;
    }

    public String getDate() {
        return date;
    }

    public Float getProfitpercent() {
        return profitpercent;
    }

    public Float getProfit() {
        return profit;
    }

    public String getMode() {
        return mode;
    }
}
