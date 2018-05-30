package brunohorta.pt.takemyspot.entity;


public class SpotIntent {

    private String takerId;
    private long spotId;

    public SpotIntent() {
    }

    public SpotIntent(String takerId, long spotId) {
        this.takerId = takerId;
        this.spotId = spotId;
    }

    public long getSpotId() {
        return spotId;
    }

    public void setSpotId(long spotId) {
        this.spotId = spotId;
    }

    public String getTakerId() {
        return takerId;
    }

    public void setTakerId(String takerId) {
        this.takerId = takerId;
    }


}

