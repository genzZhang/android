# Document for Demo  


## 1. 水平循环滚动：支持自动播放和左右滑动

![](/Demo/docs/image/2017-07-30-HorizontnalLoopView.png)

## 2. 触摸事件分发与拦截测试

当我们触摸屏幕，一个事件从产生到派发至View的过程：
>* 首先驱动，从硬件到Linux Kernel层的设备驱动产生输入事件，这些事件最终会写入到设备文件/dev/input/eventX中完成驱动层的上报（一般的手机触摸屏一般以60次/秒左右的频率上报触摸事件）；   
>* 再到框架FrameWork层，InputManager中的InputReader线程会通过EventHub的getEvent方法从/dev/input/目录下的设备文件中持续读取事件，然后将读取到的事件放至消息队列中，WindowManagerService中InputDispatcher线程则循环从消息队列中取出原始事件RawEvent进行加工，并找到Window Manager中的Focus Window，然后通过进程间通信将其发送至当前焦点所在的窗口；
>* 最终到当前焦点所在的窗口，通常是Activity，再从View树的树根ViewRootImpl开始分发到具体View。

所有的RawEvent事件都会被封装成MotionEvent对象，该对象代表了Touch事件的坐标、动作等基本信息。
一般情况下，每一个Touch事件，总是以ACTION_DOWN事件开始，中间穿插着一些ACTION_MOVE事件（取决于是否有手势的移动），然后以ACTION_UP事件结束。  
ACTION_DOWN-----LongClick------ACTION_UP------Click

对事件的处理有三类：
>分发：public boolean dispatchTouchEvent(MotionEvent ev)  
>拦截：public boolean onInterceptTouchEvent(MotionEvent ev)  
>消费：public boolean onTouchEvent(MotionEvent ev)和OnTouchListener接口中boolean onTouch(View v, MotionEvent event)  

以上三类函数非常重要，贯穿整个事件分发的流程，伪代码如下
```
public boolean dispatchTouchEvent(MotionEvent ev){
    boolean handle = false;
    if(onInterceptTouchEvent(ev)){
        handle = onTouchEvent(ev);
    }else{
        handle = child.dispatchTouchEvent(ev);
    }
    return handle;
}
```

ViewGroup 三个函数的调用关系  
```
public boolean dispatchTouchEvent(MotionEvent ev){
    // Check for interception.
    final boolean intercepted;
    if (actionMasked == MotionEvent.ACTION_DOWN
            || mFirstTouchTarget != null) {
        final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
        if (!disallowIntercept) {
            intercepted = onInterceptTouchEvent(ev);
            ev.setAction(action); // restore action in case it was changed
        } else {
            intercepted = false;
        }
    } else {
        // There are no touch targets and this action is not an initial down
        // so this view group continues to intercept touches.
        intercepted = true;
    }


	// Dispatch to touch targets.
    if (mFirstTouchTarget == null) {
        // No touch targets so treat this as an ordinary view.
        handled = dispatchTransformedTouchEvent(ev, canceled, null,
                TouchTarget.ALL_POINTER_IDS);
    } 
}  

//分发给自己，super.dispatchTouchEvent 也就是下文中View的事件分发
private boolean dispatchTransformedTouchEvent(MotionEvent event, boolean cancel,
        View child, int desiredPointerIdBits) {
    // Canceling motions is a special case.  We don't need to perform any transformations
    // or filtering.  The important part is the action, not the contents.
    final int oldAction = event.getAction();
    if (cancel || oldAction == MotionEvent.ACTION_CANCEL) {
        event.setAction(MotionEvent.ACTION_CANCEL);
        if (child == null) {
            handled = super.dispatchTouchEvent(event);
        } else {
            handled = child.dispatchTouchEvent(event);
        }
        event.setAction(oldAction);
        return handled;
    }
}
```  
ViewGroup在什么情况下可以执行onInterceptTouchEvent方法？从上述源码可以看出，需要同时满足以下条件：  
>* ACTION_DOWN或mFirstTouchTarget != null  
 + 我们知道，拦截是由 onInterceptTouchEvent 方法的返回值决定的。假设该 ViewGroup 没有被设置为不允许拦截（即正常情况下），那么对于 DOWN 事件，onInterceptTouchEvent 方法肯定会被调用。另外，如果是 MOVE、UP 或其他事件类型，只要满足 mFirstTouchTarget != null 时也会调用 onInterceptTouchEvent。
 + mFirstTouchTarget是用来记录在 DOWN 事件中消费了事件的子View，它以链表的形式存在，通过next变量串起来。在DOWN事件中，如果通过点击的坐标找到了某个子View，且该子View消费了事件，那么链表中就将这个子View记录了下来。这样在后续的MOVE、UP事件中，能直接根据这个链表，将事件分发给目标子View，而无需重复再遍历子View去寻找事件的消费者。
 + 如果在onInterceptTouchEvent方法中后面拦截了非DOWN的事件，那么分发ACTION_CANCEL到子view中，mFirstTouchTarget = NULL,并且把事件分发到自己的onTouchEvent方法去处理。而如果onInterceptTouchEvent方法中拦截的是DOWN事件，那么将导致在dispatch过程中找不到事件的消费者（即 mFirstTouchTarget == null），那么后续的MOVE、UP事件将不会再询问是否需要拦截，而是直接分发到自己的onTouchEvent方法去处理。
>* disallowIntercept == false
 子View可以调用getParent().requestDisallowInterceptTouchEvent(disallowIntercept = true)避免事件被拦截


View中方法调用关系，其中view没有子view则无需拦截，也就没有onInterceptTouchEvent方法   
```
public boolean dispatchTouchEvent(MotionEvent event) {
    if ((mViewFlags & ENABLED_MASK) == ENABLED && handleScrollBarDragging(event)) {
        result = true;
    }
    //noinspection SimplifiableIfStatement
    ListenerInfo li = mListenerInfo;
    if (li != null && li.mOnTouchListener != null
            && (mViewFlags & ENABLED_MASK) == ENABLED
            && li.mOnTouchListener.onTouch(this, event)) {
        result = true;
    }
    if (!result && onTouchEvent(event)) {
        result = true;
    }
}
```
对于View的事件分发总结如下：
>* 整个View的事件转发流程是：View.dispatchEvent->View.setOnTouchListener->View.onTouchEvent  
在dispatchTouchEvent中会进行OnTouchListener的判断，如果OnTouchListener不为null且返回true，则表示事件被消费，onTouchEvent不会被执行；否则执行onTouchEvent。 
>* MOVE事件只要不划出View边界是能处理Click和LongClick的，长按的检测时长是500ms（默认值，系统可以修改），Click没有时间限制，View能同时处理LongClick和Click。 


一般来说，super.xx是系统默认的处理结果，我们可以根据这个结果来判断一些结果。

## 3. ListView删除某一个item动画：仿照KingRoot自启动管理

![](/Demo/docs/image/2017-07-30-ListViewDelAnimation.png)

## 4. 使用PorterDuff模式，实现动画效果。 
>1）仿照腾讯手机管家齿轮转动动画  
2）使用硬编码实现特殊背景  
3）logo加载 

![](/Demo/docs/image/2017-07-30-PorterDuff.png)

## 5. Shader：Paint的setShader之着色器
>1）倒影效果  
2）闪烁文字  
3）背景渐变色设置
 
![](/Demo/docs/image/2017-07-30-Shader.png)

## 6. 读取assets中Xml解析展示Emoji
>1）解析特定格式xml文件  
2）LruCache<String, Bitmap>，缓存assets中的图片

![](/Demo/docs/image/2017-07-30-XmlEmojiParse.png)





















非科班生从0到1学习Android笔记专用 
For details, see [Blog](https://genzzhang.github.io/).