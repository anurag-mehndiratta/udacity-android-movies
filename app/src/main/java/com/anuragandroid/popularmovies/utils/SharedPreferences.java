package com.anuragandroid.popularmovies.utils;

import android.util.Log;

import com.anuragandroid.popularmovies.models.GridItem;
import com.anuragandroid.popularmovies.models.MovieItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * shared Preferences mimic database storage
 */
public class SharedPreferences {
    private static Map<String,List<GridItem>> userTopRatedMap;
    private static List<GridItem> topRatedCache;
    private static Map<String,List<GridItem>> popularMap;
    private static List<GridItem> popularCache;
    private static Map<String,List<GridItem>> favoriteItemsMap;
    private static List<GridItem> favoriteItems;
    private static Map<String, MovieItem> idItemMap;
    private static Map<String,Map<String,String>> idVideoMap;
    private static Map<String,Map<String,String>> idReviewMap;
    private static String apiKey;

    public static void addToTopRatedCache(GridItem gridItem){
        topRatedCache.add(gridItem);
    }

    public static void addToFavoriteItems(GridItem gridItem){
        favoriteItems.add(gridItem);
    }

    public static void addToPopularCache(GridItem gridItem){
        popularCache.add(gridItem);
    }

    public static void removeFromTopRatedCache(GridItem gridItem){
        topRatedCache.remove(gridItem);
    }

    public static boolean removeFromFavoriteItems(String user, GridItem gridItem){
        Log.d("Removing Favorite: ", gridItem.getId());
        for(GridItem g: SharedPreferences.getFavoriteItems(user)){
            if(g.getId().equalsIgnoreCase(gridItem.getId())) {
                Log.d("Grid Item: ", g.getId());
                return SharedPreferences.getFavoriteItems(user).remove(g);
            }
        }
        return false;
    }

    public static void removeFromPopularCache(GridItem gridItem){
        popularCache.remove(gridItem);
    }

    public static List<GridItem> getTopRatedCache(String user) {
        topRatedCache = getUserTopRatedMap().get(user);
        if(topRatedCache == null) {
            topRatedCache = new ArrayList<GridItem>();
            getUserTopRatedMap().put(user,topRatedCache);
        }
        return topRatedCache;
    }

    public static Map<String, List<GridItem>> getUserTopRatedMap(){
        if(userTopRatedMap == null) {
            userTopRatedMap = new HashMap<String, List<GridItem>>();
        }
        return userTopRatedMap;
    }

    public static List<GridItem> getPopularCache(String user) {
        popularCache = getUserPopularMap().get(user);
        if(popularCache == null) {
            popularCache = new ArrayList<GridItem>();
            getUserPopularMap().put(user,popularCache);
        }
        return popularCache;
    }

    public static Map<String, List<GridItem>> getUserPopularMap(){
        if(popularMap == null){
            popularMap = new HashMap<String, List<GridItem>>();
        }
        return popularMap;
    }

    public static List<GridItem> getFavoriteItems(String user){
        favoriteItems = getFavoriteItemsMap().get(user);
        if(favoriteItems == null){
            favoriteItems = new ArrayList<GridItem>();
            getFavoriteItemsMap().put(user, favoriteItems);
        }
        return favoriteItems;
    }

    public static Map<String, List<GridItem>> getFavoriteItemsMap(){
        if(favoriteItemsMap == null){
            favoriteItemsMap = new HashMap<String, List<GridItem>>();
        }
        return favoriteItemsMap;
    }

    public static MovieItem getMovieFromCache(String movieId){
        if(idItemMap == null){
            idItemMap = new HashMap<String, MovieItem>();
        }
        return idItemMap.get(movieId);
    }

    public static void addMovieToCache(String movieId, MovieItem movieItem){
        if(idItemMap == null){
            idItemMap = new HashMap<String, MovieItem>();
        }
        idItemMap.put(movieId,movieItem);
    }

    public static Map<String,String> getTrailerFromCache(String movieId){
        if(idVideoMap == null){
            idVideoMap = new HashMap<String, Map<String,String>>();
        }
        return idVideoMap.get(movieId);
    }

    public static void addTrailerToCache(String movieId, Map<String,String> videoMap){
        if(idVideoMap == null){
            idVideoMap = new HashMap<String, Map<String,String>>();
        }
        idVideoMap.put(movieId, videoMap);
    }

    public static Map<String,String> getReviewsFromCache(String movieId){
        if(idReviewMap == null){
            idReviewMap = new HashMap<String, Map<String,String>>();
        }
        return idReviewMap.get(movieId);
    }

    public static void addReviewToCache(String movieId, Map<String,String> reviewMap){
        if(idReviewMap == null){
            idReviewMap = new HashMap<String, Map<String,String>>();
        }
        idReviewMap.put(movieId,reviewMap);
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static void setApiKey(String apiKey) {
        SharedPreferences.apiKey = apiKey;
    }

    public static Boolean isFavoriteItem(String user, String movieId){
        for(GridItem g: SharedPreferences.getFavoriteItems(user)){
            if(g.getId().equalsIgnoreCase(movieId))
                return true;
        }
        return false;
    }

    public static void initializeCache(String user){
        getPopularCache(user);
        getTopRatedCache(user);
        getFavoriteItems(user);
    }
}
