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

import javax.net.ssl.HttpsURLConnection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eric.indecisiveeater.result.ResultActivity;
import eric.indecisiveeater.R;

//Created by Eric on 3/26/2015.

public class DecisionFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
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
                requestNearbyPlaces("restaurant");
            }
        });

        mTakeoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNearbyPlaces("meal_takeaway");
            }
        });

        mDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Toast.makeText(this.getActivity(), "Location acquired", Toast.LENGTH_SHORT).show();
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

    private class GooglePlacesSearch extends AsyncTask<String, Void, String> {

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
        protected String doInBackground(String... params) {
            try {
                //Create a new URL object in order to open the connection to the Nearby Search results
                URL placeURL = new URL(params[0]);
                HttpsURLConnection httpConnection = (HttpsURLConnection) placeURL.openConnection();
                String mNamesOfResults = readStream(httpConnection.getInputStream());
                httpConnection.disconnect();
                //This currently does NOT comply with google's terms of service.
                //DO NOT use this until user interaction makes this happen.

                //Sleep 1 second before request the next page of 20 results
//                Thread.sleep(1000);
//                if (!mNextPageToken.isEmpty()) {
//                    placeURL = new URL(params[0]+"&pagetoken="+mNextPageToken);
//                    httpConnection = (HttpsURLConnection) placeURL.openConnection();
//                    mNamesOfResults += readStream(httpConnection.getInputStream());
//                    httpConnection.disconnect();
//                }
                return mNamesOfResults;
            } catch (IOException e) {
                return "Search error";
            }
        }

        //Takes a list of names, splits them into an array, and passes them into a new activity for display
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Intent i = new Intent(getActivity(), ResultActivity.class);
            i.putExtra("LIST_OF_RESULTS", s.split("\n"));
            loadingDialog.dismiss();
            startActivity(i);
        }

        @TargetApi(19)
        private String readStream(InputStream in) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line, jsonData = "", listOfResults = "";
                //Create the JSONObject by concatenating each line of the webpage into a string
                while ((line = reader.readLine()) != null) {
                    jsonData += line;
                }
                JSONObject jsonObject = new JSONObject(jsonData);
                //Check if there are any results returned by the API
                if (!jsonObject.isNull("results")) {
                    //If there are more than 20 results,
                    // store the page token into mNextPageToken for the next HTTPURLConnection

                    //mNextPageToken = jsonObject.optString("next_page_token");

                    //Obtain name field of all the results and store them each on a separate line
                    JSONArray jsonArray = (jsonObject.getJSONArray("results"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject result = jsonArray.getJSONObject(i);
                        listOfResults += result.getString("name") + "\n";
                    }
                }
                return listOfResults;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
