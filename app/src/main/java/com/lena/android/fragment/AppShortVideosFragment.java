package com.lena.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.material.snackbar.Snackbar;
import com.lena.android.App;
import com.lena.android.AppShortVideosActivity;
import com.lena.android.databinding.AppFragmentShortVideosBinding;
import com.lena.android.databinding.AppShortVideoItemBinding;
import com.lena.android.utils.Logger;
import com.lena.android.utils.VerifyUtil;
import com.lena.android.vm.ShortLifecycleModel;
import com.lena.android.widget.MySnackBar;

import java.util.ArrayList;

public class AppShortVideosFragment extends ParentFragment {
    private AppShortVideosActivity parentActivity;
    private ShortLifecycleModel parentViewModel;

    private boolean playAble = false; // 只有可见时才能播放
    private final ArrayList<String> videoUrlList = new ArrayList<>();

    private AppFragmentShortVideosBinding binding;
    private VideoAdapter videoAdapter;

    public final static String ARG = "video_url";
    public final static class Builder {
        private final Bundle bundle = new Bundle();

        public Builder setUrls(ArrayList<String> urls) {
            bundle.remove(ARG);
            if (null != urls && !urls.isEmpty()) {
                bundle.putStringArrayList(ARG, urls);
            }
            return this;
        }

        public AppShortVideosFragment build() {
            final AppShortVideosFragment shortVideosFragment = new AppShortVideosFragment();
            shortVideosFragment.setArguments(bundle);
            return shortVideosFragment;
        }
    }

    public AppShortVideosActivity getParentActivity() {
        if (getAlive()) {
            return null != parentActivity && parentActivity.availableActivity()? parentActivity: null;
        }
        return null;
    }

    public ShortLifecycleModel getParentViewModel() {
        return parentViewModel;
    }

    public CacheDataSource.Factory getCacheFactory() {
        if (null != App.app) {
            return new CacheDataSource.Factory()
                    .setCache(App.app.cache)
                    .setUpstreamDataSourceFactory(new DefaultDataSource.Factory(getParentActivity()))
                    .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
        }
        return null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppShortVideosActivity) {
            parentActivity = (AppShortVideosActivity) context;
            parentViewModel = new ViewModelProvider(parentActivity).get(ShortLifecycleModel.class);
        } else {
            parentActivity = null;
            parentViewModel = null;
        }
    }

    @Override
    public void onDetach() {
        playAble = false;
        setActive(false);
        setAlive(false);
        parentViewModel = null;
        parentActivity = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoUrlList.clear();

        final Bundle arguments = getArguments();
        if (null != arguments) {
            final ArrayList<String> stringArrayList = arguments.getStringArrayList(ARG);
            if (null != stringArrayList && !stringArrayList.isEmpty()) {
                videoUrlList.addAll(stringArrayList);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AppFragmentShortVideosBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final AppShortVideosActivity iParentActivity = getParentActivity();
        if (null != iParentActivity) {
            final ArrayList<MyVideo> myVideos = new ArrayList<>();
            if (!videoUrlList.isEmpty()) for (String url : videoUrlList.subList(0, Math.min(20, videoUrlList.size()))) {
                if (VerifyUtil.aValidWebUrl(url)) {
                    final MyVideo myVideo = new MyVideo(url);
                    myVideos.add(myVideo);
                }
            }

            videoAdapter = new VideoAdapter(iParentActivity, myVideos, new MyController() {
                private boolean isPlaying = false;
                private VideoHolder iViewHolder = null;

                private boolean reInitFlag = false;

                @Override
                public void setFlag() {
                    if (null != iViewHolder) {
                        Logger.debug("VideoY", iViewHolder.myVideo.video + " -- Stopped");
                    }
                    reInitFlag = true;
                }

                @Override
                public void onClicked(View view, int pos) {
                    if (isPlaying) {
                        isPlaying = false;
                        pause(false, false);
                    } else {
                        play();
                    }
                }

                @Override
                public void setViewHolder(VideoHolder videoHolder) {
                    reInitFlag = false;
                    iViewHolder = videoHolder;
                }

                private void init() {
                    if (null == iViewHolder) return;

                    final CacheDataSource.Factory cacheFactory = getCacheFactory();
                    if (getActive() && null != cacheFactory) {
                        final MediaItem mediaItem = MediaItem.fromUri(iViewHolder.myVideo.video);
                        final ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(getCacheFactory()).createMediaSource(mediaItem);
                        iViewHolder.reset();
                        iViewHolder.iPlayer.setMediaSource(mediaSource);
                        iViewHolder.iPlayer.prepare();

                        if (null != iViewHolder) {
                            Logger.debug("VideoY", iViewHolder.myVideo.video + " -- RePrepare");
                        }
                    }
                    isPlaying = false;
                }

                @Override
                public void play() {
                    if (null == iViewHolder) return;
                    if (playAble) {
                        if (reInitFlag) {
                            reInitFlag = false;
                            init();
                        }
                        iViewHolder.iPlayer.play();
                        isPlaying = true;

                        if (null != iViewHolder.myVideo && VerifyUtil.aNotEmptyString(iViewHolder.myVideo.video)) {
                            Logger.debug("Video", iViewHolder.myVideo.video + " -- 正在播放");
                        }
                    }
                }

                @Override
                public void pause(boolean stop, boolean release) {
                    if (null != iViewHolder) {
                        isPlaying = false;
                        iViewHolder.iPlayer.pause();
                        if (stop) {
                            iViewHolder.iPlayer.stop();
                        }
                        if (release) {
                            iViewHolder.iPlayer.release();
                        }
                    }
                }
            });

            binding.shortVideosList.setAdapter(videoAdapter);
            binding.shortVideosList.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                private final int code = 0x01;

                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (null == videoAdapter) return;

                    // 如果滑到新Item的时候，先把页面上的视频停掉，再去设置新视频
                    // 换句话来说，就是先把之前播放到视频停下来，再去播放新视频
                    videoAdapter.controller.pause(false, false);

                    final View iView = binding.shortVideosList.getChildAt(0);
                    if (iView instanceof RecyclerView) {
                        final RecyclerView.ViewHolder vh = ((RecyclerView) iView).findViewHolderForAdapterPosition(position);
                        if (vh instanceof VideoHolder) {
                            videoAdapter.controller.setViewHolder((VideoHolder) vh);
                            videoAdapter.controller.play();
                        }
                    }
                }
            });
            binding.shortVideosList.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        gonnaPlay();
    }

    public void gonnaPlay() {
        playAble = true;
        if (null != videoAdapter && null != videoAdapter.controller)
            videoAdapter.controller.play();
    }

    @Override
    public void onPause() {
        pause();
        super.onPause();
    }

    public void pause() {
        playAble = false;
        if (null != videoAdapter && null != videoAdapter.controller) {
            videoAdapter.controller.pause(false, false);
        }
    }

    @Override
    public void onStop() {
        stop();
        super.onStop();
    }

    public void stop() {
        playAble = false;
        if (null != videoAdapter && null != videoAdapter.controller) {
            videoAdapter.controller.pause(true, false);
            videoAdapter.controller.setFlag();
        }
    }

    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    public void destroy() {
        playAble = false;
        if (null != videoAdapter && null != videoAdapter.controller) {
            videoAdapter.controller.pause(true, true);
        }
    }


    public interface MyController {
        void setFlag();
        void setViewHolder(VideoHolder videoHolder);

        void play();
        void pause(boolean stop, boolean release);

        void onClicked(View view, int pos);
    }

    public final static class VideoHolder extends RecyclerView.ViewHolder {
        public final AppShortVideoItemBinding binding;
        public final StyledPlayerView playerView;

        private final ExoPlayer iPlayer;

        public MyVideo myVideo;

        public boolean showLoadingBar = true;
        private boolean showBuffering = false;

        private boolean preloading = true; // 让初始化LoadingBar 只显示一次，不要每加载一下就显示出来
        private boolean hiddenBar = false;

        public void reset() {
            preloading = true;
            hiddenBar = false;
            showBuffering = false;
        }

        public VideoHolder(@NonNull AppShortVideoItemBinding mBinding, @NonNull final Context context) {
            super(mBinding.getRoot());

            this.binding = mBinding;
            this.playerView = this.binding.videoItem;

            this.iPlayer = new ExoPlayer.Builder(context).build();
            this.iPlayer.addListener(new Player.Listener() {
                // 测试中
                @Override
                public void onPlaybackStateChanged(final int state) {
                    Player.Listener.super.onPlaybackStateChanged(state);
                    if (state == Player.STATE_BUFFERING && !showBuffering) {
                        // Logger.debug("Video2", "视频正在停下来播放, 显示Buffer");
                        showBuffering = true;
                    } else if (state == Player.STATE_READY) {
                        if (showBuffering) {
                            // Logger.debug("Video2", "视频继续播放了, 不显示Buffer");
                        }
                        showBuffering = false;
                    }
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                    if (context instanceof Activity) {
                        MySnackBar.show((Activity) context, binding.getRoot(), error.getMessage(), Snackbar.LENGTH_SHORT);
                    }
                    binding.loading.setVisibility(View.INVISIBLE);
                    Logger.error("Video", "Tag", error);
                }
            });
            this.iPlayer.addAnalyticsListener(new AnalyticsListener() {
                private final int code = 0x01;

                @Override
                public void onRenderedFirstFrame(@NonNull EventTime eventTime, @NonNull Object output, long renderTimeMs) {
                    AnalyticsListener.super.onRenderedFirstFrame(eventTime, output, renderTimeMs);
                    showLoadingBar = false;
                    binding.loading.setVisibility(View.INVISIBLE);
                    Logger.debug("Video", "首帧渲染成功");
                }
            });
            this.iPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            this.iPlayer.setPlayWhenReady(false);
            this.playerView.setPlayer(this.iPlayer);
        }

        public boolean canPreload() {
            return null != myVideo && VerifyUtil.aValidWebUrl(myVideo.video);
        }
    }

    public final static class VideoAdapter extends RecyclerView.Adapter<VideoHolder> {
        public final Activity activity;
        public final ArrayList<MyVideo> videos;

        public final MyController controller;

        public VideoAdapter(@NonNull final Activity mActivity, @NonNull final ArrayList<MyVideo> mVideos, @NonNull final MyController iController) {
            this.activity = mActivity;
            this.videos = mVideos;

            this.controller = iController;
        }

        public CacheDataSource.Factory getCacheFactory() {
            if (null != App.app) {
                return new CacheDataSource.Factory()
                        .setCache(App.app.cache)
                        .setUpstreamDataSourceFactory(new DefaultDataSource.Factory(activity))
                        .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
            }
            return null;
        }

        @NonNull
        @Override
        public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VideoHolder(AppShortVideoItemBinding.inflate(activity.getLayoutInflater(), parent, false), activity);
        }

        @Override
        public void onBindViewHolder(@NonNull final VideoHolder holder, final int position) {
            holder.myVideo = videos.get(position);
            holder.binding.loading.setVisibility(holder.showLoadingBar?View.VISIBLE: View.INVISIBLE);

            final CacheDataSource.Factory cacheFactory = getCacheFactory();
            if (null != cacheFactory && holder.canPreload()) {
                final MediaItem mediaItem = MediaItem.fromUri(holder.myVideo.video);
                final ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(getCacheFactory()).createMediaSource(mediaItem);
                holder.iPlayer.setMediaSource(mediaSource);
                holder.iPlayer.prepare();

                Logger.debug("Video-RV-Bind", "正在提前预缓存或者准备视频");
            }

            holder.binding.getRoot().setOnClickListener(v -> {
                controller.onClicked(v, holder.getAbsoluteAdapterPosition());
            });
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }
    }

    public final static class MyVideo {
        public final String video;

        public MyVideo(final String mVideoUrl) {
            this.video = mVideoUrl;
        }
    }
}