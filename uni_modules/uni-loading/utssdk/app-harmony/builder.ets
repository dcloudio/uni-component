import { AnimatorOptions, AnimatorResult, SimpleAnimatorOptions } from '@kit.ArkUI'
import { formatColorSync } from '@dcloudio/uni-app-x-runtime'

@Component
struct CanvasCircle {
  lineWidthBase: number = 16
  @Prop @Watch('draw') duration: number = 1333
  @Prop @Watch('draw') easing: string = 'linear'
  @Prop @Watch('draw') color: string = '#000000'
  @Prop @Watch('draw') lineWidthTimes: number = 1
  @Prop @Watch('draw') degrees: number = 270
  private settings: RenderingContextSettings = new RenderingContextSettings(true)
  private context: CanvasRenderingContext2D = new CanvasRenderingContext2D(this.settings)
  private backAnimator: AnimatorResult | undefined = undefined
  @State rotate_: number = 0
  @State animationOptions: AnimatorOptions = {
    duration: this.duration,
    easing: this.easing,
    delay: 0,
    fill: 'forwards',
    direction: 'normal',
    iterations: -1,
    begin: 0,
    end: 360
  }

  draw() {
    this.animationOptions.easing = this.easing
    try {
      this.backAnimator?.reset(this.animationOptions)
    } catch (error) {}

    const canvasWidth = this.context.width
    const canvasHeight = this.context.height
    const realSize = Math.min(canvasWidth, canvasHeight)
    const realLineWidth = (realSize / this.lineWidthBase) * this.lineWidthTimes

    const angleDegrees = this.degrees

    const angleRadians = (angleDegrees * Math.PI) / 180;

    this.context.clearRect(0, 0, canvasWidth, canvasHeight);

    this.context.strokeStyle = this.color;
    this.context.lineWidth = realLineWidth;

    this.context.beginPath();

    const centerX = canvasWidth / 2; // 圆心 X 坐标
    const centerY = canvasHeight / 2; // 圆心 Y 坐标
    const radius = realSize / 2 - realLineWidth / 2; // 半径

    // 使用 arc() 方法绘制弧线
    // arc(x, y, radius, startAngle, endAngle, anticlockwise)
    // x, y: 圆心坐标
    // radius: 半径
    // startAngle: 起始弧度 (0 度在 3 点钟方向)
    // endAngle: 结束弧度
    // anticlockwise: 是否逆时针 (false 为顺时针)
    this.context.arc(centerX, centerY, radius, 0, angleRadians, false);

    this.context.stroke();
  }

  create() {
    this.backAnimator = this.getUIContext()?.createAnimator(this.animationOptions)
    this.backAnimator.onFrame = (value: number) => {
      this.rotate_ = value
    }
    this.backAnimator.play()
  }

  aboutToDisappear() {
    this.backAnimator?.finish();
    this.backAnimator = undefined;
  }

  build() {
    Canvas(this.context)
      .width('100%')
      .height('100%')
      .rotate({
        angle: this.rotate_
      })
      .onReady(() => {
        this.draw()
        setTimeout(() => {
          this.create()
        }, 0)
      })
  }
}

// 计算粗细的倍数 (最小边 / 16 * lineWidthTimes)
function getLineWidthTimes(borderWidth: string | undefined) {
  if (borderWidth == null) {
    return 1
  }
  // TODO 不支持具体 px 值
  /* if (borderWidth.endsWith('px')) {
    const num = parseFloat(borderWidth.replace('px', ''))
    return isNaN(num) ? 1 : num
  } */
  switch (borderWidth) {
    case 'medium':
    case 'normal':
      return 1
    case 'thick':
    case 'bold':
      return 2
  }
  return 1
}

@Builder
export function buildLoading(params: ESObject) {
  Flex({ alignItems: ItemAlign.Center, justifyContent: FlexAlign.Center }) {
    CanvasCircle({
      degrees: params.degrees ?? 270,
      color: formatColorSync(params.color) as string,
      lineWidthTimes: getLineWidthTimes(params.borderWidth),
      duration: params.duration ?? 1333,
      easing: params.animationTimingFunction
    })
  }
  .width('100%')
  .height('100%')
}
