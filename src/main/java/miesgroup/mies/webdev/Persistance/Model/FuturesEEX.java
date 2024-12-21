package miesgroup.mies.webdev.Persistance.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "futures_eex")
public class FuturesEEX {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "date")
    private java.sql.Date date;

    @Column(name = "settlementPrice")
    private double settlementPrice;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public java.sql.Date getDate() {
        return date;
    }

    public void setDate(java.sql.Date date) {
        this.date = date;
    }

    public double getSettlementPrice() {
        return settlementPrice;
    }

    public void setSettlementPrice(double settlementPrice) {
        this.settlementPrice = settlementPrice;
    }
}