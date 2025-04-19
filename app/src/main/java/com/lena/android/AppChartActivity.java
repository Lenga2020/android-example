package com.lena.android;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lena.android.databinding.AppActivityChartBinding;
import com.lena.android.fragment.AppLineChart1Fragment;

public class AppChartActivity extends ParentActivity {

    private AppActivityChartBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = AppActivityChartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chart), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setStatusBarColorWithWhiteText(getColor(R.color.app_purple_200));

        binding.chartToolbar.setTitle(getString(R.string.app_activity_chart));
        binding.chartToolbar.setTitleTextColor(getColor(R.color.app_white));
        binding.chartToolbar.setNavigationIcon(R.drawable.app_back);
        binding.chartToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        setPage(new AppLineChart1Fragment());
    }

    public void setPage(@NonNull final Fragment fragment) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(binding.chartPager.getId(), fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }
}