# DanmakuView

普通的`DanmakuView`, 支持右至左滚动, 左至右滚动, 顶部, 底部弹幕, 高级弹幕, 可自定义弹幕(继承`Danmaku`类或`LineDanmaku`类), 比没得用好

## 使用

见`sample`

## TODO
- 弹幕点击事件

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
