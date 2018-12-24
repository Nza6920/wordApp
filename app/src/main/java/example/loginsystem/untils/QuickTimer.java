package example.loginsystem.untils;

import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public abstract class QuickTimer
{
    private Timer timer;
    private TimerTask timerTask = new MyTimerTask();
    private Handler handler = new MyHandler();


    // 子类需要重写此方法, 此方法在线程中调用
    protected abstract Object doInBackground();

    // 更新UI, 需要尽快完成
    protected abstract void onPostExecute(Object result);

    // 停止计时
    public void cancel()
    {
        if(timer!=null) {
            timer.cancel();
            timer = null;
        }
    }

    // 开始计时
    public void schedule(int interval)
    {
        if (timer != null) return;
        timer = new Timer();
        timer.schedule(timerTask, 0, interval);
    }

    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            Object result = doInBackground();

            Message msg = new Message();
            msg.what = 1; // 消息类型
            msg.obj = result;
            handler.sendMessage(msg);
        }
    }


    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Object result = msg.obj;
            onPostExecute(result);
        }
    }
}
