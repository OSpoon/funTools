package n22.online.funtools.service;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by zhanxiaolin-n22 on 2019/1/11.
 */

public class QHBAccessibilityService extends AccessibilityService{
    public static final String TAG = "QHBAccessibilityService";

    /**
     * 启动服务调用
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    /**
     * 窗口变化调用
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        CharSequence classNameChr = event.getClassName();
        String className = classNameChr.toString();
        Log.d("AccessibilityService", event.toString());
        AccessibilityNodeInfo nodeInfo = event.getSource();//当前界面的可访问节点信息
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//界面变化事件
            ComponentName componentName = new ComponentName(event.getPackageName().toString(), event.getClassName().toString());
            ActivityInfo activityInfo = tryGetActivity(componentName);
            boolean isActivity = activityInfo != null;
            if (isActivity) {
                Log.i(TAG, componentName.flattenToShortString());
                //格式为：(包名/.+当前Activity所在包的类名)
                //如果是模拟程序的操作界面
                if (componentName.flattenToShortString().equals("com.demon.simulationclick/.MainActivity")) {
                    //当前是模拟程序的主页面，则模拟点击按钮
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        //通过id寻找控件，id格式为：(包名:id/+制定控件的id)
                        //一般除非第三方应该是自己的，否则我们很难通过这种方式找到控件
                        //List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.demon.simulationclick:id/btn_click");
                        //通过控件的text寻找控件
                        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("模拟点击");
                        if (list != null && list.size() > 0) {
                            list.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {//View点击事件
            //Log.i(TAG, "onAccessibilityEvent: " + nodeInfo.getText());
            if ((nodeInfo.getText() + "").equals("模拟点击")) {
                //Toast.makeText(this, "这是来自监听Service的响应！", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onAccessibilityEvent: 这是来自监听Service的响应！");
            }
        }
    }

    /**
     * 中断服务
     */
    @Override
    public void onInterrupt() {

    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
