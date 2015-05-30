package eric.indecisiveeater.decision;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eric.indecisiveeater.Result;
import eric.indecisiveeater.R;
import eric.indecisiveeater.result.CardsActivity;

//Created by Eric on 3/26/2015.

public class DecisionFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    @InjectView(R.id.Nearby_Button) Button mNearbySearchButton;
    @InjectView(R.id.Takeout_Button) Button mTakeoutButton;
    @InjectView(R.id.Delivery_Button) Button mDeliveryButton;
    @InjectView(R.id.Indecisive_Button) Button mIndecisiveButton;

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;
    private static final int REQUEST_CODE_CONNECT_ERROR = 1001;
    private static final String RESOLVING_ERROR_KEY = "resolve_error";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mResolvingError = savedInstanceState.getBoolean(RESOLVING_ERROR_KEY);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_decision, parent, false);
        ButterKnife.inject(this, v);

        mNearbySearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocation();
                requestNearbyPlaces("restaurant");
            }
        });

        mTakeoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocation();
                requestNearbyPlaces("meal_takeaway");
            }
        });

        mDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocation();
                requestNearbyPlaces("meal_delivery");
            }
        });

        mIndecisiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(RESOLVING_ERROR_KEY, mResolvingError);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServiceAvailability();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Connection successful, get current location here.
        if (mLastLocation != null) {
            Toast.makeText(this.getActivity(), "Location acquired", Toast.LENGTH_SHORT).show();
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        //If mResolvingError is true, the app is already trying to resolve the error.
        if (!mResolvingError) {
            if (result.hasResolution()) {
                //Set mResolvingError to true so that app doesn't try to fix the problem twice.
                //Call startResolutionForResult to begin the error resolving process.
                try {
                    mResolvingError = true;
                    result.startResolutionForResult(this.getActivity(), REQUEST_CODE_CONNECT_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    //Resolution intent had a problem, try connecting again.
                    mGoogleApiClient.connect();
                }
            } else {
                //Display error dialog
                mResolvingError = true;
                GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this.getActivity(), REQUEST_CODE_CONNECT_ERROR).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONNECT_ERROR) {
            mResolvingError = false;
            if (resultCode == Activity.RESULT_OK) {
                if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    private void requestLocation() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void checkPlayServiceAvailability() {
        final int PLAY_SERVICE_REQUEST_CODE = 1;
        //Checks if the user device has the correct version of Google Play Services installed
        int mServiceCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity());
        if (mServiceCode != ConnectionResult.SUCCESS) {

            //Generates an error dialog if isUserRecoverableError() returns true,
            //Otherwise, it outputs "Device not supported"
            if (GooglePlayServicesUtil.isUserRecoverableError(mServiceCode)) {
                GooglePlayServicesUtil.getErrorDialog(mServiceCode, this.getActivity(), PLAY_SERVICE_REQUEST_CODE).show();
            } else {
                Toast.makeText(this.getActivity(), "Device not supported", Toast.LENGTH_LONG).show();
                this.getActivity().finish();
            }
        }
    }

    private void requestNearbyPlaces(String type) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        //Determine if the network connection was successful or not.
        //If so, construct the URL for a Place request here,
        // then pass it to a AsyncTask for it to make the connection to the site
        if (networkInfo != null && networkInfo.isConnected()) {
            final String NEARBY_PLACE_SEARCH = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";
            new GooglePlacesSearch().execute((NEARBY_PLACE_SEARCH
                    + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude()
                    + "&rankby=distance&opennow&types=" + type + "&key=AIzaSyCA9kQ7lX5d1fLfZ5RV2Y5rsynPo5OjxqE"));
        } else {
            //Tell user that the device is not connected to a network.
            Toast.makeText(this.getActivity(), "Network error", Toast.LENGTH_SHORT).show();
        }
    }

    private class GooglePlacesSearch extends AsyncTask<String, Void, ArrayList<Result>> {

        //You must uncomment the code in readStream() when you uncomment the variable below.
        //private String mNextPageToken;
        private ProgressDialog loadingDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new ProgressDialog(getActivity());
            loadingDialog.setMessage("Loading...");
            loadingDialog.show();
        }

        @Override
        protected ArrayList<Result> doInBackground(String... params) {
            try {
                //Create a new URL object in order to open the connection to the Nearby Search results
                URL placeURL = new URL(params[0]);
                HttpsURLConnection httpConnection = (HttpsURLConnection) placeURL.openConnection();
                ArrayList<Result> mResultInformation = readStream(httpConnection.getInputStream());
                httpConnection.disconnect();
                return mResultInformation;
            } catch (IOException e) {
                return null;
            }
        }

        //Takes a list of names, splits them into an array, and passes them into a new activity for display
        @Override
        protected void onPostExecute(ArrayList<Result> list) {
            super.onPostExecute(list);
            Intent i = new Intent(getActivity(), CardsActivity.class);
            i.putParcelableArrayListExtra("RESULTS", list);
            loadingDialog.dismiss();
            startActivity(i);
        }

        @TargetApi(19)
        private ArrayList<Result> readStream(InputStream in) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line, jsonData = "";
                ArrayList<Result> mResultList = new ArrayList<>();
                //Create the JSONObject by concatenating each line of the web page into a string
                while ((line = reader.readLine()) != null) {
                    jsonData += line;
                }
                JSONObject jsonObject = new JSONObject(jsonData);
                //Check if there are any results returned by the API
                if (!jsonObject.isNull("results")) {

                    //Obtain name field of all the results and store them each on a separate line
                    JSONArray jsonArray = (jsonObject.getJSONArray("results"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject result = jsonArray.getJSONObject(i);
                        Location location = new Location("result");
                        location.setLatitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                        location.setLongitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                        mResultList.add(new Result(result.getString("name"),
                                                   result.getString("vicinity"),
                                                   result.getString("place_id"),
                                                   new DecimalFormat("#.##").format(mLastLocation.distanceTo(location) / (float) 1600)));
                        if (!result.isNull("rating")) {
                            mResultList.get(mResultList.size()-1).setRating(result.getString("rating"));
                        }
                        if (!result.isNull("photos")) {
                            try {
                                mResultList.get(mResultList.size() - 1)
                                        .setIcon(result.getJSONArray("photos").getJSONObject(0).getString("photo_reference"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                return mResultList;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
