package com.anuragandroid.popularmovies;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anuragandroid.popularmovies.models.GridItem;
import com.anuragandroid.popularmovies.utils.CommonUtils;
import com.anuragandroid.popularmovies.utils.SharedPreferences;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * This Fragment is the parent fragment which displays all the movies
 */
public class GridViewFragment extends Fragment {
    private GridView mGridView;
    private ProgressBar mProgressBar;

    public static final String TAG = GridViewFragment.class.getName();
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;

    String filter = "popular";  //Default value
    private String FEED_URL = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Grid View","On Create Method called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("Grid View","On Grid View Method called");
        View _view = inflater.inflate(R.layout.fragment_movie_grid, container, false);
        Log.d("First call","GridViewFragment");
        Bundle args = getArguments();
        String apiKey = SharedPreferences.getApiKey();
        Log.d("Grid - onCreate","View initialized");
        if(args!=null) {
            filter = args.getString("filter");
        }

        Log.i("onCreate - GridFragment", filter);

        FEED_URL = "http://api.themoviedb.org/3/movie/popular?api_key=" + apiKey;   //Default Value for Popular Movies (Default Selection)
        if ("top_rated".equalsIgnoreCase(filter)) {
            FEED_URL = "http://api.themoviedb.org/3/movie/top_rated?api_key="+apiKey;
        }

        mGridView = (GridView) (_view.findViewById(R.id.gridView));
        mProgressBar = (ProgressBar) (_view.findViewById(R.id.progressBar));

        //Initialize with empty data
        mGridData = new ArrayList<GridItem>();
        mGridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                mProgressBar.setVisibility(View.VISIBLE);
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                String movieId = item.getId();
                Toast.makeText(getActivity().getBaseContext(), item.getTitle(), Toast.LENGTH_LONG).show();

                Log.d("Movie id", movieId);
                Log.d("Image", item.getTitle());
                Log.d("Title", item.getImage());
                if(getResources().getBoolean(R.bool.twoPaneMode)){ //Update second fragment
                    ((GridViewActivity) getActivity()).goToFragment(R.id.fragment_movie_detail, movieId, item.getTitle(), item.getImage());
                }else { //Replace first fragment - Go to Second Fragment
                    ((GridViewActivity) getActivity()).goToFragment(R.id.fragment_grid, movieId, item.getTitle(), item.getImage());
                }

                //Hide progressbar
                mProgressBar.setVisibility(View.GONE);
            }
        });

        Log.d("Filter", filter);
        Log.d("Favorites Before",SharedPreferences.getFavoriteItems("user").size()+"");
        Log.d("Top Rated Before", SharedPreferences.getTopRatedCache("user").size() + "");
        Log.d("Popular Before", SharedPreferences.getPopularCache("user").size() + "");
        Log.d("Feed URL Before",FEED_URL);

        if ("favorites".equalsIgnoreCase(filter)) { //Fetch data from SharedPreferences
            Log.d("Starting activity", "Favorites");
            Log.d("Favorites", "Fetching data from data structure");
            mProgressBar.setVisibility(View.VISIBLE);
            mGridData.addAll(SharedPreferences.getFavoriteItems("user"));
            if(SharedPreferences.getFavoriteItems("user").size() == 0){     //Common for both modes
                Toast.makeText(getActivity(), "No favorite data found!", Toast.LENGTH_SHORT).show();
                ((GridViewActivity) getActivity()).clearLoaderFragment();
            }else if(getResources().getBoolean(R.bool.twoPaneMode)) {   //Only for two pane mode
                GridItem currentGrid = mGridData.get(0);
                ((GridViewActivity) getActivity()).goToFragment(R.id.fragment_movie_detail, currentGrid.getId(), currentGrid.getTitle(), currentGrid.getImage());
            }
            Log.d("Favorites After", SharedPreferences.getFavoriteItems("user").size() + "");
            mProgressBar.setVisibility(View.GONE);
        }else if("top_rated".equalsIgnoreCase(filter)){
            Log.d("Starting activity","Top Rated");
            if(SharedPreferences.getTopRatedCache("user")==null || SharedPreferences.getTopRatedCache("user").size()==0) {
                Log.d("Top Rated", "Fetching data from Network");
                new AsyncHttpTask().execute(FEED_URL);
                mProgressBar.setVisibility(View.VISIBLE);
            }else{  //Fetch data from SharedPreferences
                Log.d("Top Rated", "Fetching data from cache");
                mGridData.addAll(SharedPreferences.getTopRatedCache("user"));
                if(getResources().getBoolean(R.bool.twoPaneMode)) {
                    GridItem currentGrid = mGridData.get(0);
                    ((GridViewActivity) getActivity()).goToFragment(R.id.fragment_movie_detail, currentGrid.getId(), currentGrid.getTitle(), currentGrid.getImage());
                }
                //Hide progressbar
                mProgressBar.setVisibility(View.GONE);
            }
        }else if("popular".equalsIgnoreCase(filter)){
            //Start download
            Log.d("Starting activity","Popular");
            if(SharedPreferences.getPopularCache("user")==null || SharedPreferences.getPopularCache("user").size()==0) {
                //Start download
                Log.d("Popular", "Fetching data from Network");
                new AsyncHttpTask().execute(FEED_URL);
                mProgressBar.setVisibility(View.VISIBLE);
            }else{ //Fetch data from SharedPreferences
                Log.d("Popular", "Fetching data from cache");
                mGridData.addAll(SharedPreferences.getPopularCache("user"));
                if(getResources().getBoolean(R.bool.twoPaneMode)) {
                    GridItem currentGrid = mGridData.get(0);
                    ((GridViewActivity) getActivity()).goToFragment(R.id.fragment_movie_detail, currentGrid.getId(), currentGrid.getTitle(), currentGrid.getImage());
                }
                //Hide progressbar
                mProgressBar.setVisibility(View.GONE);
            }
        }
        //Inflate the layout for this fragment
        return _view;
    }

    //Downloading data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            try {
                Log.d("Background call","Call made");
                // Create Apache HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                Log.d("Feed URL: ",params[0]);  //Feel URL is passed as first parameter
                HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));
                int statusCode = httpResponse.getStatusLine().getStatusCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    String response = CommonUtils.streamToString(httpResponse.getEntity().getContent());
                    parseResult(response);
                    result = 1; // Successful
                    Log.d("Feed URL Call: ","Success");
                } else {
                    result = 0; //"Failed
                    Log.d("Feed URL Call: ","Failed");
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
                Log.d("Feed URL Call: ","Failed");
            }

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d("Post execute", "post execute");
            // Download complete. Lets update UI
            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(getActivity(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            //Hide progressbar
            mProgressBar.setVisibility(View.GONE);
        }
    }


    /**
     * Parsing the feed results and get the list
     *
     * @param result
     */
    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray posts = response.optJSONArray("results");
            GridItem item;
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);
                String title = post.optString("title");
                item = new GridItem();
                item.setTitle(title);
                String id = post.optString("id");
                Log.d("ID",id);
                item.setId(id);
                String posterPath = post.getString("poster_path");
                String url = "http://image.tmdb.org/t/p/w500/"+posterPath;
                item.setImage(url);
                mGridData.add(item);
            }
            if("top_rated".equalsIgnoreCase(filter)) {
                SharedPreferences.getTopRatedCache("user").addAll(mGridData);
                Log.d("Top Rated After", SharedPreferences.getTopRatedCache("user").size() + "");
            }else if("popular".equalsIgnoreCase(filter)){
                SharedPreferences.getPopularCache("user").addAll(mGridData);
                Log.d("Popular After", SharedPreferences.getPopularCache("user").size() + "");
            }
            if(getResources().getBoolean(R.bool.twoPaneMode)) {
                GridItem currentGrid = mGridData.get(0);
                ((GridViewActivity) getActivity()).goToFragment(R.id.fragment_movie_detail, currentGrid.getId(), currentGrid.getTitle(), currentGrid.getImage());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("Grid View","Save Instance Method called");
    }
}
