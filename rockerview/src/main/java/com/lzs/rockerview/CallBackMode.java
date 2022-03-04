package com.lzs.rockerview;

/**
 * title：CallBackMode
 * author: zsliu
 * created by Administrator on 2022/2/15 10:29
 * description: 回调模式
 */
public enum CallBackMode {
    // 有移动就立刻回调
    CALL_BACK_MODE_MOVE,
    // 只有状态变化的时候才回调
    CALL_BACK_MODE_STATE_CHANGE,
    //只有状态变化或者距离变化的时候才回调
    CALL_BACK_MODE_STATE_DISTANCE_CHANGE
}
