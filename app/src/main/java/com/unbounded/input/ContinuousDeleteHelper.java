// 连续触发工具：长按时每隔N毫秒重复执行Command
package com.unbounded.input;

import android.os.Handler;
import android.os.Looper;

public class ContinuousDeleteHelper {
    private final Handler repeatHandler = new Handler(Looper.getMainLooper());
    private Runnable repeatRunnable;
    private final KeyboardActionDispatcher dispatcher;

    private static final int REPEAT_DELAY_MS = 350;
    private static final int REPEAT_INTERVAL_MS = 60;

    public ContinuousDeleteHelper(KeyboardActionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void start(final Command backspaceCmd) {
        stop();
        repeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (dispatcher != null) {
                    dispatcher.onCommand(backspaceCmd);
                    repeatHandler.postDelayed(this, REPEAT_INTERVAL_MS);
                }
            }
        };
        repeatHandler.postDelayed(repeatRunnable, REPEAT_DELAY_MS);
    }

    public void stop() {
        if (repeatRunnable != null) {
            repeatHandler.removeCallbacks(repeatRunnable);
            repeatRunnable = null;
        }
    }
}
