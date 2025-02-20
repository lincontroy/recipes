package com.app.myrecipes.activities;

import static com.app.myrecipes.config.AppConfig.ENABLE_RTL_MODE;
import static com.app.myrecipes.utils.Constant.RECIPES_GRID_2_COLUMN;
import static com.app.myrecipes.utils.Constant.RECIPES_GRID_3_COLUMN;
import static com.app.myrecipes.utils.Constant.RECIPES_LIST_BIG;
import static com.app.myrecipes.utils.Constant.RECIPES_LIST_SMALL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.myrecipes.BuildConfig;
import com.app.myrecipes.R;
import com.app.myrecipes.databases.prefs.AdsPref;
import com.app.myrecipes.databases.prefs.SharedPref;
import com.app.myrecipes.fragments.FragmentCategory;
import com.app.myrecipes.fragments.FragmentFavorite;
import com.app.myrecipes.fragments.FragmentHome;
import com.app.myrecipes.fragments.FragmentRecipes;
import com.app.myrecipes.utils.AdsManager;
import com.app.myrecipes.utils.AppBarLayoutBehavior;
import com.app.myrecipes.utils.Constant;
import com.app.myrecipes.utils.RtlViewPager;
import com.app.myrecipes.utils.Tools;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private long exitTime = 0;
    MyApplication myApplication;
    private BottomNavigationView navigation;
    private ViewPager viewPager;
    private RtlViewPager viewPagerRTL;
    private TextView title_toolbar;
    MenuItem prevMenuItem;
    int pager_number = 4;
    ImageButton btn_search;
    SharedPref sharedPref;
    CoordinatorLayout coordinatorLayout;
    ImageButton btn_overflow;
    public ImageButton btn_filter;
    private BottomSheetDialog mBottomSheetDialog;
    AdsPref adsPref;
    AdsManager adsManager;
    private AppUpdateManager appUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
//        adsPref = new AdsPref(this);
        if (ENABLE_RTL_MODE) {
            setContentView(R.layout.activity_main_rtl);
        } else {
            setContentView(R.layout.activity_main);
        }

        sharedPref = new SharedPref(this);
        Tools.setNavigation(this);

        AppBarLayout appBarLayout = findViewById(R.id.appbarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        myApplication = MyApplication.getInstance();

//        adsManager = new AdsManager(this);
//        adsManager.initializeAd();
//        adsManager.updateConsentStatus();
//        adsManager.loadBannerAd(BANNER_HOME);
//        adsManager.loadInterstitialAd(INTERSTITIAL_ON_RECIPES_LIST, adsPref.getInterstitialAdInterval());

        title_toolbar = findViewById(R.id.title_toolbar);
        btn_filter = findViewById(R.id.btn_filter);
        btn_overflow = findViewById(R.id.btn_overflow);
        btn_overflow.setOnClickListener(view -> showBottomSheetDialog());

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        navigation = findViewById(R.id.navigation);
        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        if (ENABLE_RTL_MODE) {
            initRTLViewPager();
        } else {
            initViewPager();
        }

        Tools.notificationOpenHandler(this, getIntent());
        Tools.getCategoryPosition(this, getIntent());
        Tools.getRecipesPosition(this, getIntent());

        initToolbarIcon();

        if (!BuildConfig.DEBUG) {
            appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
            inAppUpdate();
            inAppReview();
        }

    }

    public void showInterstitialAd() {
//        adsManager.showInterstitialAd();
    }

    public void initViewPager() {
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(pager_number);
        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_recent:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_category:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.navigation_favorite:
                    viewPager.setCurrentItem(3);
                    return true;
            }
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPager.getCurrentItem() == 0) {
                    title_toolbar.setText(getResources().getString(R.string.app_name));
                    showFilter(false);
                } else if (viewPager.getCurrentItem() == 1) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_recent));
                    showFilter(true);
                } else if (viewPager.getCurrentItem() == 2) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_category));
                    showFilter(false);
                } else if (viewPager.getCurrentItem() == 3) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_favorite));
                    showFilter(false);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void initRTLViewPager() {
        viewPagerRTL = findViewById(R.id.viewpager_rtl);
        viewPagerRTL.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPagerRTL.setOffscreenPageLimit(pager_number);
        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPagerRTL.setCurrentItem(0);
                    return true;
                case R.id.navigation_recent:
                    viewPagerRTL.setCurrentItem(1);
                    return true;
                case R.id.navigation_category:
                    viewPagerRTL.setCurrentItem(2);
                    return true;
                case R.id.navigation_favorite:
                    viewPagerRTL.setCurrentItem(3);
                    return true;
            }
            return false;
        });

        viewPagerRTL.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPagerRTL.getCurrentItem() == 0) {
                    title_toolbar.setText(getResources().getString(R.string.app_name));
                    showFilter(false);
                } else if (viewPagerRTL.getCurrentItem() == 1) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_recent));
                    showFilter(true);
                } else if (viewPagerRTL.getCurrentItem() == 2) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_category));
                    showFilter(false);
                } else if (viewPagerRTL.getCurrentItem() == 3) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_favorite));
                    showFilter(false);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void selectFragmentRecipe() {
        if (ENABLE_RTL_MODE) {
            viewPagerRTL.setCurrentItem(1);
        } else {
            viewPager.setCurrentItem(1);
        }
    }

    public void selectFragmentCategory() {
        if (ENABLE_RTL_MODE) {
            viewPagerRTL.setCurrentItem(2);
        } else {
            viewPager.setCurrentItem(2);
        }
    }

    public void showFilter(Boolean show) {
        if (show) {
            btn_filter.setVisibility(View.VISIBLE);
        } else {
            btn_filter.setVisibility(View.GONE);
        }
    }

    public class MyAdapter extends FragmentPagerAdapter {

        MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentHome();
                case 1:
                    return new FragmentRecipes();
                case 2:
                    return new FragmentCategory();
                case 3:
                    return new FragmentFavorite();
            }
            return null;
        }

        @Override
        public int getCount() {
            return pager_number;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void initToolbarIcon() {

        if (sharedPref.getIsDarkTheme()) {
            findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
            navigation.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        btn_search = findViewById(R.id.btn_search);
        btn_search.setOnClickListener(view -> new Handler().postDelayed(() -> startActivity(new Intent(getApplicationContext(), ActivitySearch.class)), 50));

    }

    @Override
    public void onBackPressed() {
        if (ENABLE_RTL_MODE) {
            if (viewPagerRTL.getCurrentItem() != 0) {
                viewPagerRTL.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        } else {
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        }
    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            showSnackBar(getString(R.string.press_again_to_exit));
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public void showSnackBar(String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @SuppressWarnings("rawtypes")
    private void showBottomSheetDialog() {

        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.lyt_bottom_sheet, null);

        FrameLayout lyt_bottom_sheet = view.findViewById(R.id.bottom_sheet);
        SwitchCompat switch_theme = view.findViewById(R.id.switch_theme);

        if (sharedPref.getIsDarkTheme()) {
            switch_theme.setChecked(true);
            lyt_bottom_sheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_dark));
        } else {
            switch_theme.setChecked(false);
            lyt_bottom_sheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_default));
        }

        switch_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.e("INFO", "" + isChecked);
            new Handler().postDelayed(() -> {
                sharedPref.setIsDarkTheme(isChecked);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                mBottomSheetDialog.dismiss();
            }, 300);
        });

        changeRecipesListViewType(view);

        view.findViewById(R.id.btn_privacy_policy).setOnClickListener(action -> {
            startActivity(new Intent(getApplicationContext(), ActivityPrivacyPolicy.class));
            mBottomSheetDialog.dismiss();
        });
        view.findViewById(R.id.btn_rate).setOnClickListener(action -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
            mBottomSheetDialog.dismiss();
        });
        view.findViewById(R.id.btn_more).setOnClickListener(action -> {
            String url = sharedPref.getMoreAppsUrl();
            if (url.startsWith("http") || url.startsWith("https") || url.startsWith("www")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPref.getMoreAppsUrl())));
            } else {
                Snackbar.make(coordinatorLayout, "Whoops, no more apps available at this time", Snackbar.LENGTH_SHORT).show();
            }
            mBottomSheetDialog.dismiss();
        });
        view.findViewById(R.id.btn_about).setOnClickListener(action -> {
            aboutDialog();
            mBottomSheetDialog.dismiss();
        });

        if (ENABLE_RTL_MODE) {
            if (sharedPref.getIsDarkTheme()) {
                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogDarkRTL);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogRTL);
            }
        } else {
            if (sharedPref.getIsDarkTheme()) {
                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogDark);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialog);
            }
        }

        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        BottomSheetBehavior bottomSheetBehavior = mBottomSheetDialog.getBehavior();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

    }

    public void aboutDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.custom_dialog_about, null);

        TextView txt_version = view.findViewById(R.id.txt_version);
        txt_version.setText(getString(R.string.sub_about_app_version) + " " + BuildConfig.VERSION_NAME);

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.dialog_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    private void changeRecipesListViewType(View view) {

        final TextView txt_recipes_view = view.findViewById(R.id.txt_current_recipes_list);
        if (sharedPref.getRecipesViewType() == RECIPES_LIST_SMALL) {
            txt_recipes_view.setText(getResources().getString(R.string.single_choice_list_small));
        } else if (sharedPref.getRecipesViewType() == RECIPES_LIST_BIG) {
            txt_recipes_view.setText(getResources().getString(R.string.single_choice_list_big));
        } else if (sharedPref.getRecipesViewType() == RECIPES_GRID_2_COLUMN) {
            txt_recipes_view.setText(getResources().getString(R.string.single_choice_grid_2));
        } else if (sharedPref.getRecipesViewType() == RECIPES_GRID_3_COLUMN) {
            txt_recipes_view.setText(getResources().getString(R.string.single_choice_grid_3));
        }

        view.findViewById(R.id.btn_switch_recipes).setOnClickListener(view2 -> {
            String[] items = getResources().getStringArray(R.array.dialog_recipes_list);
            int itemSelected = sharedPref.getRecipesViewType();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_setting_recipes)
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, position) -> {
                        sharedPref.updateRecipesViewType(position);

                        if (position == 0) {
                            txt_recipes_view.setText(getResources().getString(R.string.single_choice_list_small));
                        } else if (position == 1) {
                            txt_recipes_view.setText(getResources().getString(R.string.single_choice_list_big));
                        } else if (position == 2) {
                            txt_recipes_view.setText(getResources().getString(R.string.single_choice_grid_2));
                        } else if (position == 3) {
                            txt_recipes_view.setText(getResources().getString(R.string.single_choice_grid_3));
                        }

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("recipes_position", "recipes_position");
                        startActivity(intent);

                        dialogInterface.dismiss();
                    })
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    private void inAppReview() {
        if (sharedPref.getInAppReviewToken() <= 3) {
            sharedPref.updateInAppReviewToken(sharedPref.getInAppReviewToken() + 1);
        } else {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> {
                    }).addOnCompleteListener(complete -> {
                            }
                    ).addOnFailureListener(failure -> {
                    });
                }
            }).addOnFailureListener(failure -> Log.d("In-App Review", "In-App Request Failed " + failure));
        }
    }

    private void inAppUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo);
            }
        });
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, Constant.IMMEDIATE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                showSnackBar(getString(R.string.msg_cancel_update));
            } else if (resultCode == RESULT_OK) {
                showSnackBar(getString(R.string.msg_success_update));
            } else {
                showSnackBar(getString(R.string.msg_failed_update));
                inAppUpdate();
            }
        }
    }

}
