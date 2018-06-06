package brunohorta.pt.takemyspot.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.google.android.gms.location.LocationResult;
import com.google.gson.JsonObject;

import brunohorta.pt.takemyspot.application.TakeMySpotApp;
import brunohorta.pt.takemyspot.entity.Spot;
import brunohorta.pt.takemyspot.entity.SpotIntent;
import brunohorta.pt.takemyspot.entity.SpotValidation;
import brunohorta.pt.takemyspot.repository.NavigationRepository;
import brunohorta.pt.takemyspot.repository.SpotRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {

    private double latitude = -9999;
    private double longitude = -9999;
    private SpotRepository mRepository;
    private NavigationRepository mNavRepository;

    public MainViewModel(@NonNull Application application, SpotRepository spotRepository, NavigationRepository navRepository) {
        super(application);
        this.mRepository = spotRepository;
        this.mNavRepository = navRepository;
        this.mRepository.checkAndDismissInterestingLocation();
        this.mRepository.updateMySpotAsTaken(null);
    }

    public MutableLiveData<Spot> getInterestingSpotLiveData() {
        return mRepository.getCurrentInterestingSpotLiveData();
    }

    public MutableLiveData<Spot> getMySpotTakenLiveData() {
        return mRepository.getMySpotTakenLiveData();
    }


    public Spot getInterestingSpot() {
        return mRepository.getCurrentInterestingSpot();
    }

    public void registerSpot(final Callback<Spot> callback) {
        final Spot spot = new Spot(System.currentTimeMillis(), 0, TakeMySpotApp.getInstance().getPushToken(), latitude, longitude);
        mRepository.registerSpot(spot, new Callback<Spot>() {
            @Override
            public void onResponse(@NonNull Call<Spot> call, @NonNull Response<Spot> response) {
                if (response.isSuccessful()) {
                    mRepository.updateMySpotAsTaken(response.body());
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<Spot> call, @NonNull Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void takeSpot(Callback<JsonObject> callback) {
        mRepository.takeSpot(new SpotIntent(TakeMySpotApp.getInstance().getPushToken(), mRepository.getCurrentInterestingSpot().getSpotId()), callback);
    }

    public void verifySpot(String senderId, Callback<JsonObject> callback) {
        mRepository.verifySpot(new SpotValidation(mRepository.getCurrentInterestingSpot().getSpotId(), senderId, TakeMySpotApp.getInstance().getPushToken()), callback);
    }

    public void updateLocation(LocationResult locationResult, Callback<JsonObject> callback) {
        if (locationResult.getLastLocation() == null) {
            latitude = -9999;
            longitude = -9999;
        } else {
            latitude = locationResult.getLastLocation().getLatitude();
            longitude = locationResult.getLastLocation().getLongitude();
        }
        mNavRepository.updateUserLocation(latitude, longitude, getInterestingSpot() != null, callback);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void dismissCurrentInterestingLocation() {
        this.mRepository.setInterestingSpotAvailable(null);

    }

    public void markSpotAsReserved() {
        this.mRepository.markInterestingSpotAsReserved();
    }

    public void dismissMyTakenSpot() {
        this.mRepository.updateMySpotAsTaken(null);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Application mApplication;

        public Factory(@NonNull Application application) {
            this.mApplication = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new MainViewModel(mApplication, SpotRepository.getInstance(), NavigationRepository.getInstance());
        }
    }
}
