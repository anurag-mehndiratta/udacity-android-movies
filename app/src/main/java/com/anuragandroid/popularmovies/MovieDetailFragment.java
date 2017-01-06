package com.anuragandroid.popularmovies;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.anuragandroid.popularmovies.models.GridItem;
import com.anuragandroid.popularmovies.models.MovieItem;
import com.anuragandroid.popularmovies.utils.CommonUtils;
import com.anuragandroid.popularmovies.utils.SharedPreferences;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This fragment handles the movie details
 */
public class MovieDetailFragment extends Fragment {
    private ProgressBar mProgressBar;
    private TableLayout mTableLayout;
    private TextView titleTextView;
    private ImageView poster;
    private ToggleButton favorite;
    private TextView releaseDateTextView;
    private TextView ratings;
    private TextView overview;

    private Map<String, String> trailer;
    private Map<String, String> review;
    private List<String> reviewResults;
    View _view;
    GridItem gridItem;
    private String title, synopsis, releaseDate, url, voteAvg, id, image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Movie Detail","On create Method called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        try{
            Log.d("Movie Detail","On Create View Method called");
            _view = inflater.inflate(R.layout.fragment_movie_detail, container, false);
            mProgressBar = (ProgressBar) _view.findViewById(R.id.detailProgressBar);
            mProgressBar.setVisibility(View.VISIBLE);

            fetchCurrMovieDetails();
            initializeViews();
            downloadData();
            handleFavMovie();

            mTableLayout.setVisibility(View.GONE);

        }catch(Exception e){
            mTableLayout.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            Log.d("Exception", e.getMessage(), e);
        }
        return _view;
    }

    /**
     * This method fetches the movie details from the selected item in the list
     */
    private void fetchCurrMovieDetails(){
        gridItem = new GridItem();
        Bundle args = getArguments();
        id = args.getString("id");
        gridItem.setId(id);
        image = args.getString("image");
        gridItem.setImage(image);
        title = args.getString("title");
        gridItem.setTitle(title);
        Log.d("Movie id", id);
        Log.d("Image",image);
        Log.d("Title",title);
    }

    /**
     * This method is used to initialize the views
     */
    private void initializeViews(){
        mTableLayout = (TableLayout) _view.findViewById(R.id.table);
        titleTextView = (TextView) _view.findViewById(R.id.title);
        favorite = (ToggleButton) _view.findViewById(R.id.favorite);
        poster = (ImageView) _view.findViewById(R.id.movieImage);
        releaseDateTextView = (TextView) _view.findViewById(R.id.releaseDate);
        ratings = (TextView) _view.findViewById(R.id.voteAvg);
        overview = (TextView) _view.findViewById(R.id.synopsis);

        //Datastructures for movie details
        trailer = new LinkedHashMap<String, String>();
        review = new LinkedHashMap<String, String>();
        reviewResults = new ArrayList<String>();
    }

    /**
     * This method is used to download data
     */
    private void downloadData(){
        String apiKey = SharedPreferences.getApiKey();
        Log.d("API_KEY", apiKey);
        String FEED_URL = "https://api.themoviedb.org/3/movie/" + id + "?api_key=" + apiKey;
        String FEED_URL_VIDS = "http://api.themoviedb.org/3/movie/" + id + "/videos?api_key=" + apiKey;
        String FEED_URL_RVWS = "http://api.themoviedb.org/3/movie/" + id + "/reviews?api_key=" + apiKey;
        Log.d("FeedURL", FEED_URL);
        Log.d("FeedURLVids", FEED_URL_VIDS);
        Log.d("FeedURLReviews", FEED_URL_RVWS);
        //Start download
        new AsyncHttpTask().execute(FEED_URL, FEED_URL_VIDS, FEED_URL_RVWS);
    }

    private void handleFavMovie(){
        Log.d("Movie is favorite: ", SharedPreferences.isFavoriteItem("user", id).toString());
        favorite.setChecked(SharedPreferences.isFavoriteItem("user", id));
        favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d("State: ", b + "");
                SharedPreferences.getFavoriteItems("user");    //Initialize map/list corresponding to the logged-in user
                if (b) {
                    SharedPreferences.addToFavoriteItems(gridItem);
                    favorite.setChecked(true);
                } else {
                    SharedPreferences.removeFromFavoriteItems("user", gridItem);
                    favorite.setChecked(false);
                }
                Log.d("Favorite List: ", SharedPreferences.getFavoriteItems("user") + "");
            }
        });
    }
    //Downloading data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            try {
                if(SharedPreferences.getMovieFromCache(id) == null) {
                    // Create Apache HttpClient
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));
                    int statusCode = httpResponse.getStatusLine().getStatusCode();

                    // 200 represents HTTP OK
                    if (statusCode == 200) {
                        String response = CommonUtils.streamToString(httpResponse.getEntity().getContent());
                        parseResult(response);
                        result = 1; // Successful
                    } else {
                        result = 0; //"Failed
                    }
                }else{
                    Log.d("Getting Movie","Cache");
                    MovieItem item = SharedPreferences.getMovieFromCache(id);
                    title = item.getTitle();
                    synopsis = item.getPlotSynopsis();
                    releaseDate = item.getReleaseDate();
                    url = item.getMoviePoster();
                    voteAvg = item.getVoteAverage();
                    result = 1;     //Successfully fetched
                }

                if(SharedPreferences.getTrailerFromCache(id) == null) {
                    // Create Apache HttpClient
                    HttpClient httpclientVids = new DefaultHttpClient();
                    HttpResponse httpResponseVids = httpclientVids.execute(new HttpGet(params[1]));
                    int statusCodeVids = httpResponseVids.getStatusLine().getStatusCode();

                    // 200 represents HTTP OK
                    if (statusCodeVids == 200) {
                        String responseVids = CommonUtils.streamToString(httpResponseVids.getEntity().getContent());
                        parseResultVids(responseVids);
                        result = 1; // Successful
                    } else {
                        result = 0; //"Failed
                    }
                }else{
                    Log.d("Getting trailer","Cache");
                    trailer = SharedPreferences.getTrailerFromCache(id);
                    result = 1; //Successful
                }

                if(SharedPreferences.getReviewsFromCache(id) == null) {
                    // Create Apache HttpClient
                    HttpClient httpclientRvws = new DefaultHttpClient();
                    HttpResponse httpResponseRvws = httpclientRvws.execute(new HttpGet(params[2]));
                    int statusCodeRvws = httpResponseRvws.getStatusLine().getStatusCode();

                    // 200 represents HTTP OK
                    if (statusCodeRvws == 200) {
                        String responseRvws = CommonUtils.streamToString(httpResponseRvws.getEntity().getContent());
                        parseResultRvws(responseRvws);
                        result = 1; // Successful
                    } else {
                        result = 0; //"Failed
                    }
                }else{
                    Log.d("Getting review","Cache");
                    Map<String, String> reviewMap = SharedPreferences.getReviewsFromCache(id);
                    for(String key: reviewMap.keySet()) {
                        reviewResults.add(key + " : " + reviewMap.get(key));
                    }
                    review = reviewMap;
                    result = 1; //Successful
                }
            } catch (Exception e) {
                Log.d("Exception", e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Lets update UI
            if (result == 1) {
                bindDataToUI();
            }else {
                Toast.makeText(getActivity(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }

            //Hide progressbar
            mProgressBar.setVisibility(View.GONE);
            mTableLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method binds data to UI
     */
    public void bindDataToUI(){
        {
            Log.i("Result", "Set data to UI");
            TableLayout tablelayout = (TableLayout) _view.findViewById(R.id.table);
            tablelayout.setColumnShrinkable(1, true);
            titleTextView.setText(title);
            Picasso.with(getActivity().getBaseContext()).load(url).into(poster);
            releaseDateTextView.setText("Release Date: " + releaseDate);
            ratings.setText("Rating: " + voteAvg + "/10");
            overview.setText(synopsis);
            List temp = new ArrayList<String>(trailer.keySet());
            Log.i("Number of trailers", temp.size() + "");
            Log.i("Number of reviews", reviewResults.size() + "");
            Log.i("Reviews Data structure", review.size() + "");

            final LinearLayout trailerLayout = (LinearLayout) _view.findViewById(R.id.r1);
            final TextView[] trailerView = new TextView[trailer.size()];
            final List<String> trailerTempList = new ArrayList<String>(trailer.keySet());
            for( int i = 0; i < trailerTempList.size(); i++ )
            {
                final LinearLayout linearLayout = new LinearLayout(getActivity());
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                ImageView btnGreen = new ImageView(getActivity());
                btnGreen.setImageResource(R.drawable.play_icon);

                View v = new View(getActivity());
                v.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 5
                ));
                v.setBackgroundColor(Color.parseColor("#B3B3B3"));
                trailerView[i] = new TextView(getActivity());
                trailerView[i].setText(trailerTempList.get(i));
                trailerView[i].setPadding(0, 40, 0, 0);
                linearLayout.addView(btnGreen);
                linearLayout.addView(trailerView[i]);
                Log.d("Total Views: ", linearLayout.getChildCount() + "");
                for(int j = 0; j<linearLayout.getChildCount(); j++){
                    Log.d("View: ",j + " "+linearLayout.getChildAt(j));
                }
                linearLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (b)
                            view.setBackgroundColor(Color.rgb(211, 211, 211));
                    }
                });
                linearLayout.setBackgroundResource(R.drawable.movie_selector);
                linearLayout.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        String videoId = ((TextView) linearLayout.getChildAt(1)).getText().toString();
                        Toast.makeText(getActivity().getBaseContext(), videoId, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailer.get(videoId))));
                        Log.d("Video", "Video Playing...." + videoId + ":" + trailer.get(videoId));
                    }
                });

                trailerLayout.addView(linearLayout);
                if(i<(trailerTempList.size()-1))
                    trailerLayout.addView(v);
            }

            final LinearLayout linearLayout = (LinearLayout) _view.findViewById(R.id.r2);
            final TextView[] reviewsView = new TextView[review.size()];
            final List<String> reviewTempList = new ArrayList<String>(review.keySet());
            for( int i = 0; i < reviewTempList.size(); i++ )
            {
                View v = new View(getActivity());
                v.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 5
                ));

                reviewsView[i] = new TextView(getActivity());
                reviewsView[i].setText(reviewTempList.get(i) + " : " + review.get(reviewTempList.get(i)));
                reviewsView[i].setPadding(0,5,0,5);
                linearLayout.addView(reviewsView[i]);
                if(i<(reviewTempList.size()-1))
                    linearLayout.addView(v);
            }
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
            MovieItem item = new MovieItem();
            title = response.optString("title");
            item.setTitle(title);
            synopsis = response.getString("overview");
            item.setPlotSynopsis(synopsis);
            releaseDate = response.getString("release_date");
            item.setReleaseDate(releaseDate);
            String moviePoster = response.getString("poster_path");
            url = "http://image.tmdb.org/t/p/w342/"+moviePoster;
            item.setMoviePoster(url);
            voteAvg = response.getString("vote_average");
            item.setVoteAverage(voteAvg);
            SharedPreferences.addMovieToCache(id, item);
            Log.d("title", title);
            Log.d("overview", synopsis);
            Log.d("releaseDate", releaseDate);
            Log.d("posterPath", url);
            Log.d("voteAvg",voteAvg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parsing the feed results and get the videos
     *
     * @param result
     */
    private void parseResultVids(String result) {
        try {
            JSONObject response = new JSONObject(result);
            Log.d("Videos JSON: ",response.toString());
            JSONArray results = response.getJSONArray("results");

            for(int i=0; i<results.length(); i++) {
                JSONObject jsonObj = results.getJSONObject(i);
                String trailerTitle = jsonObj.getString("name");
                String trailerKey = jsonObj.getString("key");
                Log.d("Trailer Number",i + " trailer");
                Log.d("trailerTitle", trailerTitle);
                Log.d("trailerKey", trailerKey);
                trailer.put(trailerTitle, trailerKey);
            }
            SharedPreferences.addTrailerToCache(id, trailer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parsing the feed results and get the reviews
     *
     * @param result
     */
    private void parseResultRvws(String result) {
        try {
            JSONObject response = new JSONObject(result);
            Log.d("Reviews JSON: ",response.toString());
            JSONArray results = response.optJSONArray("results");

            for(int i=0; i<results.length(); i++){
                JSONObject jsonObj = results.getJSONObject(i);
                String author = jsonObj.getString("author");
                String content = jsonObj.getString("content");
                Log.d("Review Number",i+" review");
                Log.d("Author: ",author);
                Log.d("Content: ",content);
                review.put(author, content);
                reviewResults.add(author + " : " + content);
                SharedPreferences.addReviewToCache(id, review);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("Movie Detail","Save Instance Method called");
        outState.putString("id",id);
        outState.putString("title",title);
        outState.putString("image",image);
    }
}
