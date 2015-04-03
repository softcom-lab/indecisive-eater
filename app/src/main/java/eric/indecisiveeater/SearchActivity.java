package eric.indecisiveeater;

import android.app.Fragment;

/**
 * Created by Eric on 3/27/2015.
 */
public class SearchActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new NearbyFragment();
    }
}
