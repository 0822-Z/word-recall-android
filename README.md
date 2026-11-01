# 词忆 WordRecall

一个面向学生记忆单词和短语的本地 Android 应用。应用不提供评分，不会判断用户填写的汉语释义是否正确，只帮助用户完成“看英文、回想汉语、对照答案、标记熟记”的学习流程。

## 已实现功能

- 逐个导入英文单词或短语及其汉语意思。
- 三个主入口：导入、乱序回忆、单词本。
- 回忆时仅显示英文，用户自行填写汉语意思。
- 提交后同时展示英文、原始汉语和用户答案，不评分、不评改。
- 可将单词标记为已熟记；标记后该词不会继续出现在回忆队列中。
- 单词本可查看全部单词或只看已熟记单词。
- 可取消熟记标记，单词会重新进入乱序回忆。
- 所有数据保存在手机本地，无需注册或联网。

## 在 Android Studio 中运行

1. 使用 Android Studio 打开本目录。
2. 等待 Gradle 同步完成。
3. 连接 Android 8.0（API 26）或更高版本的手机，开启开发者选项和 USB 调试。
4. 点击 Run，或执行 `.\gradlew.bat assembleDebug`。

生成的调试安装包位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 在 VS Code 中运行

安装 Android SDK、JDK 17 和 Android 相关扩展后，可以直接打开项目目录并使用 Gradle 构建。项目本身不依赖 Android Studio。

## 数据说明

单词数据使用 Android 本地 `SharedPreferences` 保存。卸载应用或清除应用数据会删除单词记录。

## 最低系统版本

Android 8.0（API 26）。
