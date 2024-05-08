package com.app.myrecipes.config;

public class AppConfig {

    //your Server Key obtained from the admin panel
    public static final String SERVER_KEY = "WVVoU01HTklUVFpNZVRsNVdsZE9jR05IVlhWa1IyeHFZVEpXTUZrelNtaGFibEYxV1RJNGRXRXlWWFpZTWtaM1kwZDRjRmt5UmpCaFZ6bDFVMWRTWmxreU9YUk1iVVozWTBNMU5XSXpWbmxqYlZacVlWaENiRmxZUW5jPQ==";

    //your Rest API Key obtained from the admin panel
    public static final String REST_API_KEY = "cda11Uib7PLEA8pjKehSVfY0vdHsXI269J3MlqcGatWZBmxOgR";

    //layout customization
    public static final boolean ENABLE_RECIPE_COUNT_ON_CATEGORY = true;
    public static final boolean FORCE_VIDEO_PLAYER_TO_LANDSCAPE = false;
    public static final boolean ENABLE_RECIPES_VIEW_COUNT = true;

    //if you use RTL Language e.g : Arabic Language or other, set true
    public static final boolean ENABLE_RTL_MODE = false;

    //load more for next list videos
    public static final int POST_PER_PAGE = 12;

    //GDPR EU Consent
    public static final boolean LEGACY_GDPR = true;

    //Url handler for recipe details
    public static final boolean OPEN_LINK_INSIDE_APP = false;

    //auto image slider duration on featured recipes
    public static final int AUTO_SLIDER_DURATION = 6000;

}