package brunohorta.pt.takemyspot;

public class Spot {
    private String senderId;
    private double latitude;
    private double longitude;

    public Spot() {
    }

    public Spot(String senderId, double latitude, double longitude) {
        this.senderId = senderId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

