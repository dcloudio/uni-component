<template>
	<!-- #ifdef APP -->
	<view :ref="elId" class="uni-loading-circle"></view>
	<!-- #endif -->
	<!-- #ifdef WEB -->
	<svg viewBox="25 25 50 50" class="uni-loading-circle uni-load__img uni-load__img--android-H5">
		<circle cx="50" cy="50" r="20" fill="none" :style="{color:color}" :stroke-width="iconsSize"></circle>
	</svg>
	<!-- #endif -->
	<!-- #ifdef MP-WEIXIN -->
	<view class="uni-loading-circle-wx" :style="{color:color,borderWidth:iconsSize+'px'}"></view>
	<!-- #endif -->
</template>
<script>
	const easeInOutCubic = (t : number) : number => {
		return t < 0.5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
	}

	type AnimationState = {
		startTime : number
		rotate : number
		isReversing : boolean
	}

	type AnimationConfig = {
		ctx : DrawableContext
		center : number
		lineWidth : number
		duration : number
		angleFactor : number
		COLOR : string
		MAX_ROTATE : number
		ROTATE_STEP : number
	}
	let elId = 0
	export default {
		name: "LoadingCircle",
		props: {
			speed: {
				type: Number,
				default: 16,
			},
			size: {
				type: Number,
				default: 5,
			},
			color: {
				type: String,
				default: '#666',
			}
		},
		data() {
			// 防止多调用，随机元素id
			elId += 1
			const elID = `Uni_Load_Circle_${elId}`
			return {
				elId: elID,
				refs: '',
				timer: 0,
			};
		},
		computed: {
			iconsSize() : number {
				return this.size
			}
		},
		mounted() {
			// #ifdef APP
			setTimeout(() => {
				const refs = this.$refs[this.elId] as UniElement
				console.log(this.elId, refs);
				// this.init(refs)
				this.build_circular(refs)
			}, 200)
			// #endif
		},
		unmounted() {
			// 组件卸载时，需要卸载，优化性能，防止页面卡死
			cancelAnimationFrame(this.timer)
		},
		methods: {
			/**
			 * 构建圆环动画
			 */
			build_circular(refs : UniElement) {
				const ctx = refs.getDrawableContext()!
				const { width, height } = refs.getBoundingClientRect()
				const size = Math.min(width, height)
				const center = size / 2
				const lineWidth = this.size
				const duration = 1200
				const ARC_LENGTH = 359
				const COLOR = this.color ?? '#666'
				const MAX_ROTATE = 360
				const ROTATE_STEP = 0.05

				const angleFactor = (Math.PI / 180) * ARC_LENGTH
				let animationState : AnimationState = {
					startTime: 0,
					rotate: 0,
					isReversing: false
				}

				const animationConfig : AnimationConfig = {
					ctx,
					center,
					lineWidth,
					duration,
					angleFactor,
					COLOR,
					MAX_ROTATE,
					ROTATE_STEP
				}

				this.timer = requestAnimationFrame((timestamp) => {
					this.animate(timestamp, animationState, animationConfig)
				})
			},

			animate(timestamp : number, state : AnimationState, config : AnimationConfig) {
				const {
					ctx, center, lineWidth, duration,
					angleFactor, COLOR, MAX_ROTATE,
					ROTATE_STEP
				} = config

				if (state.startTime == 0) {
					state.startTime = timestamp
				}
				const elapsed = timestamp - state.startTime
				const progress = Math.min(elapsed / duration, 1)
				const eased = easeInOutCubic(progress)
				ctx.reset()

				const startAngle = state.rotate + (state.isReversing ? eased : 0) * angleFactor
				const endAngle = state.rotate + (state.isReversing ? 0 : eased) * angleFactor
				
				ctx.arc(center, center, center - lineWidth, startAngle, endAngle)
				ctx.lineWidth = lineWidth
				ctx.strokeStyle = COLOR
				ctx.stroke()
				ctx.update()

				state.rotate = (state.rotate + ROTATE_STEP) % MAX_ROTATE

				if (progress >= 1) {
					state.isReversing = !state.isReversing
					state.startTime = timestamp - (duration * (1 - progress)); 
				}

				this.timer = requestAnimationFrame((timestamp) => {
					this.animate(timestamp, state, config)
				})
			}
		}
	}
</script>

<style scoped>
	.uni-loading-circle {
		width: 50px;
		height: 50px;
		/* border: 1px red solid; */
		box-sizing: border-box;
	}

	.block {
		width: 50px;
		height: 50px;
	}

	/* #ifdef MP-WEIXIN */
	.uni-loading-circle-wx {
		display: inline-block;
		box-sizing: border-box;
		width: 50px;
		height: 50px;
		border: 5px solid transparent;
		border-top-color: currentColor;
		border-radius: 50%;
		animation: loading-rotate 0.8s linear infinite;
		color: #1989fa;
	}

	@keyframes loading-rotate {
		from {
			transform: rotate(0deg);
		}

		to {
			transform: rotate(360deg);
		}
	}

	/* #endif */

	/* #ifdef WEB */
	.uni-load__img {
		width: 24px;
		height: 24px;
	}

	.uni-load__img--android-H5 {
		animation: loading-android-H5-rotate 2s linear infinite;
		transform-origin: center center;
	}

	.uni-load__img--android-H5 circle {
		display: inline-block;
		animation: loading-android-H5-dash 1.5s ease-in-out infinite;
		stroke: currentColor;
		stroke-linecap: round;
	}

	@keyframes loading-android-H5-rotate {
		0% {
			transform: rotate(0deg);
		}

		100% {
			transform: rotate(360deg);
		}
	}

	@keyframes loading-android-H5-dash {
		0% {
			stroke-dasharray: 1, 120;
			stroke-dashoffset: 0;
		}

		50% {
			stroke-dasharray: 120, 110;
			stroke-dashoffset: -5;
		}

		100% {
			stroke-dasharray: 150, 180;
			stroke-dashoffset: -120;
		}
	}

	.uni-load__img--android-H5,
	.uni-load__img--android-H5 circle {
		will-change: transform, stroke-dashoffset;
		transform: translate3d(0, 0, 0);
		/* 硬件加速 */
	}

	/* #endif */
</style>