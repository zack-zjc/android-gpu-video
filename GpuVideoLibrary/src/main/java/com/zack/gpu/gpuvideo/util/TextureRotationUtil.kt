package com.zack.gpu.gpuvideo.util

import com.zack.gpu.gpuvideo.param.Rotation


/**
 * @Author zack
 * @Date 2019-09-21
 * @Description 旋转是的util
 * @Version 1.0
 */
object TextureRotationUtil {

    private val TEXTURE_NO_ROTATION = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    private val TEXTURE_ROTATED_90 = floatArrayOf(1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f)
    private val TEXTURE_ROTATED_180 = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f)
    private val TEXTURE_ROTATED_270 = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)

    /**
     * 获取旋转角度的矩阵
     */
    fun getRotation(rotation: Rotation, flipHorizontal: Boolean = false, flipVertical: Boolean = false): FloatArray {
        var rotatedTex: FloatArray
        rotatedTex = when (rotation) {
            Rotation.ROTATION_90 -> TEXTURE_ROTATED_90
            Rotation.ROTATION_180 -> TEXTURE_ROTATED_180
            Rotation.ROTATION_270 -> TEXTURE_ROTATED_270
            Rotation.NORMAL -> TEXTURE_NO_ROTATION
        }
        if (flipHorizontal) {
            rotatedTex = floatArrayOf(flip(rotatedTex[0]),rotatedTex[1],
                flip(rotatedTex[2]),rotatedTex[3],
                flip(rotatedTex[4]),rotatedTex[5],
                flip(rotatedTex[6]),rotatedTex[7])
        }
        if (flipVertical) {
            rotatedTex = floatArrayOf(rotatedTex[0],flip(rotatedTex[1]),
                rotatedTex[2],flip(rotatedTex[3]),
                rotatedTex[4],flip(rotatedTex[5]),
                rotatedTex[6],flip(rotatedTex[7]))
        }
        return rotatedTex
    }

    /**
     * 翻转位置
     */
    private fun flip(i: Float): Float = if (i == 0.0f) 1.0f else 0.0f

}