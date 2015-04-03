package eric.indecisiveeater;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Eric on 3/26/2015.
 */
public class DecisionFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_decision, parent, false);

        Button mNearbySearchButton = (Button) v.findViewById(R.id.Nearby_Button);
        mNearbySearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SearchActivity.class);
                startActivity(i);
            }
        });

        Button mTakeoutButton = (Button) v.findViewById(R.id.Takeout_Button);
        mTakeoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button mDeliveryButton = (Button) v.findViewById(R.id.Delivery_Button);
        mDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button mIndecisiveButton = (Button) v.findViewById(R.id.Indecisive_Button);
        mIndecisiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return v;
    }
}
