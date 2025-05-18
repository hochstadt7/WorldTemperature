import java.util.Date;

public class DailyTemp {
    private Date date;
    private double temperature;

    public DailyTemp(double temperature, Date date) {
        this.temperature = temperature;
        this.date = date;
    }

    public double getTemperature() {
        return temperature;
    }

    public Date getDate() {
        return date;
    }
}
