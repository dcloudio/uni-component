<template>
	<view>
		<slot />
	</view>
</template>

<script lang="uts">
	import UTSAndroid from 'io.dcloud.uts.UTSAndroid';
	import IjkPlayerView from 'io.dcloud.media.video.ijkplayer.media.IjkPlayerView';
	import OnPlayerChangedListener from 'io.dcloud.media.video.ijkplayer.OnPlayerChangedListener';
	import EnumPlayStrategy from 'io.dcloud.media.video.ijkplayer.option.EnumPlayStrategy';
	import MediaPlayerParams from 'io.dcloud.media.video.ijkplayer.media.MediaPlayerParams';

	import OnInfoListener from 'tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener';
	import IMediaPlayer from 'tv.danmaku.ijk.media.player.IMediaPlayer';
	import OnBufferingUpdateListener from 'tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener';

	import Activity from 'android.app.Activity';
	import FrameLayout from 'android.widget.FrameLayout';
	import ViewGroup from 'android.view.ViewGroup';
	import View from 'android.view.View';
	import TextUtils from 'android.text.TextUtils';
	import Locale from 'java.util.Locale';
	import JSONObject from 'org.json.JSONObject';

	import Glide from 'com.bumptech.glide.Glide';

	import { Danmu, RequestFullScreenOptions } from '../interface.uts';

	export default {
		name: "video-view",
		data() {
			return {
				rootView: null as FrameLayout | null,
				playerView: null as IjkPlayerView | null
			};
		},
		emits: ["play", "pause", "ended", "timeupdate", "fullscreenchange", "waiting", "error", "progress", "fullscreenclick", "controlstoggle"],
		props: {
			"src": { // 要播放视频的资源地址
				type: String,
				default: ""
			},
			"autoplay": { // 是否自动播放
				type: Boolean,
				default: false
			},
			"loop": { // 是否循环播放
				type: Boolean,
				default: false
			},
			"muted": { // 是否静音播放
				type: Boolean,
				default: false
			},
			"initialTime": { // 指定视频初始播放位置，单位为秒（s）
				type: Number,
				default: 0
			},
			"duration": { // 指定视频时长，单位为秒（s）
				type: Number,
				default: 0
			},
			"controls": { // 是否显示默认播放控件（播放/暂停按钮、播放进度、时间）
				type: Boolean,
				default: true
			},
			"danmuList": { // 弹幕列表
				type: Array<Danmu>,
				default: new Array<Danmu>()
			},
			"danmuBtn": { // 是否显示弹幕按钮，只在初始化时有效，不能动态变更
				type: Boolean,
				default: false
			},
			"enableDanmu": { // 是否展示弹幕，只在初始化时有效，不能动态变更
				type: Boolean,
				default: false
			},
			"pageGesture": { // 在非全屏模式下，是否开启亮度与音量调节手势
				type: Boolean,
				default: false
			},
			"direction": { // 设置全屏时视频的方向，不指定则根据宽高比自动判断。有效值为 0（正常竖向）, 90（屏幕逆时针90度）, -90（屏幕顺时针90度）
				type: Number,
				default: -90
			},
			"showProgress": { // 是否展示进度条，若不设置，宽度大于240时才会显示
				type: Boolean,
				default: true
			},
			"showFullscreenBtn": { // 是否显示全屏按钮
				type: Boolean,
				default: true
			},
			"showPlayBtn": { // 是否显示视频底部控制栏的播放按钮
				type: Boolean,
				default: true
			},
			"showCenterPlayBtn": { // 是否显示视频中间的播放按钮
				type: Boolean,
				default: true
			},
			"showLoading": { // 是否显示loading控件
				type: Boolean,
				default: true
			},
			"enableProgressGesture": { // 是否开启控制进度的手势
				type: Boolean,
				default: true
			},
			"objectFit": { // 当视频大小与 video 容器大小不一致时，视频的表现形式。contain：包含，fill：填充，cover：覆盖
				type: String,
				default: "contain"
			},
			"poster": { // 视频封面的图片网络资源地址，如果 controls 属性值为 false 则设置 poster 无效	
				type: String,
				default: ""
			},
			"showMuteBtn": { // 是否显示静音按钮
				type: Boolean,
				default: false
			},
			"title": { // 视频的标题，全屏时在顶部展示
				type: String,
				default: ""
			},
			"enablePlayGesture": { // 是否开启播放手势，即双击切换播放/暂停
				type: Boolean,
				default: false
			},
			"vslideGesture": { // 在非全屏模式下，是否开启亮度与音量调节手势（同 page-gesture）
				type: Boolean,
				default: false
			},
			"vslideGestureInFullscreen": { // 在全屏模式下，是否开启亮度与音量调节手势
				type: Boolean,
				default: true
			},
			"codec": { // 解码器选择，hardware：硬解码（硬解码可以增加解码算力，提高视频清晰度。少部分老旧硬件可能存在兼容性问题）；software：ffmpeg 软解码；
				type: String,
				default: "hardware"
			},
			"httpCache": { // 是否对 http、https 视频源开启本地缓存。缓存策略:开启了此开关的视频源，在视频播放时会在本地保存缓存文件，如果本地缓存池已超过100M，在进行缓存前会清空之前的缓存（不适用于m3u8等流媒体协议）
				type: Boolean,
				default: false
			},
			"playStrategy": { // 播放策略，0：普通模式，适合绝大部分视频播放场景；1：平滑播放模式（降级），增加缓冲区大小，采用open sl解码音频，避免音视频脱轨的问题，可能会降低首屏展现速度、视频帧率，出现开屏音频延迟等。 适用于高码率视频的极端场景；2： M3U8优化模式，增加缓冲区大小，提升视频加载速度和流畅度，可能会降低首屏展现速度。 适用于M3U8在线播放的场景
				type: Number,
				default: 0
			},
			"header": { // HTTP 请求 Header
				type: UTSJSONObject,
				default: new UTSJSONObject()
			}
		},
		watch: {
			"src": {
				handler(newValue : string, oldValue : string) {
					let path = this.getSrcPath(newValue);
					if (newValue != oldValue) { // 切换资源
						this.playerView?.switchVideoPath(newValue);
						this.reload();
					} else { // 初始化资源
						this.playerView?.setVideoPath(path);
					}
				},
				immediate: false
			},
			"autoplay": {
				handler(value : boolean) {
					// 临时方案
					setTimeout(() => { // 运行在非主线程中
						this.runOnUiThread(function () {
							if (value && this.playerView?.isPlaying() == false) {
								this.playerView?.start();
							}
						});
					}, 100);
				},
				immediate: false
			},
			"loop": {
				handler(_ : boolean) {
					// do nothing
				},
				immediate: false
			},
			"muted": {
				handler(value : boolean) {
					this.playerView?.setMutePlayer(value);
				},
				immediate: false
			},
			"initialTime": {
				handler(value : number) {
					if (value > 0) this.playerView?.seekTo(value as Int * 1000);
				},
				immediate: false
			},
			"duration": {
				handler(value : number) {
					if (value > 0) this.playerView?.setDuration(value as Int * 1000);
				},
				immediate: false
			},
			"controls": {
				handler(value : boolean) {
					this.playerView?.setControls(value);
				},
				immediate: false
			},
			"danmuList": {
				handler(value : Array<Danmu>) {
					this.playerView?.setmDanmuList(JSON.stringify(value));
				},
				immediate: false
			},
			"danmuBtn": {
				handler(value : boolean) {
					this.playerView?.enableDanmuBtn(value);
				},
				immediate: false
			},
			"enableDanmu": {
				handler(value : boolean) {
					this.playerView?.enableDanmaku(value);
				},
				immediate: false
			},
			"pageGesture": {
				handler(value : boolean) {
					this.playerView?.setPageGesture(value);
				},
				immediate: false
			},
			"direction": {
				handler(value : number) {
					this.playerView?.setDirection(value as Int);
				},
				immediate: false
			},
			"showProgress": {
				handler(value : boolean) {
					this.playerView?.setProgressVisibility(value);
				},
				immediate: false
			},
			"showFullscreenBtn": {
				handler(value : boolean) {
					this.playerView?.setFullscreenBntVisibility(value);
				},
				immediate: false
			},
			"showPlayBtn": {
				handler(value : boolean) {
					this.playerView?.setPlayBntVisibility(value);
				},
				immediate: false
			},
			"showCenterPlayBtn": {
				handler(value : boolean) {
					this.playerView?.setCenterPlayBntVisibility(value);
				},
				immediate: false
			},
			"showLoading": {
				handler(value : boolean) {
					this.playerView?.setLoadingVisibility(value);
				},
				immediate: false
			},
			"enableProgressGesture": {
				handler(value : boolean) {
					this.playerView?.setIsEnableProgressGesture(value);
				},
				immediate: false
			},
			"objectFit": {
				handler(value : string) {
					this.playerView?.setScaleType(value);
				},
				immediate: false
			},
			"poster": {
				handler(value : string) {
					if (!TextUtils.isEmpty(value)) {
						let thumb = this.playerView?.mPlayerThumb;
						if (thumb != null) Glide.with(this.$androidContext!).load(value).into(thumb);
					}
				},
				immediate: false
			},
			"showMuteBtn": {
				handler(value : boolean) {
					this.playerView?.isMuteBtnShow(value);
				},
				immediate: false
			},
			"title": {
				handler(value : string) {
					if (!TextUtils.isEmpty(value)) this.playerView?.setTitle(value);
				},
				immediate: false
			},
			"enablePlayGesture": {
				handler(value : boolean) {
					this.playerView?.setmIsDoubleTapEnable(value);
				},
				immediate: false
			},
			"vslideGesture": {
				handler(value : boolean) {
					this.playerView?.setPageGesture(value);
				},
				immediate: false
			},
			"vslideGestureInFullscreen": {
				handler(value : boolean) {
					this.playerView?.setIsFullScreenPageGesture(value);
				},
				immediate: false
			},
			"codec": {
				handler(value : string) {
					this.playerView?.isUseMediaCodec(value == "hardware");
				},
				immediate: false
			},
			"httpCache": {
				handler(value : boolean) {
					this.playerView?.setViewHttpCacheOpen(value);
				},
				immediate: false
			},
			"playStrategy": {
				handler(value : number) {
					let strategy : EnumPlayStrategy;
					switch (value) {
						case EnumPlayStrategy.PLAY_SMOOTH.getFlagVal():
							strategy = EnumPlayStrategy.PLAY_SMOOTH;
							break;
						case EnumPlayStrategy.START_QUICK.getFlagVal():
							strategy = EnumPlayStrategy.START_QUICK;
							break;
						case EnumPlayStrategy.M3U8_SMOOTH.getFlagVal():
							strategy = EnumPlayStrategy.M3U8_SMOOTH;
							break;
						case EnumPlayStrategy.DEFAULT.getFlagVal():
						default:
							strategy = EnumPlayStrategy.DEFAULT;
							break;
					}
					this.playerView?.setFlowStrategy(strategy);
				},
				immediate: false
			},
			"header": {
				handler(newValue : UTSJSONObject, oldValue : UTSJSONObject) {
					this.playerView?.setHeader(newValue.toString());
					if (newValue != oldValue) { // 切换header
						this.playerView?.switchVideoPath(this.getSrcPath(this.src)); // 需要重新加载
						this.reload();
					}
				},
				immediate: false
			}
		},
		NVLoad() : FrameLayout {
			this.rootView = new FrameLayout(this.$androidContext!);
			this.playerView = new IjkPlayerView(this.$androidContext);
			this.rootView?.addView(this.playerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			return this.rootView!;
		},
		NVLoaded() {
			this.playerView?.init();
			this.playerView?.setPlayerRootView(this.$el);
			this.playerView?.setOnPlayerChangedListener(new OnPlayerChangedListenerImpl(this));
			this.playerView?.setOnInfoListener(new OnInfoListenerImpl(this, this.playerView!));
			this.playerView?.setOnBufferingUpdateListener(new OnBufferingUpdateListenerImpl(this));
		},
		NVUnloaded() { // 资源回收
			this.playerView?.onDestroy();
			this.playerView = null;
		},
		expose: ['play', 'pause', 'seek', 'requestFullScreen', 'exitFullScreen', 'stop', 'hide', 'show', 'close', 'sendDanmu', 'playbackRate'],
		methods: {
			/**
			 * 播放视频
			 */
			play: function () {
				this.runOnUiThread(function () {
					this.playerView?.start();
				});
			},
			/**
			 * 暂停视频
			 */
			pause: function () {
				this.runOnUiThread(function () {
					this.playerView?.pause();
				});
			},
			/**
			 * 跳转到指定位置
			 * @param pos 跳转到的位置，单位：秒（s）
			 */
			seek: function (pos : number) {
				this.playerView?.seekTo((pos as Int) * 1000);
			},
			/**
			 * 切换到全屏
			 * @param direction 视频方向，0（正常竖向）, 90（屏幕逆时针90度）, -90（屏幕顺时针90度）
			 */
			requestFullScreen: function (options : RequestFullScreenOptions | null) {
				this.runOnUiThread(function () {
					let direction = -90;
					if (options != null) {
						direction = options.direction as Int;
					}
					this.playerView?.fullScreen(direction as Int);
				});
			},
			/**
			 * 退出全屏
			 */
			exitFullScreen: function () {
				this.runOnUiThread(function () {
					this.playerView?.exitFullScreen();
				});
			},
			/**
			 * 停止播放视频
			 */
			stop: function () {
				this.runOnUiThread(() => {
					this.playerView?.stop();
				});
			},
			/** 
			 * 隐藏视频播放控件
			 */
			hide: function () {
				this.runOnUiThread(function () {
					this.$el?.setVisibility(View.INVISIBLE);
				});
			},
			/**
			 * 显示视频播放控件
			 */
			show: function () {
				this.runOnUiThread(function () {
					this.$el?.setVisibility(View.VISIBLE);
				});
			},
			/**
			 * 关闭视频播放控件
			 */
			close: function () {
				this.runOnUiThread(function () {
					this.playerView?.stop();
					this.playerView?.onDestroy();
					this.playerView = null;
				});
			},
			/**
			 * 发送弹幕
			 * @param data 弹幕数据
			 */
			sendDanmu: function (danmu : Danmu) {
				this.runOnUiThread(function () {
					const data = new JSONObject();
					data.put('text', danmu.text);
					data.put('color', danmu.color);
					this.playerView?.sendDanmaku(data, true);
				});
			},
			/**
			 * 设置倍速播放
			 * @param rate 播放的倍率
			 */
			playbackRate: function (rate : number) {
				this.playerView?.playbackRate(rate.toString());
			},
			/**
			 * 内部函数
			 * 切换src、header后重新加载
			 */
			reload: function () {
				this.runOnUiThread(function () {
					this.playerView?.setDuration(this.duration as Int * 1000);
					this.playerView?.seekTo(this.initialTime as Int * 1000);
					this.playerView?.setMutePlayer(this.playerView?.isMutePlayer() == true);
					this.playerView?.clearDanma();
					this.playerView?.enableDanmaku(this.enableDanmu)
					if (this.autoplay) this.playerView?.start();
				});
			},
			/**
			 * 内部函数
			 * 获取资源路径
			 */
			getSrcPath: function (src : string) : string {
				if (src.startsWith("https://") || src.startsWith("http://")) { // 网络地址
					return src;
				} else { // 本地地址
					return UTSAndroid.getResourcePath(src);
				}
			},
			/**
			 * 内部函数
			 * runnable切换到UI线程执行
			 */
			runOnUiThread: function (runnable : () => void) {
				(this.$androidContext as Activity).runOnUiThread(new MainRunnable(runnable));
			}
		}
	}

	/**
	 * 切换到主线程
	 */
	class MainRunnable implements Runnable {

		private runnable : (() => void) | null;

		constructor(runnable : (() => void) | null) {
			this.runnable = runnable;
		}

		override run() : void {
			this.runnable?.invoke();
		}
	}

	class OnPlayerChangedListenerImpl implements OnPlayerChangedListener {

		private comp : UTSContainer<FrameLayout>;

		constructor(comp : UTSContainer<FrameLayout>) {
			super();
			this.comp = comp;
		}

		override onChanged(type : String, msg : String) : void {
			this.comp.$emit(type, new Map<string, any>().set("detail", msg));
			// if (type == "fullscreenchange") {
			// 	if (playerView?.isFullscreen() == true) {
			// 		let container = rootView?.getChildAt(1);
			// 		if (container == null) return;
			// 		setTimeout(() => {
			// 			rootView?.removeView(container);
			// 			playerView?.addView(container);
			// 			container?.bringToFront();
			// 		}, 100);
			// 	} else {
			// 		let container = playerView?.getChildAt(1);
			// 		if (container == null) return;
			// 		setTimeout(() => {
			// 			playerView?.removeView(container);
			// 			rootView?.addView(container);
			// 			container?.bringToFront();
			// 		}, 100);
			// 	}
			// }
		}
	}

	class OnInfoListenerImpl implements OnInfoListener {

		private comp : UTSContainer<FrameLayout>;
		private playerView : IjkPlayerView;

		constructor(comp : UTSContainer<FrameLayout>, playerView : IjkPlayerView) {
			super();
			this.comp = comp;
			this.playerView = playerView;
		}

		override onInfo(iMediaPlayer : IMediaPlayer | null, status : Int, extra : Int) : boolean {
			switch (status) {
				case MediaPlayerParams.STATE_COMPLETED:
					if ((this.comp as VideoViewComponent).loop) {
						let initialTime = (this.comp as VideoViewComponent).initialTime as Int;
						if (initialTime > 0) this.playerView.seekTo(initialTime * 1000);
						this.playerView.start();
					}
					this.comp.$emit("ended");
					break;
				case MediaPlayerParams.STATE_PLAYING:
					this.comp.$emit("play");
					break;
				case MediaPlayerParams.STATE_PAUSED:
					this.comp.$emit("pause");
					break;
				case MediaPlayerParams.STATE_ERROR:
					this.comp.$emit("error");
					break;
				case MediaPlayerParams.STATE_PREPARING:
				case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
					this.comp.$emit("waiting");
					break;
				case MediaPlayerParams.STATE_SEEKCOMPLETE:
					this.comp.$emit("seekcomplete", new Map<string, string>().set("detail", String.format(Locale.US, "{'position':%d}", this.playerView.getCurPosition())));
					break;
			}
			return false;
		}
	}

	class OnBufferingUpdateListenerImpl implements OnBufferingUpdateListener {

		private comp : UTSContainer<FrameLayout>;

		constructor(comp : UTSContainer<FrameLayout>) {
			super();
			this.comp = comp;
		}

		override onBufferingUpdate(iMediaPlayer : IMediaPlayer, i : Int) : void {
			this.comp.$emit("progress", new Map<string, any>().set("detail", new Map<string, any>().set("buffered", i)));
		}
	}
</script>