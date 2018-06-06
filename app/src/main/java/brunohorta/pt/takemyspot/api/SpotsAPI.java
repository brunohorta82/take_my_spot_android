package brunohorta.pt.takemyspot.api;

import com.google.gson.JsonObject;

import brunohorta.pt.takemyspot.entity.Spot;
import brunohorta.pt.takemyspot.entity.SpotIntent;
import brunohorta.pt.takemyspot.entity.SpotValidation;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SpotsAPI {

    @POST("api/spots/")
    Call<Spot> registerSpot(@Body Spot spot);

    @POST("api/spots/grab")
    Call<JsonObject> grabSpot(@Body SpotIntent spotIntent);

    @POST("api/spots/proof")
    Call<JsonObject> verifySpot(@Body SpotValidation spotValidation);

}