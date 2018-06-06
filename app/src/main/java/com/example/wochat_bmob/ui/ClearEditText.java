package com.example.wochat_bmob.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;


import com.example.wochat_bmob.R;

/**
 * 增加一个清除图标的EditText
 * Created by 邹永鹏 on 2018/4/30.
 */

public class ClearEditText extends AppCompatEditText {

    /*清除图标，即清除文本框内容*/
    private Drawable mClearIcon;

    /*是否显示清除图标
    * 一开始不显示，当文本框中存在内容才显示*/
    private boolean mClearIconVisible=false;

    public ClearEditText(Context context){
        super(context);
        initView(context);
    }

    public ClearEditText(Context context, AttributeSet attrs){
        super(context,attrs);
        initView(context);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        mClearIcon = getResources().getDrawable(R.drawable.edittext_clear_icon);
        /*setBounds(x,y,width,height)
        x:组件在容器X轴上的起点
        y:组件在容器Y轴上的起点
        width:组件的长度 height:组件的高度*/
        mClearIcon.setBounds(0,0,mClearIcon.getMinimumWidth(),mClearIcon.getMinimumHeight());

        /*文本监听事件*/
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            /*文本变化时*/
            @Override
            public void afterTextChanged(Editable editable) {
                String text=getText().toString();
                if (isFocused() && !text.isEmpty()){//处在焦点的文本不为空时
                    if (!mClearIconVisible){//清除图标不显示时
                        /*可以在上、下、左、右设置图标，如果不想在某个地方显示，则设置为null
                        * 注意：要添加的资源必须已经setBounds(Rect)，即已经设置过初始位置、宽和高等信息
                        * */
                        setCompoundDrawables(null,null,mClearIcon,null);
                        mClearIconVisible=true;
                    }
                }else {
                    setCompoundDrawables(null,null,null,null);
                    mClearIconVisible=false;
                }
            }
        });
    }

    /*清除图标点击事件*/
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*清除图标可见，当点击清除图标后松开时出发
        * MotionEvent.ACTION_UP：当最后一个触点松开时被触发。
        * MotionEvent.ACTION_CANCEL：触发事件结束*/
        if (mClearIconVisible && mClearIcon!=null && event.getAction()==MotionEvent.ACTION_UP){
            if (event.getX()> getWidth()-getPaddingRight()-mClearIcon.getIntrinsicWidth()){//判断清除图标的位置
                setText("");
                event.setAction(MotionEvent.ACTION_CANCEL);
            }
        }
        return super.onTouchEvent(event);
    }
}
