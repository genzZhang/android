# Document for Demo  


## 1. 水平循环滚动：支持自动播放和左右滑动

![](/Demo/docs/image/2017-07-30-HorizontnalLoopView.png)

## 2. 触摸事件分发与拦截测试
详情参考：[Android 触摸事件分发机制详解](https://genzzhang.github.io/Android触摸事件分发机制详解/)  
>```
>1）触摸事件分发流程
>屏幕 -> 驱动 -> 定期上报触摸事件 -> 设备文件记录 -> Andorid定期读取  -> Window -> Activity ->  ViewTree 
>
>2）ViewTree处理触摸事件
>所有的RawEvent事件都会被封装成MotionEvent对象，该对象代表了Touch事件的坐标、动作等基本信息。
>动作：ACTION_DOWN、ACTION_MOVE、ACTION_UP、ACTION_CANCEL等
>坐标：getRawX、getX
>分发：public boolean dispatchTouchEvent(MotionEvent ev)
>拦截：public boolean onInterceptTouchEvent(MotionEvent ev)
>消费：public boolean OnTouchListener.onTouch(View v, MotionEvent event)
>     public boolean onTouchEvent(MotionEvent ev)
>     public void OnClickListener.onClick(View v) ...
>```
>
>```
>3）触摸事件分发处理流程
>ViewGroup:
>    dispatchTouchEvent           
>        onInterceptTouchEvent            
>        child.dispatchTouchEvent (TouchTarget记录消费Down事件View)                 
>        super.dispatchTouchEvent
>View:
>    dispatchTouchEvent         
>        OnTouchListener.onTouch(View v, MotionEvent event)       
>        onTouchEvent(MotionEvent ev)
>            TouchDegate.onTouchEvent(MotionEvent ev)
>            OnClickListener.onClick(View v)
>```
>
>```
>4）使用自己的分发逻辑  
>   合理不调用super.dispatchTouchEvent
>   合理修改MotionEvent属性，包括动作或坐标，如果cancel则TouchTarget记录清除后续不再分发
>   合理自行调用触摸相关的函数
>   TouchDegate.onTouchEvent代理，可以把A接收到的事件传递给B
>   return true表示此View或其子View要处理相关的触摸逻辑，后续的相关事件要继续分发到此
>```

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

ViewGroup在什么情况下可以执行onInterceptTouchEvent方法？需要同时满足以下2个条件：  
>### ACTION_DOWN或mFirstTouchTarget != null
 + 我们知道，拦截是由 onInterceptTouchEvent 方法的返回值决定的。假设该 ViewGroup 没有被设置为不允许拦截（即正常情况下），那么对于 DOWN 事件，onInterceptTouchEvent 方法肯定会被调用。另外，如果是 MOVE、UP 或其他事件类型，只要满足 mFirstTouchTarget != null 时也会调用 onInterceptTouchEvent。  
 + mFirstTouchTarget是用来记录在 DOWN 事件中消费了事件的子View，它以链表的形式存在，通过next变量串起来。在DOWN事件中，如果通过点击的坐标找到了某个子View，且该子View消费了事件，那么链表中就将这个子View记录了下来。这样在后续的MOVE、UP事件中，能直接根据这个链表，将事件分发给目标子View，而无需重复再遍历子View去寻找事件的消费者。  
 + 如果在onInterceptTouchEvent方法中后面拦截了非DOWN的事件，那么分发ACTION_CANCEL到子view中，mFirstTouchTarget = NULL,并且把事件分发到自己的onTouchEvent方法去处理。而如果onInterceptTouchEvent方法中拦截的是DOWN事件，那么将导致在dispatch过程中找不到事件的消费者（即 mFirstTouchTarget == null），那么后续的MOVE、UP事件将不会再询问是否需要拦截，而是直接分发到自己的onTouchEvent方法去处理。
>### disallowIntercept == false  
 + child View可以调用getParent().requestDisallowInterceptTouchEvent(disallowIntercept = true)避免事件被拦截


View中方法调用关系，其中view没有子view则无需拦截，也就没有onInterceptTouchEvent方法   
对于View的事件分发总结如下：
>**整个View的事件转发流程**是：View.dispatchEvent->View.setOnTouchListener->View.onTouchEvent  
在dispatchTouchEvent中会进行OnTouchListener的判断，如果OnTouchListener不为null且返回true，则表示事件被消费，onTouchEvent不会被执行；否则执行onTouchEvent。 


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
>2）闪烁文字  
>3）背景渐变色设置  
>4）转菊花  

![](/Demo/docs/image/2017-07-30-Shader.png)

## 6. 读取assets中Xml解析展示Emoji
>1）解析特定格式xml文件  
2）LruCache<String, Bitmap>，缓存assets中的图片

![](/Demo/docs/image/2017-07-30-XmlEmojiParse.png)

## 7. 富文本展示
>1）ImageSpan居中设置背景文本  
2）详情参考：[Android Canvas drawText 实现文字垂直居中及其背景绘制](https://genzzhang.github.io/Android-Canvas-drawText实现文字垂直居中及其背景绘制/)

![](/Demo/docs/image/2017-08-07-RichTexts.png)

## 8.嵌套滑动吸顶页面

> 1）自定义嵌套滑动控件，解决ScrollView嵌套ListView性能的问题  
> 2）页面吸顶效果实现

![](/Demo/docs/image/2018-10-19-NestScrollSuction.png)


















非科班生从0到1学习Android笔记专用 
For details, see [Blog](https://genzzhang.github.io/).