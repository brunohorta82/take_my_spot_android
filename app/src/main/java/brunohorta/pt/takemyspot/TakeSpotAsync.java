package brunohorta.pt.takemyspot;

import android.os.AsyncTask;

import com.google.gson.JsonObject;

import retrofit2.Callback;

/**
 * Created by bruno on 08/05/2018.
 */

public class TakeSpotAsync extends AsyncTask<Void, Void, Void> {

    private SpotIntent spot;
    private SpotsAPI spotsAPI;

    private Callback<JsonObject> callback;

    public TakeSpotAsync(SpotIntent spot, SpotsAPI spotsAPI, Callback<JsonObject> callback) {
        this.spot = spot;
        this.spotsAPI = spotsAPI;
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        spotsAPI.grabSpot(spot).enqueue(callback);
        return null;
    }
}
