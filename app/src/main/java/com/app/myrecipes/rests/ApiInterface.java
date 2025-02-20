package com.app.myrecipes.rests;

import com.app.myrecipes.callbacks.CallbackAds;
import com.app.myrecipes.callbacks.CallbackCategories;
import com.app.myrecipes.callbacks.CallbackCategoryDetail;
import com.app.myrecipes.callbacks.CallbackHome;
import com.app.myrecipes.callbacks.CallbackRecipeDetail;
import com.app.myrecipes.callbacks.CallbackRecipes;
import com.app.myrecipes.callbacks.CallbackSetting;
import com.app.myrecipes.callbacks.CallbackUser;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Your Recipes App";

    @Headers({CACHE, AGENT})
    @GET("api.php?get_home")
    Call<CallbackHome> getHome(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_recent_recipes")
    Call<CallbackRecipes> getRecipesList(
            @Query("page") int page,
            @Query("count") int count,
            @Query("filter") String filter,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_recipe_detail")
    Call<CallbackRecipeDetail> getRecipeDetail(
            @Query("id") String id
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_category_index")
    Call<CallbackCategories> getAllCategories(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_category_posts")
    Call<CallbackCategoryDetail> getRecipesByCategory(
            @Query("id") int id,
            @Query("page") int page,
            @Query("count") int count,
            @Query("filter") String filter,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_search_results")
    Call<CallbackRecipes> getSearch(
            @Query("search") String search,
            @Query("count") int count,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_search_results_rtl")
    Call<CallbackRecipes> getSearchRTL(
            @Query("search") String search,
            @Query("count") int count,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_ads")
    Call<CallbackAds> getAds(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_user_token")
    Call<CallbackUser> getUserToken(
            @Query("user_unique_id") String user_unique_id
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_settings")
    Call<CallbackSetting> getSettings(
            @Query("api_key") String api_key
    );

}
