package com.anuragandroid.popularmovies.models;

/**
 * This class is Value Object class for the movie grid items
 */
public class GridItem {
    private String image;
    private String title;
    private String id;

    public GridItem() {
        super();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
