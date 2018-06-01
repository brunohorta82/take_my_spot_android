package brunohorta.pt.takemyspot.entity;

public class Spot {
    private long timestamp;
    private long spotId;
    private String senderId;
    private double latitude;
    private double longitude;
    private boolean reserved;
    private double takerLatitude;
    private double takerLongitude;
    private boolean validated;

    public Spot() {
    }

    public Spot(long timestamp, long spotId, String senderId, double latitude, double longitude) {
        this.timestamp = timestamp;
        this.spotId = spotId;
        this.senderId = senderId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getSpotId() {
        return spotId;
    }

    public void setSpotId(long spotId) {
        this.spotId = spotId;
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

    public void markAsReserved() {
        this.reserved = true;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public static boolean isSame(Spot spot, double spotId) {
        return spot.spotId == spotId;
    }

    public double getTakerLatitude() {
        return takerLatitude;
    }

    public void setTakerLatitude(double takerLatitude) {
        this.takerLatitude = takerLatitude;
    }

    public double getTakerLongitude() {
        return takerLongitude;
    }

    public void setTakerLongitude(double takerLongitude) {
        this.takerLongitude = takerLongitude;
    }

    public void markTakerLocation(double takerLatitude, double takerLongitude) {
        setTakerLatitude(takerLatitude);
        setTakerLongitude(takerLongitude);
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public boolean isValidated() {
        return validated;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

