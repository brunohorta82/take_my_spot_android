package brunohorta.pt.takemyspot.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;

import brunohorta.pt.takemyspot.entity.Spot;
import brunohorta.pt.takemyspot.utils.GsonUtils;

public class SpotPreferences {

    public static final String SPOT = "spot";

    private SharedPreferences preferences;
    private Spot spot;

    public SpotPreferences(Context context) {
        preferences = context.getSharedPreferences(SPOT, Context.MODE_PRIVATE);

    }

    public void setSpot(Spot spot) {
        this.spot = spot;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SPOT, GsonUtils.getGson().toJson(spot));
        editor.apply();
    }

    public Spot getSpot() {
        String spotStr = preferences.getString(SPOT, null);
        if (spot == null && spotStr != null) {
            Spot spot = GsonUtils.getGson().fromJson(spotStr, new TypeToken<Spot>() {
            }.getType());
        }
        return spot;
    }
}
