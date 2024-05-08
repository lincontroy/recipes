package com.app.myrecipes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.app.myrecipes.R;

import java.util.Arrays;
import java.util.List;

public class Selection extends AppCompatActivity implements PurchasesUpdatedListener {

    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this::onPurchasesUpdated)
                .build();

        // Start connection to Google Play Billing client
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Billing client is ready
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Retry connecting to Billing client
            }
        });

        Button basicMonthlyButton = findViewById(R.id.btn_basic_monthly_subscription);
        Button premiumMonthlyButton = findViewById(R.id.btn_premium_monthly_subscription);
        Button basicYearlyButton = findViewById(R.id.btn_basic_yearly_subscription);
        Button premiumYearlyButton = findViewById(R.id.btn_premium_yearly_subscription);

        basicMonthlyButton.setOnClickListener(v -> initiatePurchase("weekly_250"));
        premiumMonthlyButton.setOnClickListener(v -> initiatePurchase("monthly_160"));
        basicYearlyButton.setOnClickListener(v -> initiatePurchase("sixmonth_180"));
        premiumYearlyButton.setOnClickListener(v -> initiatePurchase("yearly_250"));

    }


    private void initiatePurchase(String skuId) {
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(Arrays.asList(skuId))
                .setType(BillingClient.SkuType.SUBS);

        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    for (SkuDetails skuDetails : skuDetailsList) {
                        if (skuDetails.getSku().equals(skuId)) {
                            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build();
                            billingClient.launchBillingFlow(Selection.this, flowParams);
                        }
                    }
                }
            }
        });
    }


    @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            }
        }

        private void handlePurchase(Purchase purchase) {

            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                // TODO: Handle successful purchase
                // For example, grant access to premium content or features
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                // TODO: Handle pending state (for asynchronous operations)
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                // TODO: Handle unspecified state
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (billingClient != null && billingClient.isReady()) {
                billingClient.endConnection();
            }
        }


    }
