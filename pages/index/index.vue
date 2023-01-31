<template>
	<div>
		<animation-view-h5 class="animation" :path="path" :loop="loop" :autoplay="autoplay" :action="action"
			:hidden="hidden" @bindended="lottieEnd">
		</animation-view-h5>
		<button @click="playLottie" type="primary">{{status}}lottie动画</button>
		<button @click="changeLottie" type="primary">切换{{location}}动画</button>
		<button @click="changeLottieDisplay" type="primary">{{display}}动画</button>
	</div>
</template>

<script>
	export default {
		data() {
			return {
				path: '/uni_modules/uni-animation-view/static/lottie.json',
				loop: false,
				autoplay: false,
				action: 'stop',
				hidden: false,
				status: '播放',
				location: '网络',
				display: '隐藏'
			}
		},
		methods: {
			playLottie() {
				this.action = ('play' !== this.action) ? 'play' : 'pause';
				this.status = ('pause' === this.action) ? '播放' : '暂停';
			},
			changeLottie() {
				this.action = 'stop';
				this.status = '播放';
				this.path = ('网络' === this.location) ? 'https://native-res.dcloud.net.cn/uni-app/static/lottie.json' : '/uni_modules/uni-animation-view/static/lottie.json';
				this.location = ('网络' === this.location) ? '本地' : '网络';
				console.log('path='+this.path);
			},
			changeLottieDisplay() {
				this.hidden = !this.hidden;
				this.display = this.hidden ? '显示' : '隐藏';
			},
			lottieEnd() {
				this.status = '播放';
				this.action = 'stop';
				console.log('动画播放结束');
			}
		}
	}
</script>

<style>
	.animation {
		width: 750rpx;
		height: 300rpx;
		background-color: #CCCCCC;
		margin-bottom: 20px;
	}
</style>
