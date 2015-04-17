package eric.indecisiveeater.result;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eric.indecisiveeater.R;

//Created by Eric on 3/27/2015

public class ResultActivity extends ActionBarActivity {
    @InjectView(R.id.viewpager) ViewPager mViewPager;
    ResultPagerAdapter mResultPagerAdapter;
    private String[] mRestaurantNames, mRestaurantAddresses, mRestaurantRatings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);
        ButterKnife.inject(this);

        mRestaurantNames = getIntent().getStringArrayExtra("RESULT_NAMES");
        mRestaurantAddresses = getIntent().getStringArrayExtra("RESULT_ADDRESSES");
        mRestaurantRatings = getIntent().getStringArrayExtra("RESULT_RATINGS");
        FragmentManager manager = getSupportFragmentManager();
        mResultPagerAdapter = new ResultPagerAdapter(manager);
        mViewPager.setAdapter(mResultPagerAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServiceAvailability();
    }

    private void checkPlayServiceAvailability() {
        final int PLAY_SERVICE_REQUEST_CODE = 1;
        //Checks if the user device has the correct version of Google Play Services installed
        int mServiceCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (mServiceCode != ConnectionResult.SUCCESS) {

            //Generates an error dialog if isUserRecoverableError() returns true,
            //Otherwise, it outputs "Device not supported"
            if (GooglePlayServicesUtil.isUserRecoverableError(mServiceCode)) {
                GooglePlayServicesUtil.getErrorDialog(mServiceCode, this, PLAY_SERVICE_REQUEST_CODE).show();
            } else {
                Toast.makeText(this, "Device not supported", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }

    public class ResultPagerAdapter extends FragmentPagerAdapter {

        public ResultPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            ResultFragment mResultFragment = new ResultFragment();
            Bundle data = new Bundle();
            data.putInt("current_page", position);
            data.putString("result_name", mRestaurantNames[position]);
            data.putString("result_address", mRestaurantAddresses[position]);
            data.putString("result_rating", mRestaurantRatings[position]);
            mResultFragment.setArguments(data);
            return mResultFragment;
        }

        @Override
        public int getCount() {
            return mRestaurantNames.length;
        }
    }
}
