<template>
  <!-- #ifdef APP-ANDROID || APP-IOS || APP-HARMONY -->
  <view>
    <native-view
      :style="{
        'border-width': borderWidth,
        'border-color': borderColor,
        'animation-timing-function': timingFunction,
        width: width ?? '16px',
        height: height ?? '16px'
      }"
      @init="onviewinit"
    ></native-view>
  </view>
  <!-- #endif -->
  <!-- #ifdef WEB ||MP-WEIXIN -->
  <view class="_uni-loading_ loading-4-3"></view>
  <!-- #endif -->
</template>
<script setup lang="uts">
// #ifdef APP-ANDROID || APP-IOS || APP-HARMONY
import { NativeLoading } from "@/uni_modules/uni-loading";

const style = useComputedStyle({
    properties: [
      'border-width',
      'border-color',
      'width',
      'height',
      'animation-timing-function'],
    filterProperties: true
  } as UseComputedStyleOptions)

const width = computed(() => style.get('width')?.toString())
const height = computed(() => style.get('height')?.toString())
// border-color 会被解为四个方向的值，取 top 值（哪个方向都一样）
const borderColor = computed(() => style.get('border-color')?.toString() ?? style.get('border-top-color')?.toString())
// border-width 会被解为四个方向的值，取 top 值（哪个方向都一样）
const borderWidth = computed(() => style.get('border-width')?.toString() ?? style.get('border-top-width')?.toString())
const timingFunction = computed(() => style.get('animation-timing-function')?.toString())

interface LoadingState {
  nativeLoading: NativeLoading | null
}
const loadingState = reactive<LoadingState>({
  nativeLoading: null
})

watchEffect(() => {
  // #ifdef APP-IOS
  loadingState.nativeLoading?.updateStyle(
    width.value,
    height.value,
    borderColor.value,
    borderWidth.value,
    timingFunction.value,
  )
  // #endif

  // #ifndef APP-IOS
  loadingState.nativeLoading?.updateStyle(
    borderColor.value,
    borderWidth.value,
    timingFunction.value,
  )
  // #endif
})

//native-view初始化时触发此方法
const onviewinit = (e : UniNativeViewInitEvent) => {
  //获取UniNativeViewElement 传递给NativeButton对象
  loadingState.nativeLoading = new NativeLoading(e.detail.element);
}

onUnmounted(() => {
  loadingState.nativeLoading?.destroy()
})
// #endif
</script>
<style>
/* #ifdef WEB || MP-WEIXIN*/
._uni-loading_ {
  width: 16px;
  height: 16px;
  border-radius: 100px;
  border-width: 1px;
  border-style: solid;
  border-color: transparent;
  transform: translateZ(0);
  /* 启用硬件加速 */
  image-rendering: -webkit-optimize-contrast;
  /* 提高图像对比度 */
  image-rendering: crisp-edges;
  /* 边缘清晰 */
  animation: k-loading-spin 1.333s infinite;
  animation-timing-function: linear;
}

/*.uni-loading_.loading-4-1 {
  border-left-color: #007AFF;
}

.uni-loading_.loading-4-2 {
  border-left-color: #007AFF;
  border-top-color: #007AFF;
}*/

.loading-4-3 {
  border-color: #000;
  border-right-color: transparent !important;
}

@keyframes k-loading-spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}
/* #endif */
</style>
