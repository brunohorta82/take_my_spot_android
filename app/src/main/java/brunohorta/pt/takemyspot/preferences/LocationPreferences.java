package brunohorta.pt.takemyspot.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class LocationPreferences {

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    private SharedPreferences preferences;
    private Float latitude;
    private Float longitude;

    public LocationPreferences(Context context) {
        preferences = context.getSharedPreferences(LATITUDE, Context.MODE_PRIVATE);
    }


    public void updateLocation(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(LATITUDE, latitude);
        editor.putFloat(LONGITUDE, longitude);
        editor.apply();
    }

    public float getLatitude() {
        if (latitude == null) {
            latitude = preferences.getFloat(LATITUDE, -9999);
        }
        return latitude;
    }

    public float getLongitude() {
        if (longitude == null) {
            longitude = preferences.getFloat(LONGITUDE, -9999);
        }
        return longitude;
    }
}
