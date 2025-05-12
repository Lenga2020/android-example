package com.lena.android;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.lena.android.databinding.AppActivityShortVideosBinding;
import com.lena.android.fragment.AppShortEmptyFragment;
import com.lena.android.fragment.AppShortVideosFragment;
import com.lena.android.fragment.AppVideoTagsFragment;
import com.lena.android.vm.ShortLifecycleModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppShortVideosActivity extends ParentActivity {
    private AppActivityShortVideosBinding binding;
    private ShortLifecycleModel parentViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AppActivityShortVideosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        enableFullScreen();

        parentViewModel = new ViewModelProvider(this).get(ShortLifecycleModel.class);
        parentViewModel.liveLifeData.postValue(ShortLifecycleModel.Lifecycle.CREATE);

        final ArrayList<Fragment> fragmentArrayList = initFragments();
        binding.svTab1.setOnClickListener(v -> changeTab(fragmentArrayList, 0));
        binding.svTab2.setOnClickListener(v -> changeTab(fragmentArrayList, 1));
        binding.svTab3.setOnClickListener(v -> changeTab(fragmentArrayList, 2));
        binding.svTab4.setOnClickListener(v -> changeTab(fragmentArrayList, 3));

        changeTab(fragmentArrayList, 2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (null != parentViewModel) parentViewModel.liveLifeData.postValue(ShortLifecycleModel.Lifecycle.START);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != parentViewModel) parentViewModel.liveLifeData.postValue(ShortLifecycleModel.Lifecycle.RESUME);
        if (currentFragment instanceof AppShortVideosFragment) {
            ((AppShortVideosFragment) currentFragment).gonnaPlay();
        } else if (currentFragment instanceof AppVideoTagsFragment) {
            ((AppVideoTagsFragment) currentFragment).gonnaResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != parentViewModel) parentViewModel.liveLifeData.postValue(ShortLifecycleModel.Lifecycle.PAUSE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != parentViewModel) {
            parentViewModel.liveLifeData.postValue(ShortLifecycleModel.Lifecycle.STOP);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != parentViewModel) parentViewModel.liveLifeData.postValue(ShortLifecycleModel.Lifecycle.DESTROY);
    }

    private Fragment currentFragment;
    private void changeTab(List<Fragment> fragments, int tab) {
        if (!getActive()) return;
        if (fragments == null || fragments.isEmpty() || tab >= fragments.size()) return;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment targetFragment = fragments.get(tab);
        if (targetFragment == null || currentFragment == targetFragment) return;

        // 隐藏当前 Fragment
        if (currentFragment != null) {
            ft.hide(currentFragment);
            if (currentFragment instanceof AppShortVideosFragment) {
                ((AppShortVideosFragment) currentFragment).disable();
            } else if (currentFragment instanceof AppVideoTagsFragment) {
                ((AppVideoTagsFragment) currentFragment).disable();
            }
        }

        // 添加或显示目标 Fragment
        if (!targetFragment.isAdded()) {
            ft.add(binding.svContainers.getId(), targetFragment);
        } else {
            ft.show(targetFragment);
        }

        // 设置主导航 Fragment（推荐）
        ft.setPrimaryNavigationFragment(targetFragment);

        // 启动目标 Fragment 的逻辑
        if (targetFragment instanceof AppShortVideosFragment) {
            ((AppShortVideosFragment) targetFragment).gonnaPlay();
        } else if (targetFragment instanceof AppVideoTagsFragment) {
            ((AppVideoTagsFragment) targetFragment).gonnaResume();
        }

        currentFragment = targetFragment;
        ft.commitAllowingStateLoss();
    }


    private ArrayList<Fragment> initFragments() {
        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new AppVideoTagsFragment.Builder().setType(1).build());
        fragments.add(new AppVideoTagsFragment.Builder().setType(2).build());

        fragments.add(new AppShortEmptyFragment());

        final ArrayList<String> urls3 = new ArrayList<>();
        for (int i = 1; i < 50; i++) {
            urls3.add("https://v.walktracker.fun/female/V" + i + ".mp4");
            urls3.add("https://v.walktracker.fun/male/V" + i + ".mp4");
        }
        fragments.add(new AppShortVideosFragment.Builder().setUrls(urls3).build());
        return fragments;
    }
}