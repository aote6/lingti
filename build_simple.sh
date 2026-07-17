#!/bin/bash

PROJECT_DIR=~/storage/shared/lingti
BUILD_DIR=$PROJECT_DIR/build/simple
SRC_DIR=$PROJECT_DIR/app/src/main/java/com/unbounded/input
RES_DIR=$PROJECT_DIR/app/src/main/res
MANIFEST=$PROJECT_DIR/app/src/main/AndroidManifest.xml

# 清理并创建构建目录
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/classes
mkdir -p $BUILD_DIR/apk

# 找到 android.jar
ANDROID_JAR=$HOME/android-sdk/platforms/android-34/android.jar

echo "=== 编译 Java 源文件 ==="
ecj -d $BUILD_DIR/classes \
    -cp $ANDROID_JAR \
    -source 17 -target 17 \
    $SRC_DIR/MainActivity.kt $SRC_DIR/SimpleImeService.kt 2>&1

# ecj 只能编译 Java，Kotlin 需要 kotlinc
# 如果上面的 ecj 失败，用 kotlinc 代替
if [ $? -ne 0 ]; then
    echo "ecj 不支持 Kotlin，改用 kotlinc..."
    kotlinc -d $BUILD_DIR/classes \
        -cp $ANDROID_JAR \
        -jvm-target 17 \
        $SRC_DIR/MainActivity.kt $SRC_DIR/SimpleImeService.kt 2>&1
fi

echo "=== 处理资源 ==="
aapt package -f -M $MANIFEST -S $RES_DIR -I $ANDROID_JAR -F $BUILD_DIR/resources.apk

echo "=== 生成 dex ==="
dx --dex --output=$BUILD_DIR/classes.dex $BUILD_DIR/classes

echo "=== 打包 APK ==="
cd $BUILD_DIR/apk
unzip -o ../resources.apk
cp ../classes.dex .

# 重新打包
zip -r $BUILD_DIR/app-unsigned.apk . 2>&1

echo "=== 签名 APK ==="
# 生成调试密钥（如果不存在）
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
        -dname "CN=Android Debug,O=Android,C=US" 2>&1
fi

# 签名
jarsigner -verbose \
    -keystore ~/.android/debug.keystore \
    -storepass android \
    -keypass android \
    $BUILD_DIR/app-unsigned.apk androiddebugkey 2>&1

echo ""
echo "=== 完成 ==="
echo "APK 位置: $BUILD_DIR/app-unsigned.apk"
