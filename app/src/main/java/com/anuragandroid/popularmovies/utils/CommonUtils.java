package com.anuragandroid.popularmovies.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class contains the common utility methods
 */
public class CommonUtils {
    /**
     * This method converts the inputstream to String
     * @param stream
     * @return
     * @throws IOException
     */
    public static String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        // Close stream
        if (null != stream) {
            stream.close();
        }
        return result;
    }
}
