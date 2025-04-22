package com.lena.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.material.snackbar.Snackbar;
import com.lena.android.databinding.AppActivityGoogleBillingBinding;
import com.lena.android.databinding.AppItemGoogleProductBinding;
import com.lena.android.db.ConstantSharedPreferences;
import com.lena.android.utils.Logger;
import com.lena.android.widget.MySnackBar;
import com.lena.android.widget.MyToast;

import org.checkerframework.checker.units.qual.N;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Deprecated
public class AppGoogleBillingActivity extends ParentActivity {
    private final static String[] productIds = new String[]{"product_demo_1", "product_demo_2", "product_demo_3"};

    private AppActivityGoogleBillingBinding binding;

    private Handler handler;
    private ProgressDialog progressDialog;

    private BillingProductAdapter adapter;
    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = AppActivityGoogleBillingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.google_billing), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setStatusBarColorWithWhiteText(getColor(R.color.app_purple_200));

        handler = new Handler(Looper.getMainLooper());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        binding.billingToolbar.setTitle(getString(R.string.app_activity_google_billing));
        binding.billingToolbar.setTitleTextColor(getColor(R.color.app_white));
        binding.billingToolbar.setNavigationIcon(R.drawable.app_back);
        binding.billingToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        adapter = new BillingProductAdapter(this);
        adapter.setListener((view, productDetails, position) -> {
            final BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build();
            final BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(Collections.singletonList(productDetailsParams))
                    .build();
            if (null != billingClient) {
                // 这一步是发起购买，购买成功会执行onPurchasesUpdated种的核心方法handlePurchase(purchase);
                // 失败不执行handlePurchase(purchase)，会执行下方else
                final BillingResult billingResult = billingClient.launchBillingFlow(AppGoogleBillingActivity.this, billingFlowParams);
                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    MyToast.makeText(AppGoogleBillingActivity.this, getString(R.string.app_text_error) + billingResult.getResponseCode(), Snackbar.LENGTH_SHORT);
                }
            }
        });
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.billingProducts.setAdapter(adapter);
        binding.billingProducts.setLayoutManager(layoutManager);

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(billingClientStateListener);
    }

    @Override
    protected void onStop() {
        if (null != handler) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onStop();
    }

    private void handlePurchase(Purchase purchase) {
        final AcknowledgePurchaseParams acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        if (null != billingClient) {
            if (purchase.isAcknowledged()) {
                postToServer(purchase);
            } else {
                billingClient.acknowledgePurchase(acknowledgeParams, billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        // 确认购买成功
                        postToServer(purchase);
                        MySnackBar.show(AppGoogleBillingActivity.this, binding.getRoot(), getString(R.string.app_text_purchase_acknowledged), Snackbar.LENGTH_SHORT);
                    } else {
                        // 确认购买失败
                        MySnackBar.show(AppGoogleBillingActivity.this, binding.getRoot(), getString(R.string.app_text_purchase_fail) + billingResult.getResponseCode(), Snackbar.LENGTH_SHORT);
                    }
                });
            }
        }
    }

    private void postToServer(@NonNull final Purchase purchase) {
        String purchaseToken = purchase.getPurchaseToken();
        // App.app.googleBillingIds: App走onCreate的时候，会读取ConstantSharedPreferences的AcknowledgeIds
        if (App.app.googleBillingIds.add(purchaseToken)) {
            // App.app.googleBillingIds增加时再向ConstantSharedPreferences增加
            ConstantSharedPreferences.putAcknowledgeId(AppGoogleBillingActivity.this, purchaseToken);
            // 利用Retrofit发起服务器请求

        }
    }

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                MySnackBar.show(AppGoogleBillingActivity.this, binding.getRoot(), getString(R.string.app_text_purchase_cancel), Snackbar.LENGTH_SHORT);
            } else {
                MySnackBar.show(AppGoogleBillingActivity.this, binding.getRoot(), getString(R.string.app_text_purchase_fail) + billingResult.getResponseCode(), Snackbar.LENGTH_SHORT);
            }
        }
    };

    private final BillingClientStateListener billingClientStateListener = new BillingClientStateListener() {
        private boolean reconnect = false;

        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
            if (!getAlive()) return;
            binding.billingPb.setVisibility(View.GONE);
            if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                queryActivePurchases();
            } else {
                String message;
                switch (billingResult.getResponseCode()) {
                    case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                        // Google Play 服务不可用，请检查网络连接
                        message = getString(R.string.app_text_service_unavailable);
                        break;
                    case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                        // 无法连接到 Google Play，请确认已安装 Play 商店并登录账户
                        message = getString(R.string.app_text_play_unavailable);
                        break;
                    case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                        // 当前设备不支持此功能
                        message = getString(R.string.app_text_device_unsupported);
                        break;
                    default:
                        message = getString(R.string.app_text_error) + billingResult.getResponseCode();
                }
                MySnackBar.show(AppGoogleBillingActivity.this, binding.getRoot(), message, Snackbar.LENGTH_SHORT);
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
            // 连接Google Play失败， 3s后重连
            if (!reconnect && null != handler) {
                reconnect = true;
                handler.postDelayed(() -> billingClient.startConnection(billingClientStateListener), 3000L);
            }
        }

        private void queryActivePurchases() {
            if (billingClient == null || !billingClient.isReady()) return;
            final QueryPurchasesParams queryPurchasesParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build();
            if (null != billingClient) {
                runOnUiThread(() -> {
                    if (!progressDialog.isShowing()) progressDialog.show();
                });

                billingClient.queryPurchasesAsync(queryPurchasesParams, new PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                        if (!getAlive()) return;

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (!list.isEmpty()) for (Purchase purchase : list) {
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    // ???
                                    handlePurchase(purchase);
                                }
                            }
                        }

                        queryProductDetails();
                    }
                });
            }
        }

        private void queryProductDetails() {
            final List<QueryProductDetailsParams.Product> productList = getProductList(BillingClient.ProductType.SUBS, Arrays.asList(productIds));
            if (null != productList && !productList.isEmpty()) {
                final QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build();

                if (null != billingClient) {
                    billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
                        public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> productDetailsList) {
                            if (!getAlive()) return;
                            if (BillingClient.BillingResponseCode.OK == billingResult.getResponseCode()) {
                                if (App.app.isDebugMode()) {
                                    logProductInfo(productDetailsList);
                                }
                                if (getActive()) runOnUiThread(() -> adapter.setProducts(productDetailsList));
                            } else {
                                MySnackBar.show(AppGoogleBillingActivity.this, binding.getRoot(), "server is busy!", Snackbar.LENGTH_SHORT);
                            }

                            runOnUiThread(() -> {
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                            });
                        }

                        private void logProductInfo(List<ProductDetails> list) {
                            if (null != list && !list.isEmpty()) for (ProductDetails productDetail: list) {
                                if (null != productDetail) {
                                    Logger.debug("Product", "product id: " + productDetail.getProductId());
                                    Logger.debug("Product", "product title: " + productDetail.getTitle());
                                }
                            }
                        }
                    });
                    return;
                }
            }

            runOnUiThread(() -> {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            });
        }

        private List<QueryProductDetailsParams.Product> getProductList(@NonNull final @BillingClient.ProductType String type, final List<String> ids) {
            if (null == ids || ids.isEmpty()) return null;

            final List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
            for (String productId : ids) {
                QueryProductDetailsParams.Product productItem = QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(type)
                        .build();
                productList.add(productItem);
            }
            return productList;
        }
    };

    public final static class BillingProductAdapter extends RecyclerView.Adapter<BillingProductViewHolder> {
        private final Activity mActivity;
        private final ArrayList<ProductDetails> products;

        private BillingProductListener mListener;

        public BillingProductAdapter(@NonNull final Activity activity) {
            this.mActivity = activity;
            this.products = new ArrayList<>();
        }

        public void setProducts(List<ProductDetails> list) {
            if (!products.isEmpty()) {
                notifyItemRangeRemoved(0, products.size());
                products.clear();
            }
            if (null != list && !list.isEmpty()) {
                products.addAll(list);
                notifyItemRangeInserted(0, list.size());
            }
        }

        public void setListener(@NonNull BillingProductListener listener) {
            this.mListener = listener;
        }

        @NonNull
        @Override
        public BillingProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BillingProductViewHolder(AppItemGoogleProductBinding.inflate(mActivity.getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull BillingProductViewHolder holder, int position) {
            final ProductDetails productDetails = products.get(position);
            if (null != productDetails) {
                holder.mBinding.productTitle.setText(productDetails.getTitle());
                holder.mBinding.productId.setText(productDetails.getProductId());
                holder.mBinding.productName.setText(productDetails.getName());
                holder.mBinding.productDescription.setText(productDetails.getDescription());
                holder.mBinding.getRoot().setOnClickListener(v -> {
                    if (null != mListener) {
                        mListener.onClick(v, productDetails, holder.getAdapterPosition());
                    }
                });
                holder.mBinding.getRoot().setClickable(true);
            } else {
                holder.mBinding.productTitle.setText(mActivity.getString(R.string.app_name));
                holder.mBinding.productId.setText("");
                holder.mBinding.productName.setText("");
                holder.mBinding.productDescription.setText("");

                holder.mBinding.getRoot().setOnClickListener(null);
                holder.mBinding.getRoot().setClickable(false);
            }
        }

        @Override
        public int getItemCount() {
            return products.size();
        }
    }

    public final static class BillingProductViewHolder extends RecyclerView.ViewHolder {
        public final AppItemGoogleProductBinding mBinding;

        public BillingProductViewHolder(@NonNull final AppItemGoogleProductBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }

    public interface BillingProductListener {
        void onClick(@NonNull final View view, final ProductDetails productDetails, final int position);
    }
}