package model;

import java.math.BigDecimal;

public class Treatment {

    private int id;
    private String name;
    private BigDecimal fee;

    public Treatment() {
    }

    public Treatment(int id, String name, BigDecimal fee) {
        this.id = id;
        this.name = name;
        this.fee = fee;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    @Override
    public String toString() {
        return name;
    }
}
