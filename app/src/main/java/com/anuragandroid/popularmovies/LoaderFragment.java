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

import java.util.ArrayList;

/**
 * This Fragment is dummy loader for the movie details fragment
 */
public class LoaderFragment extends Fragment {
    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View _view = inflater.inflate(R.layout.fragment_loader, container, false);

        mProgressBar = (ProgressBar) (_view.findViewById(R.id.loaderBar));
        mProgressBar.setVisibility(View.VISIBLE);

        //Inflate the layout for this fragment
        return _view;
    }
}
