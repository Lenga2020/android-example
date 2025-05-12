package com.lena.android.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lena.android.R;
import com.lena.android.databinding.AppFragmentViedoTagsBinding;
import com.lena.android.utils.Logger;
import com.lena.android.widget.MySnackBar;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Collections;

public class AppVideoTagsFragment extends ParentFragment {
    private AppFragmentViedoTagsBinding binding;

    private boolean enableSelfManager = false;
    private boolean iCanAutoPlay = false;
    private int type = 0;
    private VideoTagAdapter videoTagAdapter;

    private final static String ARG = "type";
    private final static String ARG_SELF_MANAGER = "arg_self_manager";
    public final static class Builder {
        private final Bundle bundle = new Bundle();

        public Builder setType(int type) {
            bundle.putInt(ARG, type);
            return this;
        }

        public Builder enableSelfManager() {
            bundle.putBoolean(ARG_SELF_MANAGER, true);
            return this;
        }

        public AppVideoTagsFragment build() {
            final AppVideoTagsFragment appVideoTagsFragment = new AppVideoTagsFragment();
            appVideoTagsFragment.setArguments(bundle);
            return appVideoTagsFragment;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (null != arguments) {
            type = arguments.getInt(ARG, 0);
            enableSelfManager = arguments.getBoolean(ARG_SELF_MANAGER, false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AppFragmentViedoTagsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null != getActivity()) {
            videoTagAdapter = new VideoTagAdapter(getActivity(), type);
            binding.videoTags.setAdapter(videoTagAdapter);
            binding.videoTags.setUserInputEnabled(true);
            binding.videoTags.setOffscreenPageLimit(1);
            binding.videoTags.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                private int last = -1;

                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (last == position) return;
                    last = position;
                    if (null != videoTagAdapter && iCanAutoPlay) {
                        videoTagAdapter.pauseAll();
                        videoTagAdapter.resumeCurrent(position);
                    }
                }
            });

            binding.videoTags.setCurrentItem(0);
            if (type == 1) {
                binding.tab4.setVisibility(View.GONE);
                binding.tab5.setVisibility(View.GONE);
            }

            binding.tab1.setOnClickListener(v -> binding.videoTags.setCurrentItem(0));
            binding.tab2.setOnClickListener(v -> binding.videoTags.setCurrentItem(1));
            binding.tab3.setOnClickListener(v -> binding.videoTags.setCurrentItem(2));
            if (2 == type) {
                binding.tab4.setOnClickListener(v -> binding.videoTags.setCurrentItem(3));
                binding.tab5.setOnClickListener(v -> binding.videoTags.setCurrentItem(4));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (enableSelfManager) {
            Logger.debug("Manager", getClass().getName() + "拥有自主控制权");
            gonnaResume();
        }
    }

    public void disable() {
        enableSelfManager = false;
    }

    public void gonnaResume() {
        iCanAutoPlay = true;
        if (null != videoTagAdapter) {
            videoTagAdapter.resumeCurrent(binding.videoTags.getCurrentItem());
        }
    }

    @Override
    public void onPause() {
        if (null != videoTagAdapter) {
            videoTagAdapter.pauseAll();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (null != videoTagAdapter) {
            videoTagAdapter.stopAll();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (null != videoTagAdapter) {
            videoTagAdapter.destroyAll();
        }
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (null != videoTagAdapter) videoTagAdapter.pauseAll();
        }
    }

    private final static class VideoTagAdapter extends FragmentStateAdapter {
        private final ArrayList<AppShortVideosFragment> fragments = new ArrayList<>();

        public VideoTagAdapter(@NonNull FragmentActivity fragmentActivity, int type) {
            super(fragmentActivity);

            if (type == 1) {
                type1();
            } else if (type == 2) {
                type2();
            }
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }

        public void resumeCurrent(int pos) {
            try {
                AppShortVideosFragment appShortVideosFragment = fragments.get(pos);
                if (null != appShortVideosFragment) {
                    appShortVideosFragment.gonnaPlay();
                }
            } catch (IndexOutOfBoundsException e) {
                Log.e("TAG", "resumeCurrent: ", e);
            }
        }

        public void pauseAll() {
            for (AppShortVideosFragment fragment : fragments) {
                fragment.pause();
            }
        }

        public void stopAll() {
            for (AppShortVideosFragment fragment : fragments) {
                fragment.stop();
            }
        }

        public void destroyAll() {
            for (AppShortVideosFragment fragment : fragments) {
                fragment.destroy();
            }
        }


        private void type1() {
            final ArrayList<String> urls1 = new ArrayList<>();
            for (int i = 1; i < 95; i++) {
                String url = "https://v.walktracker.fun/male/V" + i + ".mp4";
                urls1.add(url);
            }
            fragments.add(new AppShortVideosFragment.Builder().setUrls(urls1).disableSelfManager().build());

            final ArrayList<String> urls2 = new ArrayList<>();
            for (int i = 1; i < 95; i++) {
                String url = "https://v.walktracker.fun/female/V" + i + ".mp4";
                urls2.add(url);
            }
            fragments.add(new AppShortVideosFragment.Builder().setUrls(urls2).disableSelfManager().build());

            final ArrayList<String> urls3 = new ArrayList<>();
            for (int i = 1; i < 95; i++) {
                urls3.add("https://v.walktracker.fun/female/V" + i + ".mp4");
                urls3.add("https://v.walktracker.fun/male/V" + i + ".mp4");
            }
            fragments.add(new AppShortVideosFragment.Builder().setUrls(urls3).disableSelfManager().build());
        }

        private void type2() {
            final ArrayList<String> urls1 = new ArrayList<>();
            for (int i = 95; i >= 1; i--) {
                String url = "https://v.walktracker.fun/male/V" + i + ".mp4";
                urls1.add(url);
            }
            fragments.add(new AppShortVideosFragment.Builder().setUrls(urls1).disableSelfManager().build());

            final ArrayList<String> urls2 = new ArrayList<>();
            for (int i = 95; i >= 1; i--) {
                String url = "https://v.walktracker.fun/female/V" + i + ".mp4";
                urls2.add(url);
            }
            fragments.add(new AppShortVideosFragment.Builder().setUrls(urls2).disableSelfManager().build());

            final ArrayList<String> urls3 = new ArrayList<>();
            for (int i = 95; i >= 1; i--) {
                urls3.add("https://v.walktracker.fun/female/V" + i + ".mp4");
                urls3.add("https://v.walktracker.fun/male/V" + i + ".mp4");
            }
            fragments.add(new AppShortVideosFragment.Builder().setUrls(urls3).disableSelfManager().build());

            final ArrayList<String> urls4 = new ArrayList<>();
            for (int i = 1; i < 95; i++) {
                if (i % 2 == 0) {
                    urls4.add("https://v.walktracker.fun/female/V" + i + ".mp4");
                } else {
                    urls4.add("https://v.walktracker.fun/male/V" + i + ".mp4");
                }
            }
            fragments.add(new AppShortVideosFragment.Builder().setUrls(urls4).disableSelfManager().build());

            final ArrayList<String> urls5 = new ArrayList<>();
            for (int i = 95; i >= 1; i--) {
                urls5.add("https://v.walktracker.fun/female/V" + i + ".mp4");
                urls5.add("https://v.walktracker.fun/male/V" + i + ".mp4");
            }
            Collections.shuffle(urls5);
            fragments.add(new AppShortVideosFragment.Builder().setUrls(urls5).disableSelfManager().build());
        }
    }
}