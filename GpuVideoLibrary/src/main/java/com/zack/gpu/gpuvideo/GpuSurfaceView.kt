package com.zack.gpu.gpuvideo

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.zack.gpu.gpuvideo.callback.SurfaceListener
import com.zack.gpu.gpuvideo.filter.GPUVideoFilter
import com.zack.gpu.gpuvideo.param.Rotation
import com.zack.gpu.gpuvideo.param.SurfaceRatio
import com.zack.gpu.gpuvideo.param.SurfaceScaleType


/**
 * @Author zack
 * @Date 2019-09-21
 * @Description 展示滤镜的view
 * @Version 1.0
 */
class GpuSurfaceView @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null)
    : GLSurfaceView(context,attrs), SurfaceListener {

    //render
    private var mGpuRender = GpuRender(context,this)
    //surfaceListener
    private var mSurfaceListener: SurfaceListener? = null

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.RGBA_8888)
        setRenderer(mGpuRender)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onSurfaceAvailable(surfaceTexture: SurfaceTexture) {
        mSurfaceListener?.onSurfaceAvailable(surfaceTexture)
    }

    /**
     * 设置监听器
     */
    fun setSurfaceListener(listener: SurfaceListener){
        this.mSurfaceListener = listener
    }


    /**
     * 获取当前滤镜
     */
    fun getFilter(): GPUVideoFilter = mGpuRender.getFilter()

    /**
     * 设置滤镜
     */
    fun setFilter(filter: GPUVideoFilter) {
        mGpuRender.setFilter(filter)
    }

    /**
     * 设置旋转角度
     */
    fun setRotation(rotation: Rotation){
        mGpuRender.setRotation(rotation)
    }

    /**
     * 设置视频适应模式
     */
    fun setScaleType(scaleType: SurfaceScaleType){
        mGpuRender.setScaleType(scaleType)
    }

    /**
     * 获取视频适应模式
     */
    fun getScaleType():SurfaceScaleType = mGpuRender.getScaleType()

    /**
     * 设置画布比例
     */
    fun setSurfaceRatio(surfaceRatio: SurfaceRatio){
        mGpuRender.setSurfaceRatio(surfaceRatio)
    }

    /**
     * 获取画布比例
     */
    fun getSurfaceRatio(): SurfaceRatio = mGpuRender.getSurfaceRatio()

    /**
     * 设置视频大小
     */
    fun setVideoSize(videoWidth:Int,videoHeight:Int){
        mGpuRender.setVideoSize(videoWidth,videoHeight)
    }

}

