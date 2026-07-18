#!/bin/bash
set -e

PROJECT_DIR=~/storage/shared/lingti
BUILD_DIR=$PROJECT_DIR/build/simple
SRC_DIR=$PROJECT_DIR/app/src/main/java/com/unbounded/input
RES_DIR=$PROJECT_DIR/app/src/main/res
MANIFEST=$PROJECT_DIR/app/src/main/AndroidManifest.xml
ANDROID_JAR=$HOME/android-sdk/platforms/android-34/android.jar
BUILD_TOOLS=$HOME/android-sdk/build-tools/34.0.0

rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/classes
mkdir -p $BUILD_DIR/apk

echo "=== 编译 Java 源文件 ==="
ecj -d $BUILD_DIR/classes \
    -cp $ANDROID_JAR \
    $SRC_DIR/Command.java \
    $SRC_DIR/InsertText.java \
    $SRC_DIR/Backspace.java \
    $SRC_DIR/Commit.java \
    $SRC_DIR/InputEngine.java \
    $SRC_DIR/GestureRecognizer.java \
    $SRC_DIR/RuleLoader.java \
    $SRC_DIR/T9Engine.java \
    $SRC_DIR/MultiTapEngine.java \
    $SRC_DIR/ThemeTokens.java \
    $SRC_DIR/KeyboardActionDispatcher.java \
    $SRC_DIR/ContinuousDeleteHelper.java \
    $SRC_DIR/KeyboardRenderer.java \
    $SRC_DIR/KeyboardGestureController.java \
    $SRC_DIR/NineKeyKeyboard.java \
    $SRC_DIR/SimpleImeService.java \
    $SRC_DIR/SettingsActivity.java \
    $SRC_DIR/MainActivity.java \
    $SRC_DIR/core/command/KeyEventCommand.java \
    $SRC_DIR/core/command/KeyChordCommand.java \
    $SRC_DIR/core/layout/KeyModel.java \
    $SRC_DIR/core/layout/RowSpec.java \
    $SRC_DIR/core/layout/LayoutProfile.java \
    $SRC_DIR/core/layout/KeyboardLayout.java \
    $SRC_DIR/core/layout/LayoutManager.java \
    $SRC_DIR/layouts/nine/NineKeyLayout.java

echo "=== 处理资源 ==="
aapt package -f -M $MANIFEST -S $RES_DIR -I $ANDROID_JAR -F $BUILD_DIR/resources.apk

echo "=== 生成 dex ==="
find $BUILD_DIR/classes -name "*.class" | xargs $BUILD_TOOLS/d8 --lib $ANDROID_JAR --output $BUILD_DIR/

echo "=== 打包 APK ==="
cd $BUILD_DIR/apk
unzip -o ../resources.apk > /dev/null
cp ../classes.dex .
zip -r $BUILD_DIR/unbounded-mvp-unsigned.apk . > /dev/null

echo "=== 签名 APK ==="
if [ ! -f ~/.android/debug.keystore ]; then
    mkdir -p ~/.android
    keytool -genkey -v \
        -keystore ~/.android/debug.keystore \
        -storepass android \
        -alias androiddebugkey \
        -keypass android \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US" > /dev/null 2>&1
fi

$BUILD_TOOLS/apksigner sign \
    --ks ~/.android/debug.keystore \
    --ks-pass pass:android \
    --out $BUILD_DIR/unbounded-mvp.apk \
    $BUILD_DIR/unbounded-mvp-unsigned.apk

echo ""
echo "=== 构建完成 ==="
echo "APK: $BUILD_DIR/unbounded-mvp.apk"
