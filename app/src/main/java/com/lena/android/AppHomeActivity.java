package com.lena.android;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lena.android.databinding.AppActivityHomeBinding;
import com.lena.android.databinding.AppFunctionLayoutBinding;
import com.lena.android.service.KeepService;
import com.lena.android.service.MyFirebaseMessagingService;
import com.lena.android.utils.VerifyUtil;

import java.util.ArrayList;

public class AppHomeActivity extends ParentActivity {
    private AppActivityHomeBinding binding;
    private FunctionAdapter adapter;

    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.App_Theme_Default);
        EdgeToEdge.enable(this);
        binding = AppActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setStatusBarColorWithWhiteText(getColor(R.color.app_purple_200));

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {

                    }
        });

        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(MyFirebaseMessagingService.CHANNEL_ID, getString(R.string.app_fcm_name), NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);

            final NotificationChannel channel2 = new NotificationChannel(KeepService.CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel2);
        }
        manager.cancelAll();
        App.app.notificationIndex = 0;

        if (stateCheck()) {
            adapter = new FunctionAdapter(this);
            adapter.setOnClickListener(functionOnClickListener);
            final GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
            binding.functions.setAdapter(adapter);
            binding.functions.setLayoutManager(layoutManager);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, KeepService.class));
            } else {
                startService(new Intent(this, KeepService.class));
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isExecuting = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != launcher) {
            launcher.unregister();
            launcher = null;
        }
    }

    private boolean isExecuting = false;
    private final FunctionOnClickListener functionOnClickListener = new FunctionOnClickListener() {

        @Override
        public void onClick(@NonNull View view, int pos, @NonNull FunctionActivity functionActivity) {
            if (getClickAble()) {
                final Intent intent = new Intent(AppHomeActivity.this, functionActivity.parentActivityClass);
                if (null != launcher) {
                    isExecuting = true;
                    launcher.launch(intent);
                }
            }
        }

        @Override
        public void onLongClick(@NonNull View view, int pos, @NonNull FunctionActivity functionActivity) {
            if (getClickAble()) {
                if (AppShortVideosActivity.class.getName().equals(functionActivity.parentActivityClass.getName())) {
                    Intent intent = new Intent(AppHomeActivity.this, KeepService.class);
                    stopService(intent);
                }
            }
        }

        private boolean getClickAble() {
            return availableActivity() && !isExecuting;
        }
    };

    public final static class FunctionAdapter extends RecyclerView.Adapter<FunctionViewHolder> {
        public final Activity mActivity;
        public final ArrayList<FunctionActivity> functionActivities;

        private FunctionOnClickListener onClickListener;

        public FunctionAdapter(@NonNull final Activity activity) {
            this.mActivity = activity;
            this.functionActivities = new ArrayList<>();

            initActivities();
        }

        public void initActivities() {
            final FunctionActivity functionActivity = new FunctionActivity.Builder()
                    .setTitle(mActivity.getString(R.string.app_activity_chart))
                    .setDescription(mActivity.getString(R.string.app_activity_chart))
                    .setResourceId(R.drawable.app_chart)
                    .setParentActivityClass(AppChartActivity.class)
                    .create();
            this.functionActivities.add(functionActivity);

            final FunctionActivity functionActivity2 = new FunctionActivity.Builder()
                    .setTitle(mActivity.getString(R.string.app_activity_notification))
                    .setDescription(mActivity.getString(R.string.app_activity_notification))
                    .setResourceId(R.drawable.app_phone)
                    .setParentActivityClass(AppNotificationActivity.class)
                    .create();
            this.functionActivities.add(functionActivity2);

            final FunctionActivity functionActivity3 = new FunctionActivity.Builder()
                    .setTitle(mActivity.getString(R.string.app_activity_google_billing))
                    .setDescription(mActivity.getString(R.string.app_activity_google_billing))
                    .setResourceId(R.drawable.app_phone_google)
                    .setParentActivityClass(AppGoogleBillingActivity.class)
                    .create();
            // this.functionActivities.add(functionActivity3);


            final FunctionActivity functionActivity4 = new FunctionActivity.Builder()
                    .setTitle(mActivity.getString(R.string.app_activity_tiktok_demo))
                    .setDescription(mActivity.getString(R.string.app_activity_tiktok_demo))
                    .setResourceId(R.mipmap.app_ic_launcher)
                    .setParentActivityClass(AppShortVideosActivity.class)
                    .create();
            this.functionActivities.add(functionActivity4);
        }

        public void setOnClickListener(@NonNull final FunctionOnClickListener clickListener) {
            this.onClickListener = clickListener;
        }

        @NonNull
        @Override
        public FunctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FunctionViewHolder(AppFunctionLayoutBinding.inflate(mActivity.getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FunctionViewHolder holder, int position) {
            final FunctionActivity functionActivity = functionActivities.get(position);
            if (null != functionActivity) {
                holder.mBinding.functionIv.setImageResource(functionActivity.resourceId);
                holder.mBinding.functionTv.setText(functionActivity.title);

                holder.mBinding.getRoot().setOnClickListener(v -> {
                    if (null != onClickListener) onClickListener.onClick(v, holder.getAdapterPosition(), functionActivity);
                });
                holder.mBinding.getRoot().setOnLongClickListener(v -> {
                    if (null != onClickListener) onClickListener.onLongClick(v, holder.getAdapterPosition(), functionActivity);
                    return true;
                });
                holder.mBinding.getRoot().setClickable(true);
            } else {
                holder.mBinding.getRoot().setOnClickListener(null);
                holder.mBinding.getRoot().setOnLongClickListener(null);
                holder.mBinding.getRoot().setClickable(false);
            }
        }

        @Override
        public int getItemCount() {
            return functionActivities.size();
        }
    }

    public final static class FunctionViewHolder extends RecyclerView.ViewHolder {
        public final AppFunctionLayoutBinding mBinding;

        public FunctionViewHolder(@NonNull final AppFunctionLayoutBinding binding) {
            super(binding.getRoot());

            this.mBinding = binding;
        }
    }

    public final static class FunctionActivity {
        public final String title;
        public final String description;
        public final int resourceId;
        public final Class<? extends ParentActivity> parentActivityClass;

        private FunctionActivity(@NonNull final String title, @NonNull final String description, @DrawableRes final int resourceId, @NonNull final Class<? extends ParentActivity> parentActivityClass) {
            this.title = title;
            this.description = description;
            this.resourceId = resourceId;
            this.parentActivityClass = parentActivityClass;
        }

        public final static class Builder {
            public String bTitle;
            public String bDescription;
            public int bResourceId;
            public Class<? extends ParentActivity> bParentActivityClass;

            public Builder setTitle(@NonNull final String title) {
                this.bTitle = title;
                return this;
            }

            public Builder setDescription(final String description) {
                this.bDescription = description;
                return this;
            }

            public Builder setResourceId(@DrawableRes final int resourceId) {
                this.bResourceId = resourceId;
                return this;
            }

            public Builder setParentActivityClass(@NonNull final Class<? extends ParentActivity> parentActivityClass) {
                this.bParentActivityClass = parentActivityClass;
                return this;
            }

            public FunctionActivity create() throws IllegalArgumentException {
                if (!VerifyUtil.aNotEmptyString(bTitle)) {
                    throw new IllegalArgumentException("Invalid title.");
                }
                if (null == bParentActivityClass) {
                    throw new IllegalArgumentException("Invalid target Activity.");
                }
                if (!VerifyUtil.aNotEmptyString(bDescription)) {
                    bDescription = "";
                }
                if (bResourceId == 0) {
                    bResourceId = R.mipmap.app_ic_launcher;
                }
                return new FunctionActivity(bTitle, bDescription, bResourceId, bParentActivityClass);
            }
        }
    }

    public interface FunctionOnClickListener {
        void onClick(@NonNull final View view, final int pos, @NonNull final FunctionActivity functionActivity);
        void onLongClick(@NonNull final View view, final int pos, @NonNull final FunctionActivity functionActivity);
    }
}