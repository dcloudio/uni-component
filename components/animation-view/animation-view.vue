<template>
	<view v-show="!hidden" ref="animation">
	</view>
</template>

<script>
	import lottie from './lottie.js'
	export default {
		name: 'animation-view',
		props: {
			/**
			 * 动画资源地址，支持远程 URL 地址和本地绝对路径
			 */
			"path": {
				type: String,
				default: ""
			},
			/**
			 * 动画是否自动播放
			 */
			"autoplay": {
				type: Boolean,
				default: false
			},
			/**
			 * 动画是否循环播放
			 */
			"loop": {
				type: Boolean,
				default: false
			},
			/**
			 * 是否隐藏动画
			 */
			"hidden": {
				type: Boolean,
				default: true
			},
			/**
			 * 动画操作，可取值 play、pause、stop
			 */
			"action": {
				type: String,
				default: "play",
				validator: (value) => {
					return ['play', 'pause', 'stop'].includes(value)
				}
			}
		},
		watch: {
			path(val) {
				this.init()
			},
			action(val) {
				switch (val) {
					case "play":
						this.play()
						break
					case "pause":
						this.pause()
						break
					case "stop":
						this.stop()
						break
					default:
						break
				}
			}
		},
		data() {
			return {
				animation: null
			}
		},
		mounted() {
			this.init()
		},
		methods: {
			init() {
				if (this.animation) {
					this.animation.destroy()
				}
				// 初始化
				this.animation = lottie.loadAnimation({
					path: this.path,
					loop: this.loop,
					autoplay: this.autoplay,
					loop: this.loop,
					container: this.$refs.animation.$el
				})
				// 动画结束
				this.animation.onComplete = () => {
					this.$emit('bindended')
				}
			},
			play() {
				this.animation?.play()
			},
			pause() {
				this.animation?.pause()
			},
			stop() {
				this.animation?.stop()
			},
		}
	}
</script>