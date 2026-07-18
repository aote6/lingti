// 手势和长按控制器：五向手势识别、长按连续触发、剪贴板键处理
package com.unbounded.input;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import java.util.List;

import com.unbounded.input.core.layout.KeyModel;

public class KeyboardGestureController {
    private final List<KeyModel> keys;
    private final KeyboardActionDispatcher dispatcher;
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private final GestureRecognizer recognizer = new GestureRecognizer();

    private KeyModel activeKey;
    private boolean isGestureConsumed, isLongPressed;
    private float startX, startY;
    private final Runnable longPressRunnable;
    private final SessionAccess session;
    private final int touchSlop;

    public interface SessionAccess {
        void invalidateView();
        NineKeyKeyboard.InputMode getInputMode();
        float getDpScale();
        android.content.Context getKeyboardContext();
    }

    private final ContinuousDeleteHelper continuousHelper;

    public KeyboardGestureController(List<KeyModel> keys, KeyboardActionDispatcher dispatcher, final SessionAccess session) {
        this.keys = keys;
        this.dispatcher = dispatcher;
        this.session = session;
        this.continuousHelper = new ContinuousDeleteHelper(dispatcher);
        Context ctx = session.getKeyboardContext();
        this.touchSlop = ViewConfiguration.get(ctx).getScaledTouchSlop();
        this.longPressRunnable = new LongPressTask(this);
    }

    private static class LongPressTask implements Runnable {
        private final KeyboardGestureController ctrl;
        LongPressTask(KeyboardGestureController ctrl) { this.ctrl = ctrl; }
        public void run() {
            if (ctrl.activeKey == null || ctrl.isGestureConsumed) return;
            ctrl.isLongPressed = true;
            ctrl.isGestureConsumed = true;
            ctrl.session.invalidateView();

            Command repeatCmd = ctrl.getRepeatableCommand(ctrl.activeKey);
            if (repeatCmd != null) {
                ctrl.continuousHelper.start(repeatCmd);
            } else if (ctrl.activeKey.longPress != null) {
                ctrl.dispatcher.onCommand(ctrl.activeKey.longPress);
            }
        }
    }

    public KeyModel getActiveKey() { return activeKey; }
    public boolean isLongPressed() { return isLongPressed; }

    private Command getRepeatableCommand(KeyModel key) {
        if (key.tap == null) return null;
        if (key.tap.type == Command.Type.BACKSPACE) return key.tap;
        if (key.tap.type == Command.Type.KEY_EVENT) {
            if (key.id.equals("up") || key.id.equals("down")
                    || key.id.equals("left") || key.id.equals("right")) {
                return key.tap;
            }
        }
        return null;
    }

    public void reset() {
        if (activeKey != null) activeKey.pressed = false;
        activeKey = null;
        isGestureConsumed = false;
        isLongPressed = false;
        longPressHandler.removeCallbacks(longPressRunnable);
        continuousHelper.stop();
    }

    private KeyModel findKey(float x, float y) {
        for (KeyModel k : keys) if (k.rect.contains((int) x, (int) y)) return k;
        return null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x; startY = y; isLongPressed = false; isGestureConsumed = false;
                activeKey = findKey(x, y);
                if (activeKey != null) {
                    activeKey.pressed = true;
                    recognizer.onDown(x, y);
                    longPressHandler.postDelayed(longPressRunnable, 450);
                    session.invalidateView();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (activeKey == null) return true;
                if (isLongPressed) return true;
                if (!isGestureConsumed) {
                    float dx = x - startX;
                    float dy = y - startY;
                    float distance = (float) Math.hypot(dx, dy);
                    if (distance > touchSlop) {
                        longPressHandler.removeCallbacks(longPressRunnable);
                        GestureRecognizer.Gesture g = recognizer.onMove(x, y);
                        if (g != GestureRecognizer.Gesture.NONE) {
                            execGesture(g);
                            isGestureConsumed = true;
                            session.invalidateView();
                        }
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                longPressHandler.removeCallbacks(longPressRunnable);
                continuousHelper.stop();
                if (isLongPressed) {
                    isLongPressed = false;
                    if (activeKey != null) activeKey.pressed = false;
                    activeKey = null;
                    session.invalidateView();
                    return true;
                }
                if (activeKey != null && !isGestureConsumed) {
                    GestureRecognizer.Gesture fg = recognizer.onUp();
                    if (fg != GestureRecognizer.Gesture.NONE) execGesture(fg);
                }
                if (activeKey != null) activeKey.pressed = false;
                activeKey = null; isGestureConsumed = false;
                session.invalidateView();
                return true;
        }
        return true;
    }

    private void execGesture(GestureRecognizer.Gesture g) {
        if (activeKey == null || dispatcher == null) return;
        // 剪贴板键：依次粘贴历史记录
        if (g == GestureRecognizer.Gesture.TAP && "📋".equals(activeKey.label)) {
            java.util.List<String> history = com.unbounded.input.SimpleImeService.getClipboardHistory();
            if (!history.isEmpty()) {
                // 取第一条，粘贴后移到末尾（循环使用）
                String item = history.remove(0);
                history.add(item);
                dispatcher.onCommand(Command.insert(item + "\n"));
            }
            return;
        }
        Command cmd = null;
        switch (g) {
            case TAP: cmd = activeKey.tap; break;
            case SWIPE_UP: cmd = activeKey.swipeUp; break;
            case SWIPE_LEFT: cmd = activeKey.swipeLeft; break;
            case SWIPE_DOWN: cmd = activeKey.swipeDown; break;
            case SWIPE_RIGHT: cmd = activeKey.swipeRight; break;
            default: return;
        }
        if (cmd != null) dispatcher.onCommand(cmd);
    }
}
