package miesgroup.mies.webdev.Model;

import java.time.LocalDate;

public class Future {
    private LocalDate date;
    private double settlementPrice;
    private String year;
    private String month;
    private String quarter;

    // Costruttore
    public Future(LocalDate date, double settlementPrice, String year, String month, String quarter) {
        this.date = date;
        this.settlementPrice = settlementPrice;
        this.year = year;
        this.month = month;
        this.quarter = quarter;
    }

    public Future(){

    }

    // Getters e Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getSettlementPrice() {
        return settlementPrice;
    }

    public void setSettlementPrice(double settlementPrice) {
        this.settlementPrice = settlementPrice;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    // Metodo toString per debug
    @Override
    public String toString() {
        return "Futures{" +
                "date=" + date +
                ", settlementPrice=" + settlementPrice +
                ", year='" + year + '\'' +
                ", month='" + month + '\'' +
                ", quarter='" + quarter + '\'' +
                '}';
    }
}
