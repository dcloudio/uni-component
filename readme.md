此项目为开源uts组件插件。


## 注意事项
uts组件插件基本都会依赖三方SDK，需要使用自定义基座才能正常运行。

此项目默认为uni-app项目，如果要切换为uni-app x项目，需打开manifest.json，去掉uni-app-x节点的注释：
```
  /* uni-app x 特有相关 */
  // "uni-app-x": {},
```
关闭manifest.json后重新打开即可。


## uni-animation-view
动画组件 animation-view


## uni-video
视频播放组件 video-view，暂时仅支持Android平台的 uni-app x 项目

### 仓库分支与 HBuilder 版本对应关系

- master 对应 [HBuilder](https://www.dcloud.io/hbuilderx.html) 正式版
- alpha 对应 [HBuilder](https://www.dcloud.io/hbuilderx.html) Alpha 版
- dev 对应 [HBuilder](https://www.dcloud.io/hbuilderx.html) 内部 dev 版
