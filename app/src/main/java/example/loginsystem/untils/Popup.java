package example.loginsystem.untils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import example.loginsystem.R;

public class Popup {
    // popUpwindow
    private View contentView;
    private PopupWindow popupWindow;
    private LayoutInflater layoutInflater;

    public Popup()
    {

    }

    public Popup(Context context)
    {
        this.layoutInflater = LayoutInflater.from(context);
        this.contentView = LayoutInflater.from(context).inflate(R.layout.popupwindow, null);
    }

    // 显示 Popupwindow
    public void show(View anchor, int xOff, int yOff)
    {
        contentView = this.layoutInflater.inflate(R.layout.popupwindow, null);
        popupWindow = new PopupWindow(contentView, 450, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(anchor, xOff, yOff);
    }

    // 显示 Popupwindow
    public void show(View anchor)
    {
        contentView = layoutInflater.inflate(R.layout.popupwindow, null);
        popupWindow = new PopupWindow(contentView, 500, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(anchor,225,-400);
    }

    // 关闭Popupwindow
    public void dismiss()
    {
        if (popupWindow != null) {
            try {
                Thread.sleep(1000);
                popupWindow.dismiss();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
