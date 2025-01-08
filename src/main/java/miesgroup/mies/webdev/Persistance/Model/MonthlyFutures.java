package miesgroup.mies.webdev.Persistance.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "monthly_futures")
public class MonthlyFutures {

    @Id
    private int id;

    @Column(name = "year")
    private int year;

    @Column(name = "month")
    private int month;

    @ManyToOne
    @JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false)
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
