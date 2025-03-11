package miesgroup.mies.webdev.Model;

import java.util.Arrays;

public class AlertData {
    private String futuresType;
    private double maxPriceValue;
    private double minPriceValue;
    private String frequencyA;
    private boolean checkModality;
    private String email;
    private boolean checkEmail;

    public AlertData(String futuresType, double maxPriceValue, double minPriceValue, String frequencyA, boolean checkModality) {
        this.futuresType = futuresType;
        this.maxPriceValue = maxPriceValue;
        this.minPriceValue = minPriceValue;
        this.frequencyA = frequencyA;
        this.checkModality = checkModality;
    }
    public AlertData(String futuresType, double maxPriceValue, double minPriceValue, String frequencyA, boolean checkModality, String email, boolean checkEmail) {
        this.futuresType = futuresType;
        this.maxPriceValue = maxPriceValue;
        this.minPriceValue = minPriceValue;
        this.frequencyA = frequencyA;
        this.checkModality = checkModality;
    }
    public AlertData() {

    }


    public boolean isCheckModality() {
        return checkModality;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isCheckEmail() {
        return checkEmail;
    }

    public void setCheckEmail(boolean checkEmail) {
        this.checkEmail = checkEmail;
    }

    public String getFuturesType() {
        return futuresType;
    }

    public void setFuturesType(String futuresType) {
        this.futuresType = futuresType;
    }

    public double getMaxPriceValue() {
        return maxPriceValue;
    }

    public void setMaxPriceValue(double maxPriceValue) {
        this.maxPriceValue = maxPriceValue;
    }

    public String getFrequencyA() {
        return frequencyA;
    }

    public void setFrequencyA(String frequency) {
        this.frequencyA = frequency;
    }

    public double getMinPriceValue() {
        return minPriceValue;
    }

    public void setMinPriceValue(double minPriceValue) {
        this.minPriceValue = minPriceValue;
    }

    public boolean getCheckModality() {
        return checkModality;
    }

    public void setCheckModality(boolean checkModality) {
        this.checkModality = checkModality;
    }

    @Override
    public String toString() {
        return "AlertData{" +
               "futuresType='" + futuresType + '\'' +
               ", maxPriceValue=" + maxPriceValue +
               ", minPriceValue=" + minPriceValue +
               ", frequency=" + frequencyA +
               ", checkModality=" + checkModality +
               '}';
    }
}