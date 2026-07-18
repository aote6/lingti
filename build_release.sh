#!/bin/bash
set -e

PROJECT_DIR=~/storage/shared/lingti
BUILD_DIR=$PROJECT_DIR/build/release
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
    $SRC_DIR/MainActivity.java

echo "=== 处理资源 ==="
aapt package -f -M $MANIFEST -S $RES_DIR -I $ANDROID_JAR -F $BUILD_DIR/resources.apk

echo "=== 生成 dex ==="
find $BUILD_DIR/classes -name "*.class" | xargs $BUILD_TOOLS/d8 --lib $ANDROID_JAR --output $BUILD_DIR/

echo "=== 打包 APK ==="
cd $BUILD_DIR/apk
unzip -o ../resources.apk > /dev/null
cp ../classes.dex .
zip -r $BUILD_DIR/unbounded-release-unsigned.apk . > /dev/null

echo "=== 签名 APK ==="
KEYSTORE=$PROJECT_DIR/release.keystore
KEYSTORE_PASS="lingti2026"
KEY_ALIAS="lingti"

if [ ! -f "$KEYSTORE" ]; then
    echo "生成 release keystore: $KEYSTORE"
    keytool -genkey -v \
        -keystore $KEYSTORE \
        -storepass $KEYSTORE_PASS \
        -alias $KEY_ALIAS \
        -keypass $KEYSTORE_PASS \
        -keyalg RSA \
        -keysize 2048 \
        -validity 36500 \
        -dname "CN=Lingti, OU=Unbounded, O=Lingti, L=Earth, ST=Unknown, C=CN" > /dev/null 2>&1
fi

$BUILD_TOOLS/apksigner sign \
    --ks $KEYSTORE \
    --ks-pass pass:$KEYSTORE_PASS \
    --ks-key-alias $KEY_ALIAS \
    --key-pass pass:$KEYSTORE_PASS \
    --out $BUILD_DIR/unbounded-release.apk \
    $BUILD_DIR/unbounded-release-unsigned.apk

echo ""
echo "=== Release 构建完成 ==="
echo "APK: $BUILD_DIR/unbounded-release.apk"
echo ""
echo "安装: termux-open $BUILD_DIR/unbounded-release.apk"
