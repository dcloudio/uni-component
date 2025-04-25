<template>
  <uni-video-element class="uni-video" ref="rootRef" @click="useControlsReturn.toggleControls" @fullscreenchange="useFullScreenReturn.fullscreenchange">
    <view
      class="uni-video-container"
      ref="containerRef"
      @click="useGestureReturn.onClick"
      @touchstart="useGestureReturn.onTouchstart"
      @touchend="useGestureReturn.onTouchend"
      @touchmove="useGestureReturn.onTouchmove"
    >
      <view class="uni-video-loading" v-if="props.showLoading && videoState.start && videoState.loading">
        <loadingCircle style="margin: auto" :speed="16" :size="54" color="#d3d3d3"></loadingCircle>
      </view>

      <native-view class="native-view-video" @init="onInit"></native-view>
      <!-- #ifdef APP-HARMONY -->
      <volume-panel v-if="inited" :volume-level="volumeState.currentVolumeLevel" :position-x="volumeState.positionX"></volume-panel>
      <!-- #endif -->

      <view v-show="videoState.start && danmuState.enable" ref="danmuRef" style="z-index: 0" class="uni-video-danmu"></view>

      <view
        v-show="fullscreenState.fullscreen && controlsState.controlsShow"
        class="uni-video-bar uni-video-title-bar uni-video-bar-full"
        @click="eventWrapper"
        :style="{ marginTop: `${safeTop}px` }"
      >
        <template v-if="fullscreenState.fullscreen">
          <!-- 返回按钮 -->
          <view class="uni-video-volume-button" @click="() => useFullScreenReturn.exitFullscreen()">
            <text class="uni-icon uni-video-back-button-icon">
              {{ ICON_BACK }}
            </text>
          </view>
          <view class="uni-video-title-view">
            <text class="uni-video-title">
              {{ title }}
            </text>
          </view>
        </template>
      </view>

      <view
        v-show="controlsState.controlsShow"
        :class="{
          'uni-video-bar': true,
          'uni-video-fullscreen-bar': fullscreenState.fullscreen,
          'uni-video-bar-full': true
        }"
        @click="eventWrapper"
      >
        <view class="uni-video-controls" @click="(event) => eventWrapper(event, useControlsReturn.controlsClick)">
          <template v-if="controlsState.controlsShow">
            <view v-if="props.showPlayBtn" class="uni-video-control-button" @click="useVideoReturn.toggle">
              <text v-if="!videoState.playing" class="uni-video-icon uni-video-control-button-icon">
                {{ ICON_PLAY }}
              </text>
              <text v-else class="uni-video-icon uni-video-control-button-icon">
                {{ ICON_PAUSH }}
              </text>
            </view>
            <view v-if="props.showProgress" class="uni-video-current-time">
              <text class="uni-video-current-time-text">
                {{ formatTime(videoState.currentTime) }}
              </text>
            </view>
          </template>
          <!-- 播放 进度条 -->
          <view v-show="props.showProgress" class="uni-video-progress-container" ref="progressRef" @click="useControlsReturn.clickProgress">
            <view
              :class="{
                'uni-video-progress': true,
                'uni-video-progress-progressing': progressing
              }"
            >
              <template v-if="controlsState.controlsShow">
                <view class="uni-video-progress-buffered" />
                <view :style="{ width: videoState.progress + '%' }" class="uni-video-progress-played" />
              </template>
              <view
                ref="ballRef"
                :style="{ left: videoState.progress + '%' }"
                :class="{
                  'uni-video-ball': true,
                  'uni-video-ball-progressing': progressing
                }"
              >
                <view
                  v-if="controlsState.controlsShow"
                  :class="{
                    'uni-video-inner': true,
                    // TODO 鸿蒙样式这父级 class 动态变化时，子级样式不会变化
                    'uni-video-ball-progressing': progressing
                  }"
                />
              </view>
            </view>
          </view>
          <view v-if="controlsState.controlsShow && props.showProgress" class="uni-video-duration">
            <text class="uni-video-current-time-text">
              {{ formatTime(Number(props.duration) || videoState.duration) }}
            </text>
          </view>
        </view>
        <template v-if="controlsState.controlsShow">
          <!-- 静音按钮 -->
          <view v-if="props.showMuteBtn" class="uni-video-volume-button" @click="() => useVideoReturn.mute(!videoState.muted)">
            <text v-if="videoState.muted" class="uni-video-icon uni-video-volume-button-icon">
              {{ ICON_VOLUME_MUTE }}
            </text>
            <text v-else class="uni-video-icon uni-video-volume-button-icon">
              {{ ICON_VOLUME }}
            </text>
          </view>
          <!-- 弹幕按钮 -->
          <view v-if="props.danmuBtn" class="uni-video-danmu-button" @click="useDanmuReturn.toggleDanmu">
            <text v-if="danmuState.enable" class="uni-video-icon uni-video-danmu-button-icon">
              {{ ICON_DANMU_ACTIVE }}
            </text>
            <text v-else class="uni-video-icon uni-video-danmu-button-icon">
              {{ ICON_DANMU }}
            </text>
          </view>
          <!-- 全屏按钮 -->
          <view v-if="props.showFullscreenBtn" class="uni-video-fullscreen" @click="() => useFullScreenReturn.toggleFullscreen(!fullscreenState.fullscreen)">
            <text v-if="!fullscreenState.fullscreen" class="uni-video-icon uni-video-fullscreen-icon">
              {{ ICON_FULLSCREEN }}
            </text>
            <text v-else class="uni-video-icon uni-video-fullscreen-icon">
              {{ ICON_FULLSCREEN_CLOSE }}
            </text>
          </view>
        </template>
      </view>

      <view v-if="controlsState.centerPlayBtnShow" class="uni-video-cover" @click="eventWrapper">
        <text class="uni-video-icon uni-video-cover-play-button" @click="($event) => eventWrapper($event, useVideoReturn.play)">
          {{ ICON_PLAY }}
        </text>
      </view>

      <view v-if="gestureState.gestureType !== 'none' || volumeState.showToast" class="uni-video-loading" @click="eventWrapper">
        <view
          v-if="gestureState.gestureType === 'brightness'"
          :class="{
            'uni-video-toast-container': true,
            'uni-video-toast-container-thin': gestureState.toastThin
          }"
          :style="{ marginTop: `${safeTop + 5}px` }"
        >
          <text v-show="!gestureState.toastThin" class="uni-video-icon uni-video-toast-icon">{{ ICON_BRIGHTNESS }}</text>
          <view class="uni-video-toast-draw" :style="{ width: `${gestureState.brightnessNew * 100}%` }"></view>
        </view>

        <view
          v-if="(volumeState.showToast || gestureState.gestureType === 'volume') && fullscreenState.fullscreen"
          :class="{
            'uni-video-toast-container': true,
            'uni-video-toast-container-thin': gestureState.toastThin
          }"
          :style="{ marginTop: `${safeTop + 5}px` }"
        >
          <text
            v-if="!gestureState.toastThin && volumeState.currentVolumeLevel > 0 && (volumeState.showToast || gestureState.gestureType === 'volume')"
            class="uni-video-icon uni-video-toast-icon"
          >
            {{ ICON_VOLUME }}
          </text>
          <text v-else-if="!gestureState.toastThin" class="uni-video-icon uni-video-toast-icon">{{ ICON_VOLUME_MUTE }}</text>
          <view class="uni-video-toast-draw" :style="{ width: `${(volumeState.currentVolumeLevel / volumeState.maxVolume) * 100}%` }"></view>
        </view>
      </view>

      <view
        :class="{
          'uni-video-toast': true,
          'uni-video-toast-progress': progressing
        }"
      >
        <view class="uni-video-toast-title">
          <text class="uni-video-toast-title-current-time uni-video-toast-title-text">{{ formatTime(gestureState.currentTimeNew) }}</text>
          <text class="uni-video-toast-title-text">/ {{ formatTime(Number(props.duration) || videoState.duration) }}</text>
        </view>
      </view>

      <view class="uni-video-slots" @click="eventWrapper">
        <slot></slot>
      </view>
    </view>
  </uni-video-element>
</template>

<script setup lang="ts">
  import loadingCircle from '../loading-circle/loading-circle.uvue';
  import { formatTime } from './utils';
  import {
    UniVideoTimeUpdateEvent,
    UniVideoFullScreenChangeEvent,
    UniVideoControlsToggleEvent,
    UniVideoFullScreenClickEvent,
    UniVideoProgressEvent,
    UniVideoEvent,
    UniVideoErrorEvent,
    Video,
    ObjectFit,
    BuilderVideoOptions,
    BuilderVideoParams,
    AudioManager,
    Brightness,
    UniVideoElement
  } from '@/uni_modules/uni-video';

  function eventWrapper(event: UniEvent, fn?: (event?: UniEvent) => void) {
    fn?.(event);
    event.stopPropagation();
  }

  const ICON_BACK = '\uE601';
  const ICON_PLAY = '\uea24';
  const ICON_PAUSH = '\uea25';
  const ICON_DANMU = '\uea26';
  const ICON_DANMU_ACTIVE = '\uea27';
  const ICON_FULLSCREEN_CLOSE = '\uea28';
  const ICON_FULLSCREEN = '\uea29';
  const ICON_VOLUME = '\uea30';
  const ICON_VOLUME_MUTE = '\uea31';
  const ICON_BRIGHTNESS = '\uea32';

  defineOptions({
    name: 'video',
    // @ts-ignore
    rootElement: {
      name: 'uni-video-element',
      class: UniVideoElement
    }
  });
  /* defineOptions({
    inheritAttrs: false
  });
  const $attrs = useAttrs(); */

  const emit = defineEmits<{
    play: [event: UniEvent];
    pause: [event: UniEvent];
    ended: [event: UniEvent];
    waiting: [event: UniEvent];
    timeupdate: [event: UniVideoTimeUpdateEvent];
    fullscreenchange: [event: UniVideoFullScreenChangeEvent];
    error: [event: UniVideoErrorEvent];
    progress: [event: UniVideoProgressEvent];
    fullscreenclick: [event: UniVideoFullScreenClickEvent];
    controlstoggle: [event: UniVideoControlsToggleEvent];
  }>();

  interface VideoProps {
    src: string;
    duration?: number;
    controls?: boolean;
    danmuList?: Array<Danmu>;
    danmuBtn?: boolean;
    enableDanmu?: boolean;
    autoplay?: boolean;
    loop?: boolean;
    muted?: boolean;
    objectFit?: ObjectFit;
    poster?: string;
    direction?: number;
    showProgress?: boolean;
    initialTime?: number;
    showFullscreenBtn?: boolean;
    pageGesture?: boolean;
    vslideGesture?: boolean;
    vslideGestureInFullscreen?: boolean;
    enableProgressGesture?: boolean;
    showPlayBtn?: boolean;
    showCenterPlayBtn?: boolean;
    showLoading?: boolean;
    showMuteBtn?: boolean;
    enablePlayGesture?: boolean;
    title?: string;
  }
  const props = withDefaults(defineProps<VideoProps>(), {
    controls: true,
    autoplay: false,
    muted: false,
    objectFit: 'contain',
    loop: false,
    showProgress: true,
    showFullscreenBtn: true,
    danmuBtn: false,
    danmuList: [],
    initialTime: 0,
    enableProgressGesture: true,
    showPlayBtn: true,
    showCenterPlayBtn: true,
    showMuteBtn: false,
    enablePlayGesture: false,
    pageGesture: false,
    vslideGesture: false,
    vslideGestureInFullscreen: true,
    direction: 90,
    showLoading: true,
    enableDanmu: false // TODO 弹幕功能
  });

  let brightness = new Brightness();
  let video: Video | null = null;
  const rootRef = ref<UniElement | null>(null);
  const containerRef = ref<UniElement | null>(null);
  const progressRef = ref<UniElement | null>(null);
  const ballRef = ref<UniElement | null>(null);
  const danmuRef = ref<UniElement | null>(null);
  const safeTop = ref<number>(0)
  const windowInfo = uni.getWindowInfo()

  const options: BuilderVideoOptions = {
    src: props.src,
    previewUri: props.poster
  };

  const instance = getCurrentInstance()
  const eventSeed = instance?.uid ?? Date.now()

  const useVideoReturn = useVideo();
  const videoState = useVideoReturn.state;

  const useFullScreenReturn = useFullscreen();
  const fullscreenState = useFullScreenReturn.state;

  const useDanmuReturn = useDanmu();
  const danmuState = useDanmuReturn.state;

  const useGestureReturn = useGesture();
  const gestureState = useGestureReturn.state;

  const useControlsReturn = useControls((currentTimeNew) => {
    gestureState.currentTimeNew = currentTimeNew;
  });
  const controlsState = useControlsReturn.state;

  const progressing = useProgressing();

  const useVolumeReturn = useVolume()
  const volumeState = useVolumeReturn.state

  const inited = ref<boolean>(false)
  function onInit(event: UniNativeViewInitEvent) {
    video = new Video(event.detail.element, {
      options,
      eventSeed,
      muted: props.muted,
      autoplay: props.autoplay,
      objectFit: props.objectFit,
      loop: props.loop
    } as BuilderVideoParams);
    inited.value = true
  }

  uni.$on(`__UNIVIDEO_ON_TIME_UPDATE_${eventSeed}`, (time: number) => {
    if (!videoState.pauseUpdatingCurrentTime) {
      videoState.currentTime = time;
    }
    emit('timeupdate', new UniVideoTimeUpdateEvent(rootRef.value!, time, videoState.duration));
    useDanmuReturn.updateDanmu();
  });

  // #region useVolume
  interface UseVolumeReturn {
    state: volumeState
    updateVolume(nextVolume: number): void
    clearChangeShowToast(): void
  }
  interface volumeState {
    minVolume: number
    maxVolume: number
    currentVolumeLevel: number
    showToast: boolean
    positionX?: number
  }
  function useVolume(): UseVolumeReturn {
    const audioManager: AudioManager = new AudioManager((volume) => {
      if (gestureState.gestureType === 'volume') return
      useShowToast()
      state.currentVolumeLevel = volume;
    });

    const state: volumeState = reactive({
      minVolume: audioManager.minVolume,
      maxVolume: audioManager.maxVolume,
      currentVolumeLevel: audioManager.currentVolume,
      showToast: false
    })

    let showToastTimer: number | null = null
    function changeShowToast() {
      if (showToastTimer != null) return
      showToastTimer = setTimeout(() => {
        state.showToast = false
        gestureState.toastThin = false
        showToastTimer = null
      }, 1000)
    }
    function clearChangeShowToast() {
        clearTimeout(showToastTimer)
        showToastTimer = null
    }
    function useShowToast() {
      state.showToast = true
      clearChangeShowToast()
      changeShowToast()
      useGestureReturn.changeToastThin()
    }

    watchEffect(() => {
      const isFullscreen = fullscreenState.fullscreen
      if (isFullscreen) {
        state.positionX = -100
      } else {
        state.positionX = undefined
      }
    })
    onBeforeUnmount(() => {
      audioManager.offVolumeChange();
      state.positionX = undefined
    });
    function updateVolume(nextVolume: number) {
      if (nextVolume > state.maxVolume) {
        nextVolume = state.maxVolume;
      }
      if (nextVolume < state.minVolume) {
        nextVolume = state.minVolume;
      }
      useShowToast()
      state.currentVolumeLevel = nextVolume;
    }

    return {
      updateVolume,
      clearChangeShowToast,
      state
    }
  }
  // #endregion

  // #region useVideo
  interface VideoState {
    start: boolean;
    src: string;
    playing: boolean;
    currentTime: number;
    duration: number;
    progress: number;
    buffered: number;
    muted: boolean;
    pauseUpdatingCurrentTime: boolean;
    doubleClick: boolean;
    loading: boolean;
  }
  interface UseVideoReturn {
    state: VideoState;
    play: (event?: UniPointerEvent) => void;
    pause: (event?: UniPointerEvent) => void;
    toggle: (event?: UniPointerEvent) => void;
    stop: (event?: UniPointerEvent) => void;
    seek: (value: number | string) => void;
    playbackRate: (rate: number) => void;
    mute: (muted: boolean) => void;
  }
  function useVideo(): UseVideoReturn {
    const muted = ref<boolean>(false);
    // @ts-ignore
    const state: VideoState = reactive({
      start: false,
      src: props.src,
      playing: false,
      currentTime: 0,
      duration: 0,
      progress: 0,
      buffered: 0,
      muted,
      pauseUpdatingCurrentTime: false,
      loading: true
    });
    watchEffect(() => {
      state.muted = props.muted === true;
    });

    watch(
      () => props.src,
      (src: string) => {
        state.playing = false;
        state.loading = true;
        state.currentTime = 0;

        video && (video.src = src);
      }
    );
    watch(
      () => state.buffered,
      (buffered) => {
        // TODO buffered 鸿蒙不支持
        /* trigger('progress', {} as Event, {
        buffered,
      }) */
      }
    );
    watch(
      () => props.muted,
      (muted: boolean) => {
        mute(muted);
      }
    );
    watch(
      () => props.poster,
      (poster: string) => {
        video && (video.poster = poster);
      }
    );
    watch(
      () => props.objectFit,
      (objectFit: ObjectFit) => {
        video && (video.objectFit = objectFit);
      }
    );
    watch(
      () => props.autoplay,
      (autoplay: boolean) => {
        video && (video.autoplay = autoplay);
      }
    );
    watch(
      () => props.loop,
      (loop: boolean) => {
        video && (video.loop = loop);
      }
    );
    uni.$on(`__UNIVIDEO_ON_PLAY_${eventSeed}`, () => {
      state.start = true;
      state.playing = true;
      controlsState.controlsVisible = true;
      emit('play', new UniVideoEvent(rootRef.value!, 'play'));
    });
    uni.$on(`__UNIVIDEO_ON_PAUSE_${eventSeed}`, () => {
      state.playing = false;
      controlsState.controlsVisible = true;
      emit('pause', new UniVideoEvent(rootRef.value!, 'pause'));
    });
    uni.$on(`__UNIVIDEO_ON_ERROR_${eventSeed}`, () => {
      state.playing = true;
      controlsState.controlsVisible = true;
      emit('error', new UniVideoErrorEvent(rootRef.value!));
    });
    uni.$on(`__UNIVIDEO_ON_ENDED_${eventSeed}`, () => {
      state.playing = false;
      controlsState.controlsVisible = true;
      emit('ended', new UniVideoEvent(rootRef.value!, 'ended'));
    });
    uni.$on(`__UNIVIDEO_ON_PREPARED_${eventSeed}`, (duration: number) => {
      state.loading = false;
      state.duration = duration;
      const initialTime = Number(props.initialTime) || 0;
      if (initialTime > 0) {
        seek(initialTime);
      } else if (typeof props.poster === 'undefined' || props.poster.length === 0) {
        video?.displayTheFirstFrame();
      }
    });

    const play = () => {
      state.start = true;
      video?.play();
    };
    const pause = () => {
      video?.pause();
    };
    function toggle() {
      if (state.playing) {
        pause();
      } else {
        play();
      }
    }
    function seek(position: number | string) {
      position = Number(position);
      if (typeof position === 'number' && !isNaN(position)) {
        video?.seek(position);
      }
    }
    function stop() {
      video?.stop();
    }
    function playbackRate(rate: number) {
      video && (video.playbackRate = rate);
    }
    function mute(muted: boolean) {
      state.muted = muted;
      video && (video.muted = muted);
    }

    return {
      state,
      play,
      pause,
      toggle,
      stop,
      seek,
      playbackRate,
      mute
    };
  }
  // #endregion useVideo end

  // #region useFullscreen
  interface FullscreenState {
    fullscreen: boolean;
  }
  interface UseFullscreenReturn {
    state: FullscreenState;
    requestFullscreen: (options?: RequestFullScreenOptions | null) => void;
    exitFullscreen: () => void;
    toggleFullscreen: () => void;
    fullscreenchange: (event: UniEvent) => void;
  }
  function useFullscreen(): UseFullscreenReturn {
    const pages = getCurrentPages();
    const page = pages[pages.length - 1];
    const state: FullscreenState = reactive({
      fullscreen: false,
    });
    function requestFullscreen(options?: RequestFullScreenOptions | null) {
      video?.requestFullscreen(options);
    }
    function exitFullscreen() {
      video?.exitFullscreen();
    }
    function toggleFullscreen() {
      if (state.fullscreen) {
        exitFullscreen();
      } else {
        requestFullscreen();
      }
    }
    onBeforeUnmount(exitFullscreen);
    function fullscreenchange(event: UniEvent) {
      setTimeout(() => {
        containerRef.value?.getBoundingClientRectAsync()?.then((rect) => {
          gestureState.containerRect.height = rect.height;
          gestureState.containerRect.width = rect.width;
          gestureState.containerRect.x = rect.x;
          gestureState.containerRect.y = rect.y;

          // @ts-ignore
          state.fullscreen = page?.fullscreenElement != null;
          const isVertical = rect.height > rect.width;

          if (state.fullscreen && isVertical) {
            safeTop.value = windowInfo.statusBarHeight
          } else {
            safeTop.value = 0
          }

          emit('fullscreenchange', new UniVideoFullScreenChangeEvent(rootRef.value!, state.fullscreen, isVertical ? 'vertical' : 'horizontal'));
        });
      }, 500);
    }
    return {
      state,
      requestFullscreen,
      exitFullscreen,
      toggleFullscreen,
      fullscreenchange
    };
  }
  // #endregion useFullscreen end

  // #region useControls
  interface ControlsState {
    seeking: boolean;
    touching: boolean;
    controlsTouching: boolean;
    centerPlayBtnShow: boolean;
    controlsShow: boolean;
    controlsVisible: boolean;
    toggleControlsTimer: number;
  }
  interface UseControlsReturn {
    state: ControlsState;
    clickProgress: (event: UniPointerEvent) => void;
    toggleControls: (event: UniPointerEvent) => void;
    autoHideStart: () => void;
    autoHideEnd: () => void;
    controlsClick: () => void;
  }
  function useControls(seeking?: (currentTimeNew: number) => void): UseControlsReturn {
    const centerPlayBtnShow = computed(() => props.showCenterPlayBtn && !videoState.start);
    const controlsVisible = ref(true);
    const controlsShow = computed(() => !centerPlayBtnShow.value && props.controls && controlsVisible.value);
    // @ts-ignore
    const state: ControlsState = reactive({
      seeking: false,
      touching: false,
      controlsTouching: false,
      centerPlayBtnShow,
      controlsShow,
      controlsVisible,
      toggleControlsTimer: 0
    });

    function clickProgress(event: UniPointerEvent) {
      if (event.target) {
        const clientRect = event.target.getBoundingClientRect();
        const w = clientRect.width;
        const x = event.x - clientRect.x;
        let progress = 0;
        if (x >= 0 && x <= w) {
          progress = x / w;
          useVideoReturn.seek(videoState.duration * progress);
        }
      }
    }
    function controlsClick() {
      clearTimeout(controlsState.toggleControlsTimer);
    }
    function toggleControls(event: UniPointerEvent) {
      if (fullscreenState.fullscreen) {
        emit('fullscreenclick', new UniVideoFullScreenClickEvent(rootRef.value!, event.screenX, event.screenY, gestureState.containerRect.width, gestureState.containerRect.height));
      }
      state.toggleControlsTimer = setTimeout(() => {
        state.controlsVisible = !state.controlsVisible;
        emit('controlstoggle', new UniVideoControlsToggleEvent(rootRef.value!, state.controlsVisible));
      }, gestureState.doubleClickTime);
    }
    let hideTiming: number | null;
    function autoHideStart() {
      hideTiming = setTimeout(() => {
        state.controlsVisible = false;
      }, 3000);
    }
    function autoHideEnd() {
      if (hideTiming) {
        clearTimeout(hideTiming);
        hideTiming = null;
      }
    }
    onBeforeUnmount(() => {
      if (hideTiming) {
        clearTimeout(hideTiming);
      }
    });
    watch(
      () => state.controlsShow && videoState.playing && !state.controlsTouching,
      (val: boolean) => {
        if (val) {
          autoHideStart();
        } else {
          autoHideEnd();
        }
      }
    );

    onMounted(() => {
      /* interface PassiveOptions {
        passive : boolean
      } */
      // const passiveOptions : PassiveOptions = { passive: false }
      let originX: number;
      let originY: number;
      let moveOnce = true;
      let originProgress: number;
      const ball = ballRef.value as UniElement;
      function touchmove(event: UniTouchEvent) {
        const toucher = event.touches[0];
        const pageX = toucher.pageX;
        const pageY = toucher.pageY;
        if (moveOnce && Math.abs(pageX - originX) < Math.abs(pageY - originY)) {
          touchend(event);
          return;
        }
        moveOnce = false;
        const progressEl = progressRef.value as UniElement;
        const w = progressEl.offsetWidth;
        let progress = originProgress + ((pageX - originX) / w) * 100;
        if (progress < 0) {
          progress = 0;
        } else if (progress > 100) {
          progress = 100;
        }
        videoState.progress = progress;
        seeking?.((videoState.duration * progress) / 100);
        state.seeking = true;
        event.preventDefault()
        event.stopPropagation();
      }
      function touchend(event: UniTouchEvent) {
        state.controlsTouching = false;
        if (state.touching) {
          ball.removeEventListener('touchmove', touchmove);
          if (!moveOnce) {
            event.preventDefault()
            event.stopPropagation();
            useVideoReturn.seek((videoState.duration * videoState.progress) / 100);
          }
          state.touching = false;
        }
      }
      ball.addEventListener('touchstart', (event: UniTouchEvent) => {
        state.controlsTouching = true;
        const toucher = event.touches[0];
        originX = toucher.pageX;
        originY = toucher.pageY;
        originProgress = videoState.progress;
        moveOnce = true;
        state.touching = true;
        ball.addEventListener('touchmove', touchmove);
      });
      ball.addEventListener('touchend', touchend);
      ball.addEventListener('touchcancel', touchend);
    });
    return {
      state,
      clickProgress,
      toggleControls,
      autoHideStart,
      autoHideEnd,
      controlsClick
    };
  }
  // #endregion

  // #region useDanmu
  interface DanmuState {
    enable: boolean;
  }
  interface UseDanmuReturn {
    state: DanmuState;
    toggleDanmu: () => void;
    updateDanmu: () => void;
    playDanmu: (danmu: Danmu) => void;
    sendDanmu: (danmu: Danmu) => void;
  }
  function useDanmu(): UseDanmuReturn {
    const state = reactive({
      enable: Boolean(props.enableDanmu)
    });
    let danmuIndex = {
      time: 0,
      index: -1
    };
    const danmuList: Danmu[] = Array.isArray(props.danmuList) ? JSON.parse(JSON.stringify(props.danmuList)) : [];
    danmuList.sort(function (a: Danmu, b: Danmu) {
      return (a.time || 0) - (b.time || 0);
    });
    function toggleDanmu() {
      state.enable = !state.enable;
    }
    function updateDanmu() {
      const currentTime = videoState.currentTime;
      const oldDanmuIndex = danmuIndex;
      const newDanmuIndex = {
        time: currentTime,
        index: oldDanmuIndex.index
      };
      if (currentTime > oldDanmuIndex.time) {
        for (let index = oldDanmuIndex.index + 1; index < danmuList.length; index++) {
          const element = danmuList[index];
          if (currentTime >= (element.time || 0)) {
            newDanmuIndex.index = index;
            if (videoState.playing && state.enable) {
              playDanmu(element);
            }
          } else {
            break;
          }
        }
      } else if (currentTime < oldDanmuIndex.time) {
        for (let index = oldDanmuIndex.index - 1; index > -1; index--) {
          const element = danmuList[index];
          if (currentTime <= (element.time || 0)) {
            newDanmuIndex.index = index - 1;
          } else {
            break;
          }
        }
      }
      danmuIndex = newDanmuIndex;
    }
    function playDanmu(danmu: Danmu) {
      if (!rootRef.value) return
      if (!danmu.text || danmu.text.length === 0 || danmuRef.value == null) {
        return;
      }

      // @ts-ignore
      const p = rootRef.value.uniPage.createElement('text');

      (p as UniTextElement).setAnyAttribute('value', danmu.text);

      p.style.setProperty('font-size', '14px');
      p.style.setProperty('line-height', '14px');
      p.style.setProperty('position', 'absolute');
      p.style.setProperty('color', '#000');
      p.style.setProperty('white-space', 'nowrap');
      p.style.setProperty('left', '100%');
      p.style.setProperty('transform', 'translateX(0%)');
      p.style.setProperty('bottom', `${Math.random() * 100}%`);
      p.style.setProperty('color', `${danmu.color}`);

      (danmuRef.value as UniElement).appendChild(p);
      p.animate(
        [
          { left: '100%', transform: 'translateX(0%)' },
          { left: '0%', transform: `translateX(-100%);` }
        ],
        {
          duration: 3000,
          easing: 'linear'
        }
      );
      setTimeout(function () {
        p.remove();
      }, 3000);
    }
    function sendDanmu(danmu: Danmu) {
      danmuList.splice(danmuIndex.index + 1, 0, {
        text: String(danmu.text),
        color: danmu.color,
        time: videoState.currentTime || 0
      });
    }
    uni.$on(`__UNIVIDEO_SEND_DANMU_${eventSeed}`, (res: UTSJSONObject) => {
      sendDanmu({
        text: res['text'] as string | null,
        color: res['color'] as string | null,
        time: res['time'] as number | null
      });
    });
    return {
      state,
      toggleDanmu,
      updateDanmu,
      playDanmu,
      sendDanmu
    };
  }
  // #endregion

  // #region useProgressing
  function useProgressing() {
    const progressing = computed(() => gestureState.gestureType === 'progress' || controlsState.touching);
    watch(progressing, (val: boolean) => {
      videoState.pauseUpdatingCurrentTime = val;
      controlsState.controlsTouching = val;
      if (gestureState.gestureType === 'progress' && val) {
        controlsState.controlsVisible = true;
      }
    });
    watch(
      [
        () => videoState.currentTime,
        () => {
          props.duration;
        }
      ],
      () => {
        videoState.progress = (videoState.currentTime / videoState.duration) * 100;
      }
    );
    watch(
      () => gestureState.currentTimeNew,
      (currentTimeNew: number) => {
        videoState.currentTime = currentTimeNew;
      }
    );

    return progressing;
  }
  // #endregion

  // #region useGesture
  type VideoGestureType = 'none' | 'stop' | 'volume' | 'progress' | 'brightness';
  interface GestureState {
    seeking: boolean;
    hasSought: boolean;
    toastThin: boolean;
    gestureType: VideoGestureType;
    volumeOld: number;
    volumeNew: number;
    currentTimeOld: number;
    currentTimeNew: number;
    doubleClickTime: number;
    brightnessOld: number;
    brightnessNew: number;
    containerRect: ContainerRect;
  }
  interface TouchStartOrigin {
    x: number;
    y: number;
  }
  interface useGestureReturn {
    state: GestureState;
    onTouchstart: (event: UniTouchEvent) => void;
    onTouchend: (event: UniTouchEvent) => void;
    onTouchmove: (event: UniTouchEvent) => void;
    onClick: (event: UniEvent) => void;
    changeProgress: (x: number) => void;
    changeToastThin: () => void;
  }
  interface ContainerRect {
    width: number;
    height: number;
    x: number;
    y: number;
  }
  function useGesture(): useGestureReturn {
    let changeToastThinTimer: number | null = null
    const containerRect: ContainerRect = reactive({
      width: 0,
      height: 200,
      x: 0,
      y: 0
    });
    const state: GestureState = reactive({
      seeking: false,
      hasSought: false,
      gestureType: 'none',
      volumeOld: 0,
      volumeNew: 0,
      currentTimeOld: 0,
      currentTimeNew: 0,
      doubleClickTime: 0,
      brightnessOld: brightness.getBrightness(),
      brightnessNew: brightness.getBrightness(),
      containerRect,
      toastThin: false
    });

    const changeToastThin = () => {
      if (state.gestureType !== 'none' && changeToastThinTimer != null) return
      changeToastThinTimer = setTimeout(() => {
        state.toastThin = true
      }, 500)
    }

    const touchStartOrigin: TouchStartOrigin = {
      x: 0,
      y: 0
    };
    function onTouchstart(event: UniTouchEvent) {
      const toucher = event.touches[0];
      touchStartOrigin.x = toucher.pageX;
      touchStartOrigin.y = toucher.pageY;
      state.gestureType = 'none';
      state.volumeOld = 0;
      event.stopPropagation();
      // state.currentTimeOld = state.currentTimeNew = 0
    }
    function onTouchmove(event: UniTouchEvent) {
      if (!videoState.start) return;

      function stop() {
        event.stopPropagation();
        event.preventDefault();
      }
      if (fullscreenState.fullscreen) {
        stop();
      } else if (props.pageGesture || props.vslideGesture) {
        stop();
      }

      const containerCenter = (containerRect.x + containerRect.width) / 2;
      const gestureType = state.gestureType;
      if (gestureType === 'stop') {
        return;
      }

      const toucher = event.touches[0];
      const pageX = toucher.pageX;
      const pageY = toucher.pageY;
      const origin = touchStartOrigin;

      switch (gestureType) {
        case 'progress':
          changeProgress(pageX - origin.x);
          state.seeking = true;
          break;
        case 'volume':
        changeVolume(pageY - origin.y);
          break;
        case 'brightness':
          changeBrightness(pageY - origin.y);
          break;
      }

      if (gestureType !== 'none') {
        return;
      }

      const absX = Math.abs(pageX - origin.x);
      const absY = Math.abs(pageY - origin.y);

      if (absX > absY) {
        if (!props.enableProgressGesture) {
          state.gestureType = 'stop';
          return;
        }
        state.gestureType = 'progress';
        state.currentTimeOld = state.currentTimeNew = videoState.currentTime;
      } else if (absX < absY) {
        if (props.pageGesture || props.vslideGesture || (fullscreenState.fullscreen && props.vslideGestureInFullscreen)) {
          if (origin.x > containerCenter) {
            changeToastThin()
            state.gestureType = 'volume';
            state.volumeOld = volumeState.currentVolumeLevel;
          } else {
            changeToastThin()
            useVolumeReturn.clearChangeShowToast()
            volumeState.showToast = false
            state.toastThin = false
            state.gestureType = 'brightness';
            state.brightnessOld = brightness.getBrightness();
          }
        } else {
          state.gestureType = 'stop';
        }
      }
    }
    function onTouchend(event: UniTouchEvent) {
      if (state.gestureType !== 'none') {
        state.hasSought = true;
        if (state.gestureType !== 'stop') {
          event.stopPropagation();
          event.preventDefault();
        }
      }
      if (state.gestureType === 'progress' && state.currentTimeOld !== state.currentTimeNew) {
        useVideoReturn.seek(state.currentTimeNew);
      }
      if (state.gestureType === 'volume' || state.gestureType === 'brightness') {
        state.gestureType === 'brightness' && (state.toastThin = false)
        clearTimeout(changeToastThinTimer)
        changeToastThinTimer = null
      }
      state.gestureType = 'none';
    }
    function changeProgress(x: number) {
      const duration = videoState.duration;
      let currentTimeNew = (x / 600) * duration + state.currentTimeOld;
      if (currentTimeNew < 0) {
        currentTimeNew = 0;
      } else if (currentTimeNew > duration) {
        currentTimeNew = duration;
      }
      state.currentTimeNew = currentTimeNew;
    }
    function onClick(event: UniEvent) {
      // NOTE 会触发父级的点击事件
      if (state.hasSought) {
        event.stopPropagation();
        state.hasSought = false;
      }
    }
    function getContainerRect() {
      containerRef.value?.getBoundingClientRectAsync()?.then((rect: DOMRect) => {
        containerRect.height = rect.height;
        containerRect.width = rect.width;
        containerRect.x = rect.x;
        containerRect.y = rect.y;
      });
    }
    onMounted(() => {
      let stop: (() => void) | null = null;
      getContainerRect()
      // 鸿蒙悬浮窗状态切换时触发
      uni.$on(`__UNIVIDEO_ON_WINDOW_STATUS_CHANGE_${eventSeed}`, () => {
        setTimeout(getContainerRect, 200)
      })
      watchEffect(() => {
        if (props.enablePlayGesture) {
          const res = useDoubleClick(containerRef.value!, (event) => {
            event.stopPropagation();
            clearTimeout(controlsState.toggleControlsTimer);
            useVideoReturn.toggle();
            controlsState.controlsVisible = true;
          });
          state.doubleClickTime = res.TIME_MAX;
          stop = res.stop;
        } else {
          state.doubleClickTime = 0;
          stop?.();
        }
      });
    });

    function changeVolume(y: number) {
      const valueOld = state.volumeOld;
      let value: number;
      if (typeof valueOld === 'number') {
        value = valueOld - (y / containerRect.height) * volumeState.maxVolume;
        if (value < volumeState.minVolume) {
          value = volumeState.minVolume;
        } else if (value > volumeState.maxVolume) {
          value = volumeState.maxVolume;
        }
        useVolumeReturn.updateVolume(value);
        state.volumeNew = value;
      }
    }

    function changeBrightness(y: number) {
      const valueOld = state.brightnessOld;
      let value: number;
      if (typeof valueOld === 'number') {
        value = valueOld - y / containerRect.height;
        if (value < 0) {
          value = 0;
        } else if (value > 1) {
          value = 1;
        }
        brightness.setBrightness(value);
        state.brightnessNew = value;
      }
    }

    return {
      state,
      onTouchstart,
      onTouchend,
      changeProgress,
      onTouchmove,
      onClick,
      changeToastThin
    };
  }
  // #endregion

  // #region useDoubleClick
  interface UseDoubleClickReturn {
    TIME_MAX: number;
    stop: () => void;
  }
  function useDoubleClick(element: UniElement | null, doubleClick: (event: UniMouseEvent) => void): UseDoubleClickReturn {
    if (!element)
      return {
        TIME_MAX: 0,
        stop: () => {}
      };
    let firstEvent: UniMouseEvent | null = null;
    let timeout: number;
    const TIME_MAX = 200;
    const PAGE_MAX = 44;
    const cb = element.addEventListener<UniMouseEvent, void>('click', (event: UniMouseEvent) => {
      clearTimeout(timeout);
      if (
        firstEvent &&
        Math.abs(event.pageX - firstEvent.pageX) <= PAGE_MAX &&
        Math.abs(event.pageY - firstEvent.pageY) <= PAGE_MAX &&
        event.timeStamp - firstEvent.timeStamp <= TIME_MAX
      ) {
        doubleClick(event);
        firstEvent = null;
        return;
      }
      firstEvent = event;
      timeout = setTimeout(() => {
        firstEvent = null;
      }, TIME_MAX);
    });
    return {
      TIME_MAX,
      stop: () => {
        element.removeEventListener('click', cb);
      }
    };
  }
  // #endregion
</script>

<style>
  .uni-video-slots {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    overflow: hidden;
    pointer-events: none;
  }

  .uni-video-icon {
    font-family: 'uni-video-icon';
    text-align: center;
  }

  .uni-icon {
    font-family: 'uni-icon';
    text-align: center;
  }

  .uni-video {
    width: 300px;
    height: 225px;
    display: flex;
    line-height: 0;
    overflow: hidden;
    position: relative;
  }

  .uni-video-gesture-brightness,
  .uni-video-gesture-volume,
  .uni-video-container {
    width: 100%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
    overflow: hidden;
    background-color: black;
  }

  .native-view-video {
    width: 100%;
    height: 100%;
  }

  .uni-video-cover {
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    /* background-color: rgba(1, 1, 1, 0.5); */
    align-items: center;
    z-index: 1;
  }

  .uni-video-cover-play-button {
    width: 75px;
    height: 75px;
    line-height: 75px;
    font-size: 60px;
    color: rgba(255, 255, 255, 0.5);
    /* cursor: pointer; */
  }

  .uni-video-cover-duration {
    color: #fff;
    font-size: 16px;
    line-height: 1;
    margin-top: 10px;
  }

  .uni-video-title-bar,
  .uni-video-bar {
    height: 44px;
    background-image: linear-gradient(-180deg, transparent, rgba(0, 0, 0, 0.5));
    overflow: hidden;
    position: absolute;
    bottom: 0;
    right: 0;
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 0 10px;
    z-index: 0;
  }

  .uni-video-title-bar,
  .uni-video-fullscreen-bar {
    padding: 0 20px;
  }

  .uni-video-title-bar {
    background-image: linear-gradient(to top, transparent, rgba(0, 0, 0, 0.5));
    top: 0;
  }

  .uni-video-bar.uni-video-bar-full {
    left: 0;
  }

  .uni-video-controls {
    height: 100%;
    margin: 0 4px;
    display: flex;
    flex-direction: row;
    align-items: center;
    flex-grow: 1;
    overflow: visible;
  }

  .uni-video-current-time,
  .uni-video-duration {
    height: 15px;
    line-height: 15px;
  }

  .uni-video-current-time-text {
    font-size: 15px;
    color: rgba(255, 255, 255, 0.5);
  }

  .uni-video-progress-container {
    flex-grow: 2;
    position: relative;
  }

  .uni-video-progress-played,
  .uni-video-progress-buffered {
    position: absolute;
    left: 0;
    top: 0;
    width: 0;
    height: 100%;
    background-color: rgba(255, 255, 255, 0.3);
  }

  .uni-video-progress-buffered {
    border-top-right-radius: 20px;
    border-bottom-right-radius: 20px;
    /* transition: width 0.1s; */
  }

  .uni-video-ball {
    /* NOTE uni-video-ball-progressing width height */
    width: 8px;
    height: 8px;
    padding: 14px;
    position: absolute;
    box-sizing: content-box;
    left: 0%;
    margin-left: -16px;
  }

  .uni-video-inner {
    width: 100%;
    height: 100%;
    background-color: #ffffff;
    border-radius: 4px;
    box-shadow: 0px 0px 2px #ccc;
  }

  .uni-video-ball.uni-video-ball-progressing {
    width: 16px;
    height: 16px;
  }

  .uni-video-inner.uni-video-ball-progressing {
    border-radius: 8px;
  }

  .uni-video-volume-button,
  .uni-video-danmu-button {
    width: 24px;
    height: 24px;
    white-space: nowrap;
    margin: 0 2px;
    /* cursor: pointer; */
  }

  .uni-video-volume-button .uni-video-volume-button-icon,
  .uni-video-danmu-button .uni-video-danmu-button-icon {
    line-height: 24px;
    color: rgba(255, 255, 255, 0.5);
  }

  .uni-video-volume-button .uni-video-volume-button-icon {
    font-size: 20px;
  }

  .uni-video-danmu-button .uni-video-danmu-button-icon {
    font-size: 24px;
  }

  .uni-video-control-button {
    width: 20px;
    height: 20px;
    padding: 0px 16px 0px 0px;
    margin-left: -4px;
    margin-right: -6px;
    box-sizing: content-box;
    /* cursor: pointer; */
  }

  .uni-video-control-button .uni-video-control-button-icon {
    font-size: 18px;
    line-height: 20px;
    color: rgba(255, 255, 255, 0.5);
  }

  .uni-video-progress-container {
    height: 100%;
    flex-grow: 2;
    position: relative;
    flex-direction: row;
    align-items: center;
    padding-left: 12px;
    padding-right: 12px;
    overflow: visible;
  }

  .uni-video-progress {
    width: 100%;
    /* NOTE uni-video-progress-progressing height */
    height: 4px;
    border-radius: 20px;
    position: relative;
    /* cursor: pointer; */
    display: flex;
    flex-direction: row;
    align-items: center;
    overflow: visible;
  }

  .uni-video-progress.uni-video-progress-progressing {
    height: 8px;
  }

  .uni-video-progress .uni-video-progress-played {
    background-color: #fff;
    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
  }

  .uni-video-progress-played,
  .uni-video-progress-buffered {
    position: absolute;
    left: 0;
    top: 0;
    width: 0;
    height: 100%;
  }

  .uni-video-progress-played {
    background-color: #ffffff;
  }

  .uni-video-progress-buffered {
    width: 100%;
    border-radius: 20px;
    background-color: rgba(255, 255, 255, 0.3);
    /* border-top-right-radius: 20px;
    border-bottom-right-radius: 20px; */
    /* transition: width 0.1s; */
  }

  .uni-video-danmu {
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    width: 100%;
    height: 100%;
    margin-top: 14px;
    margin-bottom: 44px;
    z-index: 10;
    overflow: visible;
  }

  /* .uni-video-danmu-item {
    font-size: 14px;
    line-height: 14px;
    position: absolute;
    color: #ffffff;
    white-space: nowrap;
    left: 100%;
    transform: translateX(0);
    transition-property: left, transform;
    transition-duration: 3s;
    transition-timing-function: linear;
  } */

  .uni-video-fullscreen {
    width: 32px;
    height: 32px;
    box-sizing: content-box;
    /* cursor: pointer; */
  }

  .uni-video-fullscreen-icon {
    font-size: 20px;
    line-height: 32px;
    color: rgba(255, 255, 255, 0.5);
  }

  .uni-video-toast {
    pointer-events: none;
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    display: none;
    overflow: visible;
  }

  .uni-video-toast.uni-video-toast-progress {
    display: flex;
    background-color: rgba(0, 0, 0, .5);
    border-radius: 6px;
    padding: 6px;
  }

  .uni-video-toast-title {
    flex-direction: row;
  }

  .uni-video-toast .uni-video-toast-title-text {
    color: rgba(255, 255, 255, 0.6);
    font-size: 24px;
    line-height: 24px;
  }

  .uni-video-toast .uni-video-toast-title-text.uni-video-toast-title-current-time {
    color: rgba(255, 255, 255, 0.9);
  }

  .uni-video-loading {
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    right: 0;
    pointer-events: none;
  }

  .uni-video-back-button-icon {
    font-size: 26px;
    line-height: 26px;
    color: rgba(255, 255, 255, 0.5);
  }

  .uni-video-title-view {
    margin-left: 10px;
  }

  .uni-video-title {
    font-size: 18px;
    line-height: 20px;
    color: rgba(255, 255, 255, 0.5);
  }

  .uni-video-toast-icon {
    font-size: 20px;
    position: absolute;
    left: 10px;
    color: #222;
    z-index: 1;
  }

  .uni-video-toast-container {
    position: relative;
    display: flex;
    flex-direction: row;
    align-items: center;
    width: 22%;
    min-width: 100px;
    max-width: 200px;
    height: 30px;
    max-height: 30px;
    min-height: 6px;
    background-color: rgba(0, 0, 0, .4);
    box-shadow: 0px 0px 2px #ccc;
    margin: 5px auto 0;
    border-radius: 30px;
    overflow: hidden;
    transition-property: height;
    transition-duration: 0.2s;
    transition-timing-function: ease-in-out;
    opacity: 0.6;
  }

  .uni-video-toast-draw {
    height: 100%;
    background-color: #fff;
  }

  .uni-video-toast-container-thin .uni-video-toast-icon {
    display: none;
  }

  .uni-video-toast-container.uni-video-toast-container-thin {
    height: 6px;
  }
</style>
