package eric.indecisiveeater.result;

//Created by Eric on 3/10/2015.

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eric.indecisiveeater.R;

public class ResultFragment extends android.support.v4.app.Fragment {
    @InjectView(R.id.result_name) TextView mResultName;
    @InjectView(R.id.details_button) Button mDetailsButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_display, parent, false);
        ButterKnife.inject(this, v);

        mResultName.setText(getArguments().getString("result_name"));

        mDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        return v;
    }
}
