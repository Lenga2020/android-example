package com.lena.android;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.lena.android.api.PhoneManager;
import com.lena.android.databinding.AppActivityNotificationBinding;
import com.lena.android.model.Phone;
import com.lena.android.utils.VerifyUtil;
import com.lena.android.widget.MyToast;

import java.util.ArrayList;

public class AppNotificationActivity extends ParentActivity {

    private AppActivityNotificationBinding binding;

    private NotificationAdapter adapter;

    private ActivityResultLauncher<String> permissionApplier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = AppActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notification), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setStatusBarColorWithWhiteText(getColor(R.color.app_purple_200));

        binding.notificationToolbar.setTitle(getString(R.string.app_activity_notification));
        binding.notificationToolbar.setTitleTextColor(getColor(R.color.app_white));
        binding.notificationToolbar.setNavigationIcon(R.drawable.app_back);
        binding.notificationToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        final PhoneManager phoneManager = PhoneManager.getPhoneManager();
        final Phone mPhone = phoneManager.mPhone;
        if (null != mPhone) {
            binding.phoneIcon.setImageResource(mPhone.getPhoneLogo());
            binding.phoneModel.setText(mPhone.getPhoneModel());
            binding.phoneId.setText(mPhone.mIdentity);
        }

        adapter = new NotificationAdapter(this);
        adapter.clickListener = new NotificationSDKClickListener() {
            private boolean clickAble = true;
            @Override
            public void onClick(@NonNull View view, @NonNull NotificationSDK notificationSDK, int pos) {
                if (getActive() && clickAble) {
                    clickAble = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(AppNotificationActivity.this);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setOnDismissListener(dialog -> clickAble = true);
                    alertDialog.setTitle(notificationSDK.sdkName);
                    alertDialog.setMessage(notificationSDK.identity);
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.app_text_copy), new DialogInterface.OnClickListener() {
                        private boolean executeAble = true;

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (executeAble && getActive()) {
                                executeAble = false;
                                if (VerifyUtil.aNotEmptyString(notificationSDK.identity)) {
                                    final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    clipboardManager.setPrimaryClip(ClipData.newPlainText("value", notificationSDK.identity));
                                    MyToast.makeText(AppNotificationActivity.this, getString(R.string.app_text_copied), Toast.LENGTH_SHORT);
                                }
                                dialog.dismiss();
                            }
                        }
                    });
                    alertDialog.show();
                }
            }
        };
        binding.notificationSdks.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionApplier = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean o) {
                    if (null != o && o) {
                        // TODO: enable notification sdks
                        enableFirebaseNotification();
                    } else {
                        // TODO: disable notification sdks
                        disableFirebaseNotification();
                        MyToast.makeText(AppNotificationActivity.this, getString(R.string.app_text_not_grant_permission), Toast.LENGTH_SHORT);
                    }
                }
            });
            permissionApplier.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            // TODO: enable notification sdks
            enableFirebaseNotification();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != permissionApplier) {
            permissionApplier.unregister();
            permissionApplier = null;
        }
    }

    private void enableFirebaseNotification() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    final String token = task.isSuccessful()? task.getResult(): null;
                    if (getAlive()) runOnUiThread(() -> {
                        binding.firebaseFcmPb.setVisibility(View.GONE);
                        if (VerifyUtil.aNotEmptyString(token) && null != adapter) {
                            final NotificationSDK notificationSDK = new NotificationSDK(R.drawable.app_ic_firebase, "Firebase Cloud Messaging", token);
                            adapter.notificationSDKS.add(notificationSDK);
                            adapter.notifyDataSetChanged();
                        }
                    });
                });
    }

    private void disableFirebaseNotification() {
        if (getAlive()) {
            binding.firebaseFcmPb.setVisibility(View.GONE);
        }
    }

    public final static class NotificationSDK {
        public final int sdkIcon;
        public final String sdkName;
        public final String identity;

        public NotificationSDK(@DrawableRes final int sdkIcon, @NonNull final String sdkName, @NonNull final String identity) {
            if (!VerifyUtil.aNotEmptyString(sdkName))
                throw new IllegalArgumentException("error sdk name");
            if (!VerifyUtil.aNotEmptyString(identity))
                throw new IllegalArgumentException("error sdk identity");
            this.sdkIcon = sdkIcon;
            this.sdkName = sdkName;
            this.identity = identity;
        }
    }

    public final static class NotificationAdapter extends BaseAdapter {
        public final Activity activity;
        public final ArrayList<NotificationSDK> notificationSDKS;

        public NotificationSDKClickListener clickListener;

        public NotificationAdapter(Activity activity) {
            this.activity = activity;
            this.notificationSDKS = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return notificationSDKS.size();
        }

        @Override
        public Object getItem(int position) {
            return notificationSDKS.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder iViewHolder = null;
            if (null != convertView && convertView.getTag() instanceof ViewHolder) {
                iViewHolder = (ViewHolder) convertView.getTag();
            }
            if (iViewHolder == null) {
                convertView = LayoutInflater.from(activity).inflate(R.layout.app_item_notification_sdk, parent, false);
                final ImageView icon = convertView.findViewById(R.id.notification_sdk_icon);
                final TextView name = convertView.findViewById(R.id.notification_sdk_name);
                iViewHolder = new ViewHolder(icon, name);
                convertView.setTag(iViewHolder);
            }
            final NotificationSDK notificationSDK = notificationSDKS.get(position);
            if (null != notificationSDK) {
                iViewHolder.iconIv.setImageResource(notificationSDK.sdkIcon);
                iViewHolder.nameTv.setText(notificationSDK.sdkName);
                convertView.getRootView().setOnClickListener(v -> {
                    if (null != clickListener) clickListener.onClick(v, notificationSDK, position);
                });
                convertView.setClickable(true);
            } else {
                convertView.getRootView().setClickable(false);
                convertView.setOnClickListener(null);
            }
            return convertView;
        }

        public final static class ViewHolder {
            public final ImageView iconIv;
            public final TextView nameTv;

            public ViewHolder(@NonNull final ImageView icon, @NonNull final TextView name) {
                this.iconIv = icon;
                this.nameTv = name;
            }
        }
    }

    public interface NotificationSDKClickListener {
        void onClick(@NonNull final View view, @NonNull final NotificationSDK notificationSDK, final int pos);
    }
}