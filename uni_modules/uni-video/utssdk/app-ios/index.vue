<template>
    <view>
    </view>
</template>
<script lang="uts">
    import {
        UIView
    } from "UIKit"

    export default {
        name: "video",
        data() {
            return {}
        },
        // 创建组件对应的原生 View，返回值类型需要修改为实际 View 类型
        NVLoad(): UIView {
            return new UIView()
        }
    }
</script>
