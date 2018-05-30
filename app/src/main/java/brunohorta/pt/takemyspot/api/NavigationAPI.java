package brunohorta.pt.takemyspot.api;

import com.google.gson.JsonObject;

import brunohorta.pt.takemyspot.entity.NavigationNotification;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NavigationAPI {

    @POST("api/spots/navigate")
    Call<JsonObject> grabSpot(@Body NavigationNotification navigationNotification);

}