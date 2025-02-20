package com.app.myrecipes.adapters;

import static com.app.myrecipes.config.AppConfig.POST_PER_PAGE;
import static com.app.myrecipes.utils.Constant.NATIVE_AD_RECIPES_LIST;
import static com.app.myrecipes.utils.Constant.RECIPES_LIST_BIG;
import static com.app.myrecipes.utils.Constant.RECIPES_LIST_SMALL;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_MAX;
import static com.solodroid.ads.sdk.util.Constant.STARTAPP;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.myrecipes.R;
import com.app.myrecipes.config.AppConfig;
import com.app.myrecipes.databases.prefs.AdsPref;
import com.app.myrecipes.databases.prefs.SharedPref;
import com.app.myrecipes.models.Recipe;
import com.app.myrecipes.utils.Constant;
import com.balysv.materialripple.MaterialRippleLayout;
import com.solodroid.ads.sdk.format.NativeAdViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterRecipes extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_AD = 2;

    private List<Recipe> items;

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private Context context;
    private OnItemClickListener mOnItemClickListener;
    boolean scrolling = false;

    private SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Recipe obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterRecipes(Context context, RecyclerView view, List<Recipe> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
        lastItemViewDetector(view);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView category_name;
        public TextView recipe_title;
        public ImageView recipe_image;
        public ImageView thumbnail_video;
        public MaterialRippleLayout lyt_parent;

        OriginalViewHolder(View v) {
            super(v);
            category_name = v.findViewById(R.id.category_name);
            recipe_title = v.findViewById(R.id.recipe_title);
            recipe_image = v.findViewById(R.id.recipe_image);
            thumbnail_video = v.findViewById(R.id.thumbnail_video);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }

    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.load_more);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            SharedPref sharedPref = new SharedPref(context);
            if (sharedPref.getRecipesViewType() == RECIPES_LIST_SMALL) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_list_small, parent, false);
                vh = new OriginalViewHolder(v);
            } else if (sharedPref.getRecipesViewType() == RECIPES_LIST_BIG) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_list_big, parent, false);
                vh = new OriginalViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_grid, parent, false);
                vh = new OriginalViewHolder(v);
            }
        } else if (viewType == VIEW_AD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_native_ad_medium, parent, false);
            vh = new NativeAdViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Recipe p = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.category_name.setText(p.category_name);
            vItem.recipe_title.setText(p.recipe_title);

            if (p.content_type != null && p.content_type.equals("youtube")) {
                Picasso.get()
                        .load(Constant.YOUTUBE_IMAGE_FRONT + p.video_id + Constant.YOUTUBE_IMAGE_BACK_MQ)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.recipe_image);
            } else {
                Picasso.get()
                        .load(sharedPref.getApiUrl() + "/upload/" + p.recipe_image)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(vItem.recipe_image);
            }

            if (p.content_type != null && p.content_type.equals("Post")) {
                vItem.thumbnail_video.setVisibility(View.GONE);
            } else {
                vItem.thumbnail_video.setVisibility(View.VISIBLE);
            }

            vItem.lyt_parent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });

        } else if (holder instanceof NativeAdViewHolder) {

            final NativeAdViewHolder vItem = (NativeAdViewHolder) holder;
            final AdsPref adsPref = new AdsPref(context);
            final SharedPref sharedPref = new SharedPref(context);

            vItem.loadNativeAd(context,
                    adsPref.getAdStatus(),
                    NATIVE_AD_RECIPES_LIST,
                    adsPref.getAdType(),
                    adsPref.getBackupAds(),
                    adsPref.getAdMobNativeId(),
                    adsPref.getAppLovinNativeAdManualUnitId(),
                    sharedPref.getIsDarkTheme(),
                    AppConfig.LEGACY_GDPR,
                    "default"
            );

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

        if (getItemViewType(position) == VIEW_PROG || getItemViewType(position) == VIEW_AD) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Recipe recipe = items.get(position);
        if (recipe != null) {
            // Real Wallpaper should contain some data such as title, desc, and so on.
            // A Wallpaper having no title etc is assumed to be a fake Wallpaper which represents an Native Ad view
            if (recipe.recipe_title == null || recipe.recipe_title.equals("")) {
                return VIEW_AD;
            }
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }

    public void insertDataWithNativeAd(List<Recipe> items) {
        setLoaded();
        int positionStart = getItemCount();
        for (Recipe post : items) {
            Log.d("item", "TITLE: " + post.recipe_title);
        }

        if (items.size() >= Constant.NATIVE_AD_INDEX)
            items.add(Constant.NATIVE_AD_INDEX, new Recipe());

        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void insertData(List<Recipe> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {

        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        AdsPref adsPref = new AdsPref(context);
                        if (NATIVE_AD_RECIPES_LIST != 0) {
                            switch (adsPref.getAdType()) {
                                case ADMOB:
                                case STARTAPP:
                                case APPLOVIN:
                                case APPLOVIN_MAX: {
                                    int current_page = getItemCount() / (POST_PER_PAGE + 1); //posts per page plus 1 Ad
                                    onLoadMoreListener.onLoadMore(current_page);
                                    break;
                                }
                                default: {
                                    int current_page = getItemCount() / (POST_PER_PAGE);
                                    onLoadMoreListener.onLoadMore(current_page);
                                    break;
                                }
                            }
                        } else {
                            int current_page = getItemCount() / (POST_PER_PAGE);
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int last_idx = into[0];
        for (int i : into) {
            if (last_idx < i) last_idx = i;
        }
        return last_idx;
    }

}