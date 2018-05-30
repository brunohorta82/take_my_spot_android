package brunohorta.pt.takemyspot.entity;

public class NavigationNotification {

    private long spotId;
    private double latitude;
    private double longitude;
    private String takerId;

    public NavigationNotification() {
    }

    public NavigationNotification(long spotId, double latitude, double longitude, String takerId) {
        this.spotId = spotId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.takerId = takerId;
    }

    public String getTakerId() {
        return takerId;
    }

    public void setTakerId(String takerId) {
        this.takerId = takerId;
    }

    public long getSpotId() {
        return spotId;
    }

    public void setSpotId(long spotId) {
        this.spotId = spotId;
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
