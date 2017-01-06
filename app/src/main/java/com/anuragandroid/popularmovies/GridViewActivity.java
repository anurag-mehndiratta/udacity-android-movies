package com.anuragandroid.popularmovies;

import java.util.Properties;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.anuragandroid.popularmovies.utils.PropertyReader;
import com.anuragandroid.popularmovies.utils.SharedPreferences;

public class GridViewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            //Initialize the cache
            SharedPreferences.initializeCache("user");  //Logged-in user (for future enhancements)

            //Fetch API Key and store in cache
            fetchAPIKey();

            Log.d("TWO PANE MODE: ", getResources().getBoolean(R.bool.twoPaneMode) + "");

            if(savedInstanceState == null) {
                //Initialize the fragment
                initializeMainFragment("popular");  //Default value
            }
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_gridview);

        }catch(Exception e){
            Toast.makeText(GridViewActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            Log.d("Exception",e.getMessage(),e);
        }
    }

    /**
     * This method inflates the main menu on creation
     *
     * @param menu
     * @return boolean - options menu created or not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * This method handles when an item is selected from the menu
     *
     * @param item - Menu item
     * @return boolean - item selection processed flag
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Get the id of the selected item
        int id = item.getItemId();

        String filter = "popular"; //Default

        if (id == R.id.action_popular) {
            filter = "popular";
        }else if (id == R.id.action_top_rated) {
            filter = "top_rated";
        }else if (id == R.id.action_favorites){
            filter = "favorites";
        }

        initializeMainFragment(filter);

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is used to transition from one fragment to the other
     *
     * @param fragmentId    - fragment_grid for landscape mode
     * @param movieId       - id of the movie
     * @param title         - title of the movie
     * @param image         - image of the movie
     */
    public void goToFragment(int fragmentId, String movieId, String title, String image){
        Fragment movieDetailFragment = new MovieDetailFragment();
        //Add id, title and image as arguments to the movie detail fragment
        Bundle args = new Bundle();
        args.putString("id", movieId);
        args.putString("title", title);
        args.putString("image", image);
        movieDetailFragment.setArguments(args);
        FragmentManager fragMan = getFragmentManager();
        FragmentTransaction fragTrans = fragMan.beginTransaction();
        fragTrans.replace(fragmentId, movieDetailFragment, "second");
        fragTrans.addToBackStack(movieDetailFragment.getClass().getName());
        fragTrans.commit();
    }

    /**
     * This method is used to clear loader fragment
     *
     */
    public void clearLoaderFragment(){
        FragmentManager fragMan = getFragmentManager();
        FragmentTransaction fragTrans = fragMan.beginTransaction();
        Fragment frag = fragMan.findFragmentByTag("load");
        if(frag != null) {
            fragTrans.remove(frag);
            fragTrans.commit();
        }
    }

    /**
     * This method fetches the fragment from the back stack
     *
     */
    @Override
    public void onBackPressed() {
        Log.d("Callback stack: ", getFragmentManager().getBackStackEntryCount() + "");
        FragmentManager mgr = getFragmentManager();
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * This method reads the apiKey from the property file
     * Stores API Key in cache
     */
    private void fetchAPIKey() throws Exception{
        PropertyReader propertyReader = new PropertyReader(getApplicationContext());
        Properties properties = propertyReader.getProperties("APIDetails.properties");
        if (properties.containsKey("apiKey")) {
            Log.d("Debug", "Properties Loaded");
        }else{
            throw new Exception("API Key not found");
        }
        SharedPreferences.setApiKey(properties.getProperty("apiKey"));
        Log.d("API_KEY", SharedPreferences.getApiKey());
    }

    /** This method initializes the main fragment
     *
     */
    private void initializeMainFragment(String filter) {
        //Add apiKey as an argument to the fragment
        Bundle args = new Bundle();
        args.putString("filter", filter);
        //Initialize the fragment
        Fragment gridViewFragment = new GridViewFragment();
        gridViewFragment.setArguments(args);

        //Start transaction manager for fragment transactions
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_grid, gridViewFragment, "first");
        if(getResources().getBoolean(R.bool.twoPaneMode)) {
            Fragment loaderFragment = new LoaderFragment();
            fragmentTransaction.replace(R.id.fragment_movie_detail, loaderFragment, "load");
        }
        fragmentTransaction.commit();
    }
}