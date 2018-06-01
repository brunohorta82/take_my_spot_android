package brunohorta.pt.takemyspot.entity;

public class SpotValidation {
    private long spotId;
    private String senderId;
    private String takerId;

    public SpotValidation() {
    }

    public SpotValidation(long spotId, String senderId, String takerId) {
        this.spotId = spotId;
        this.senderId = senderId;
        this.takerId = takerId;
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

    public String getTakerId() {
        return takerId;
    }

    public void setTakerId(String takerId) {
        this.takerId = takerId;
    }
}

