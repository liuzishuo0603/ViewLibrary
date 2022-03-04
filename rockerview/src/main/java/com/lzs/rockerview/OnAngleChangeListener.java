package com.lzs.rockerview;

/**
 * title：OnAngleChangeListener
 * author: zsliu
 * created by Administrator on 2022/2/14 16:37
 * description: 摇动杆角度监听
 */
public interface OnAngleChangeListener {
    // 开始
    void onStart();

    //摇杆角度变化    角度[0,360)
    void angle(double angle);

    // 结束
    void onFinish();
}
