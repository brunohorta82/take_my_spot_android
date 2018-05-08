package brunohorta.pt.takemyspot;

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

import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Repository handling the work with products and comments.
 */
public class SpotRepository {

    private static SpotRepository sInstance;

    private final SpotsAPI spotsAPI;

    public void registerSpot(Spot spot, Callback<JsonObject> callback) {
        new RegisterSpotAsync(spot, spotsAPI, callback).execute();
    }

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


}