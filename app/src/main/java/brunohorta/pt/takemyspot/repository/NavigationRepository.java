package brunohorta.pt.takemyspot.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

import brunohorta.pt.takemyspot.api.NavigationAPI;
import brunohorta.pt.takemyspot.application.TakeMySpotApp;
import brunohorta.pt.takemyspot.entity.NavigationNotification;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Repository handling the work with products and comments.
 */
public class NavigationRepository {

    private static NavigationRepository sInstance;

    private final NavigationAPI navigationAPI;

    private NavigationRepository() {
        Gson gson = new GsonBuilder()
                .setLenient().registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                    @Override
                    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                        return src == null ? null : new JsonPrimitive(src.getTime());
                    }
                }).registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    @Override
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsLong());
                    }
                })
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.20:8080")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        navigationAPI = retrofit.create(NavigationAPI.class);
    }


    public static NavigationRepository getInstance() {
        if (sInstance == null) {
            synchronized (NavigationRepository.class) {
                if (sInstance == null) {
                    sInstance = new NavigationRepository();
                }
            }
        }
        return sInstance;
    }

    public void updateUserLocation(double latitude, double longitude, boolean callAPI, Callback<JsonObject> callback) {
        TakeMySpotApp.getInstance().getLocationPreferences().updateLocation((float) latitude, (float) longitude);
        if (callAPI) {
            registerCurrentLocation(callback);
        }
    }

    public void registerCurrentLocation(Callback<JsonObject> callback) {
        if (TakeMySpotApp.getInstance().getSpotPreferences().getSpot() != null) {
            long spotId = TakeMySpotApp.getInstance().getSpotPreferences().getSpot().getSpotId();
            double latitude = TakeMySpotApp.getInstance().getLocationPreferences().getLatitude();
            double longitude = TakeMySpotApp.getInstance().getLocationPreferences().getLongitude();
            NavigationNotification navigationNotification = new NavigationNotification(spotId, latitude, longitude, TakeMySpotApp.getInstance().getPushToken());
            navigationAPI.grabSpot(navigationNotification).enqueue(callback);
        }
    }


}