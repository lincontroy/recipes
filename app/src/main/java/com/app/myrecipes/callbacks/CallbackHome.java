package com.app.myrecipes.callbacks;

import com.app.myrecipes.models.Category;
import com.app.myrecipes.models.Recipe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CallbackHome implements Serializable {

    public String status = "";
    public List<Recipe> featured = new ArrayList<>();
    public List<Category> category = new ArrayList<>();
    public List<Recipe> recent = new ArrayList<>();
    public List<Recipe> videos = new ArrayList<>();

}
