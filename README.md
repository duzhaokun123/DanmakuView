# DanmakuView

普通的`DanmakuView`, 支持右至左滚动, 左至右滚动, 顶部, 底部弹幕, 高级弹幕(关键帧弹幕), 可自定义弹幕(继承`Danmaku`类或`LineDanmaku`类), 比没得用好

也可以看看 [duzhaokun123/fl_danmaku_widget](https://github.com/duzhaokun123/fl_danmaku_widget) 这是一个使用几乎相同逻辑的 flutter 版本

## 使用

见 [sample](sample)

## 效果

github 不能内联 video 标签 也不能引用存储库的视频资源

所以 自己看 [art/sample.mp4](art/sample.mp4)

## BUG

- 二次改变大小后不重绘

## 下载

### jitpack

LastVersion [![LastVersion](https://jitpack.io/v/duzhaokun123/DanmakuView.svg?style=flat-square)](https://jitpack.io/#duzhaokun123/DanmakuView)

1. Add it in your root `build.gradle` at the end of repositories:

```groovy
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency

```groovy
dependencies {
    implementation 'com.github.duzhaokun123:DanmakuView:$LastVersion'
}
```

## Thanks

[AOSP](https://source.android.com)

[DanmakuFlameMaster](https://github.com/bilibili/DanmakuFlameMaster)

[flexbox-layout](https://github.com/google/flexbox-layout)

[jitpack.io](https://jitpack.io)

[nuck](https://github.com/nukc) 在点击事件上的帮助
