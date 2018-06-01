package brunohorta.pt.takemyspot.repository;

import android.arch.lifecycle.MutableLiveData;

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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import java.lang.reflect.Type;
import java.util.Date;

import brunohorta.pt.takemyspot.api.SpotsAPI;
import brunohorta.pt.takemyspot.application.TakeMySpotApp;
import brunohorta.pt.takemyspot.entity.Spot;
import brunohorta.pt.takemyspot.entity.SpotIntent;
import brunohorta.pt.takemyspot.entity.SpotValidation;
import brunohorta.pt.takemyspot.preferences.LocationPreferences;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Repository handling the work with products and comments.
 */
public class SpotRepository {

    private static SpotRepository sInstance;

    private final SpotsAPI spotsAPI;
    private final MutableLiveData<Spot> interestingSpotLiveData = new MutableLiveData<>();
    private final MutableLiveData<Spot> mySpotTakenLiveData = new MutableLiveData<>();

    private SpotRepository() {
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
        spotsAPI = retrofit.create(SpotsAPI.class);
        interestingSpotLiveData.postValue(TakeMySpotApp.getInstance().getSpotPreferences().getSpot());
    }


    public static SpotRepository getInstance() {
        if (sInstance == null) {
            synchronized (SpotRepository.class) {
                if (sInstance == null) {
                    sInstance = new SpotRepository();
                }
            }
        }
        return sInstance;
    }

    public void registerSpot(Spot spot, Callback<JsonObject> callback) {
        spotsAPI.registerSpot(spot).enqueue(callback);
    }

    public void takeSpot(SpotIntent spot, Callback<JsonObject> callback) {
        spotsAPI.grabSpot(spot).enqueue(callback);
    }

    public void verifySpot(SpotValidation spot, Callback<JsonObject> callback) {
        spotsAPI.verifySpot(spot).enqueue(callback);
    }

    public boolean isSpotInteresting(long timestamp, double latitude, double longitude) {
        LocationPreferences locationPreferences = TakeMySpotApp.getInstance().getLocationPreferences();
        if (locationPreferences.getLatitude() <= -9999 || locationPreferences.getLongitude() <= -9999 || System.currentTimeMillis() - timestamp > 60000) {
            return false;
        }
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setNumPoints(32);
        shapeFactory.setCentre(new Coordinate(latitude, longitude));//there are your coordinates
        shapeFactory.setSize(1000 / ((Math.PI / 180) * 6378137) * 2);//this is how you set the radius
        Polygon circle = shapeFactory.createCircle();

        shapeFactory.setCentre(new Coordinate(locationPreferences.getLatitude(), locationPreferences.getLongitude()));//there are your coordinates
        shapeFactory.setSize(1000 / ((Math.PI / 180) * 6378137) * 2);//this is how you set the radius
        Polygon circleCurrent = shapeFactory.createCircle();

        if (circle.intersects(circleCurrent)) {
            return true;
        }
        return false;
    }

    public void setInterestingSpotAvailable(Spot spot) {
        TakeMySpotApp.getInstance().getSpotPreferences().setSpot(spot);
        interestingSpotLiveData.postValue(spot);
    }

    public MutableLiveData<Spot> getCurrentInterestingSpotLiveData() {
        return interestingSpotLiveData;
    }

    public Spot getCurrentInterestingSpot() {
        return TakeMySpotApp.getInstance().getSpotPreferences().getSpot();
    }

    public void checkAndDismissInterestingLocation() {
        Spot currentInterestingSpot = getCurrentInterestingSpot();
        if (currentInterestingSpot != null && !isSpotInteresting(currentInterestingSpot.getTimestamp(), currentInterestingSpot.getLatitude(), currentInterestingSpot.getLongitude())) {
            setInterestingSpotAvailable(null);
        }
    }

    public void markInterestingSpotAsReserved() {
        Spot interestingSpot = getCurrentInterestingSpot();
        interestingSpot.markAsReserved();
        setInterestingSpotAvailable(interestingSpot);
    }

    public void markMySpotTakenTakerLocation(double latitude, double longitude) {
        Spot mySpotTaken = mySpotTakenLiveData.getValue();
        if (mySpotTaken != null) {
            mySpotTaken.markTakerLocation(latitude, longitude);
        }
        updateMySpotAsTaken(mySpotTaken);
    }

    public void updateMySpotAsTaken(Spot spot) {
        mySpotTakenLiveData.postValue(spot);
    }

    public MutableLiveData<Spot> getMySpotTakenLiveData() {
        return mySpotTakenLiveData;
    }
}