package com.lzs.rockerview;

/**
 * title：OnShakeListener
 * author: zsliu
 * created by Administrator on 2022/2/15 10:08
 * description: 摇动杆方向监听
 */
public interface OnShakeListener {
    // 开始
    void onStart();

    //摇动方向
    void direction(Direction direction);

    // 结束
    void onFinish();
}
