package miesgroup.mies.webdev.Persistance.Model;

import java.util.Arrays;

public class AlertResponse {
    private AlertData[] alertData;
    private Boolean checkEmail;

    public AlertResponse(AlertData[] alertData, Boolean checkEmail) {
        this.alertData = alertData;
        this.checkEmail = checkEmail;
    }

    public AlertData[] getAlertData() {
        return alertData;
    }

    public void setAlertData(AlertData[] alertData) {
        this.alertData = alertData;
    }

    public Boolean getCheckEmail() {
        return checkEmail;
    }

    public void setCheckEmail(Boolean checkEmail) {
        this.checkEmail = checkEmail;
    }

    @Override
    public String toString() {
        return "AlertResponse{" +
                "alertData=" + Arrays.toString(alertData) +
                ", checkEmail=" + checkEmail +
                '}';
    }
}
