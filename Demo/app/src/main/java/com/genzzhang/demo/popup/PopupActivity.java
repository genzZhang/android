package com.genzzhang.demo.popup;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.genzzhang.demo.app.AbsActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;

/**
 * 背景是管理弹窗，随意addView，弹窗叠加等，尽可能少的修改历史代码完成弹窗管理
 * 同一时机触发弹窗，优先级、骚扰控制等，如何管理
 * 弹窗样式：悬浮窗、activity弹窗
 * 悬浮弹窗，接管WindowManager.addView等
 * activity弹窗，接管ams.startActivity等 https://github.com/BaoBaoJianqiang/HookAMS
 * 悬浮窗，这里只是替换activity中的mWindowManager
 * 如果全局Hook，暂时还找不到一个好的位置
 * 大部分都是Context.getSystemService(Context.WINDOW_SERVICE)系统调用
 * 只能提供一个专用接口来获取服务替换上面的系统调用接口
 */
public class PopupActivity extends AbsActivity {

    private WindowManager mOriginalWindowManager;
    private long lastAddTime;

    @Override
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mOriginalWindowManager = getWindowManager();
        try {
            Log.i(TAG, mOriginalWindowManager.toString());
            Class<?> clazz = Activity.class;
            Field field = clazz.getDeclaredField("mWindowManager");
            field.setAccessible(true);
            Object object = generateProxyObject(mOriginalWindowManager);
            field.set(this, object);
        } catch (Throwable e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }
    }

    private void getAllInterfaces(Class clazz, HashSet<Class<?>> interfaceSet) {
        Class<?>[] classes = clazz.getInterfaces();
        if (classes != null && classes.length > 0) {
            interfaceSet.addAll(Arrays.asList(classes));
        }
        if (clazz.getSuperclass() != Object.class) {
            getAllInterfaces(clazz.getSuperclass(), interfaceSet);
        }
    }

    private Object generateProxyObject(Object baseObject) {
        HashSet<Class<?>> classSet = new HashSet<>();
        getAllInterfaces(baseObject.getClass(), classSet);
        Class<?>[] interfaces = new Class[classSet.size()];
        classSet.toArray(interfaces);
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                        // updateViewLayout removeView
                        if (TextUtils.equals(method.getName(), "addView")) {
                            if (System.currentTimeMillis() - lastAddTime > 3 * 1000) {
                                lastAddTime = System.currentTimeMillis();
                                return method.invoke(mOriginalWindowManager, args);
                            } else {
                                mToast.setText("距离上次不足3秒");
                                mToast.show();
                                return null;
                            }
                        } else {
                            return method.invoke(mOriginalWindowManager, args);
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout layout = new FrameLayout(mContext);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(layout);
        final Button btn = new Button(mContext);
        btn.setGravity(Gravity.CENTER);
        btn.setText("先给悬浮窗权限再点击");
        btn.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addView();
            }
        });
    }

    private void addView() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.RGBA_8888);
        layoutParams.gravity = Gravity.TOP;
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        try {
            final WindowManager proxy = getWindowManager();
            Log.i(TAG, proxy.toString());
            final TextView textView = new TextView(mContext);
            textView.setText(System.currentTimeMillis() + "");
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    proxy.removeView(textView);
                }
            });
            proxy.addView(textView, layoutParams);
        } catch (Throwable e) {
            Log.i(TAG, Log.getStackTraceString(e));
            return;
        }
    }


}
