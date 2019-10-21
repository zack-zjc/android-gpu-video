package com.zack.gpu.gpuvideo.filter

import android.content.Context
import android.opengl.GLES20
import com.zack.gpu.gpuvideo.R
import com.zack.gpu.gpuvideo.util.RawResourceReader

/**
 * @Author zack
 * @Date 2019/9/5
 * @Description 美颜的filter
 * @Version 1.0
 */
class GPUVideoBeautyFilter(context:Context) : GPUVideoFilter(context) {

    private var toneLevel: Float = 0.toFloat()
    private var beautyLevel: Float = 0.toFloat()
    private var brightLevel: Float = 0.toFloat()
    private var paramsLocation: Int = 0
    private var brightnessLocation: Int = 0
    private var singleStepOffsetLocation: Int = 0
    //透明度
    var alphaValue: Int = 0

    init {
        setFragmentShader(RawResourceReader.readTextFileFromRawResource(context, R.raw.video_beauty_fragment_shader))
    }

    override fun onInit() {
        super.onInit()
        paramsLocation = GLES20.glGetUniformLocation(getProgram(), "params")
        brightnessLocation = GLES20.glGetUniformLocation(getProgram(), "brightness")
        singleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset")
        alphaValue = GLES20.glGetUniformLocation(getProgram(), "alphaValue")
        toneLevel = 0.47f
        beautyLevel = 0.42f
        brightLevel = 0.34f
        setParams(beautyLevel, toneLevel)
        setBrightLevel(brightLevel)
        setFilterAlpha(1.0F)
    }

    fun setBeautyLevel(beautyLevel: Float) {
        this.beautyLevel = beautyLevel
        setParams(beautyLevel, toneLevel)
    }

    private fun setBrightLevel(brightLevel: Float) {
        this.brightLevel = brightLevel
        setFloat(brightnessLocation, 0.6f * (-0.5f + brightLevel))
    }

    /**
     * 设置透明度0-1
     */
    fun setFilterAlpha(alpha:Float){
        setFloat(alphaValue,alpha)
    }

    private fun setParams(beauty: Float, tone: Float) {
        val vector = FloatArray(4)
        vector[0] = 1.0f - 0.6f * beauty
        vector[1] = 1.0f - 0.3f * beauty
        vector[2] = 0.1f + 0.3f * tone
        vector[3] = 0.1f + 0.3f * tone
        setFloatVec4(paramsLocation, vector)
    }

    private fun setTexelSize(w: Float, h: Float) {
        setFloatVec2(singleStepOffsetLocation, floatArrayOf(2.0f / w, 2.0f / h))
    }

    override fun setRenderSize(width: Int, height: Int) {
        super.setRenderSize(width, height)
        setTexelSize(width.toFloat(), height.toFloat())
    }

}