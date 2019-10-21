package com.zack.gpu.gpuvideo.param

/**
 * @Author zack
 * @Date 2019/10/14
 * @Description 画布比例
 * @Version 1.0
 */
enum class SurfaceRatio {

    RATIO_UNKNOWN,

    RATIO_16_9,

    RATIO_9_16,

    RATIO_1_1;

    /**
     * 获取比例值
     */
    fun getRatio():Float = when(this){
        RATIO_UNKNOWN -> 0F
        RATIO_16_9 -> 16*1f/9
        RATIO_9_16 -> 9*1f/16
        RATIO_1_1 -> 1F
    }

}