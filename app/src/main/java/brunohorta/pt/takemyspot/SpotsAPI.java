package brunohorta.pt.takemyspot;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SpotsAPI {

    @POST("api/spots/")
    Call<JsonObject> registerSpot(@Body Spot spot);

    @POST("api/spots/grab")
    Call<JsonObject>grabSpot(@Body SpotIntent spotIntent);

}