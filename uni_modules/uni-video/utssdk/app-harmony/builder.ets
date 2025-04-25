import { AVVolumePanel, AVVolumePanelParameter } from '@kit.AudioKit';
import { getRealPath } from "@dcloudio/uni-runtime-harmony";

const formatVideoOptions = (options : VideoOptions) : VideoOptions => {
  options.src = getRealPath(options.src as string)
  return options
}

@Builder
export function buildVideo(params : ESObject) {
  Video(formatVideoOptions(params.options))
    .attributeModifier(params.modifier)
    .width('100%')
    .height('100%')
    .objectFit(params.objectFit ?? ImageFit.Contain)
    .muted(params.muted)
    .autoPlay(params.autoplay)
    .loop(params.loop)
    .controls(params.controls ?? false)
    .onStart(params?.event?.onPlay)
    .onPause(params?.event?.onPause)
    .onFinish(params?.event?.onEnded)
    .onStop(params?.event?.onStop)
    .onPrepared(params?.event?.onPrepared)
    .onSeeking(params?.event?.onSeeking)
    .onSeeked(params?.event?.onSeeked)
    .onUpdate(params?.event?.onTimeUpdate)
    .onFullscreenChange(params?.event?.onFullscreenChange)
    .onError(params?.event?.onError)
}
