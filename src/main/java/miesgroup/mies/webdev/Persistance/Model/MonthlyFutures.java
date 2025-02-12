package miesgroup.mies.webdev.Persistance.Model;

public class MonthlyFutures {

    private int id;
    private int year;
    private int month;
    private FuturesEEX futuresEex;  // Associazione alla tabella futures_eex

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public FuturesEEX getFuturesEex() {
        return futuresEex;
    }

    public void setFuturesEex(FuturesEEX futuresEex) {
        this.futuresEex = futuresEex;
    }
}
