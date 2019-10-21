package com.zack.gpu.gpuvideo

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.zack.gpu.gpuvideo.callback.SurfaceListener
import com.zack.gpu.gpuvideo.filter.GPUVideoFilter
import com.zack.gpu.gpuvideo.param.Rotation
import com.zack.gpu.gpuvideo.param.SurfaceRatio
import com.zack.gpu.gpuvideo.param.SurfaceScaleType
import com.zack.gpu.gpuvideo.util.OpenGlUtils
import com.zack.gpu.gpuvideo.util.TextureRotationUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * @Author zack
 * @Date 2019-09-21
 * @Description surface_render
 * @Version 1.0
 */
class GpuRender(context: Context, private val mSurfaceListener: SurfaceListener): GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    //布局矩阵
    private val mCube = floatArrayOf(-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f)
    //滤镜
    private var mFilter: GPUVideoFilter = GPUVideoFilter(context)
    //渲染textureId
    private var mGLTextureId = OpenGlUtils.NO_TEXTURE
    //surface
    private var mSurfaceTexture: SurfaceTexture? = null
    //CUBE_BUFFER
    private val mGLCubeBuffer: FloatBuffer
    //TEXTURE_BUFFER
    private val mGLTextureBuffer: FloatBuffer
    //画布宽度
    private var mSurfaceWidth: Int = 0
    //画布高度
    private var mSurfaceHeight: Int = 0
    //渲染视频宽度
    private var mRenderWidth: Int = 0
    //渲染高度
    private var mRenderHeight: Int = 0
    //视频宽度
    private var mVideoWidth:Int = 0
    //视频高度
    private var mVideoHeight:Int = 0
    //画布信息变化修改
    private var mSurfaceChange:Boolean = true
    //画布的比例
    private var surfaceRatio:SurfaceRatio = SurfaceRatio.RATIO_UNKNOWN
    //画布的填充模式
    private var surfaceScaleType:SurfaceScaleType = SurfaceScaleType.SCALE_TYPE_FIT
    //旋转角度
    private var mRotation:Rotation = Rotation.NORMAL
    //绘制线程集合
    private val mRunOnDraw: Queue<Runnable> = LinkedList()
    //绘制线程完成操作集合
    private val mRunOnDrawEnd: Queue<Runnable> = LinkedList()
    //默认颜色R值
    private var mBackgroundRed = 0.15f
    //默认颜色G值
    private var mBackgroundGreen = 0.15f
    //默认颜色B值
    private var mBackgroundBlue = 0.15f
    //可用于旋转等界面操作
    private val mStMatrix = FloatArray(16)


    init {
        mGLCubeBuffer = ByteBuffer.allocateDirect(mCube.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mGLCubeBuffer.put(mCube).position(0)
        mGLTextureBuffer = ByteBuffer.allocateDirect(8 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mGLTextureBuffer.put(TextureRotationUtil.getRotation(mRotation))
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(mBackgroundRed, mBackgroundGreen, mBackgroundBlue, 1f)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        val mTextureID = IntArray(1)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glGenTextures(1, mTextureID, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID[0])
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        mGLTextureId = mTextureID[0]
        mSurfaceTexture = SurfaceTexture(mGLTextureId)
        mSurfaceTexture?.setOnFrameAvailableListener(this)
        mFilter.initFilter()
        mSurfaceListener.onSurfaceAvailable(mSurfaceTexture!!)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mSurfaceWidth = width
        mSurfaceHeight = height
        initRenderSize(width,height)
        adjustVideoSize(this.mRotation)
        mFilter.setRenderSize(this.mRenderWidth, this.mRenderHeight)
        GLES20.glUseProgram(mFilter.getProgram())
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        processRenderSize()
        adjustVideoSize(this.mRotation)
        runAll(mRunOnDraw)
        //绘制滤镜
        mFilter.onDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer)
        runAll(mRunOnDrawEnd)
        mSurfaceTexture?.updateTexImage()
        mSurfaceTexture?.getTransformMatrix(mStMatrix)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) = Unit

    /**
     * 设置渲染区域
     */
    private fun initRenderSize(width: Int, height: Int){
        val surfaceRatio = this.surfaceRatio.getRatio()
        this.mRenderWidth = width
        this.mRenderHeight = if(surfaceRatio != 0F) (width/this.surfaceRatio.getRatio()).toInt() else height
        if (this.mRenderHeight > height){
            this.mRenderWidth = if(surfaceRatio != 0F) (height*this.surfaceRatio.getRatio()).toInt() else width
            this.mRenderHeight = height
        }
        val renderXOffset = (width - this.mRenderWidth)/2
        val renderYOffset = (height - this.mRenderHeight)/2
        GLES20.glViewport(renderXOffset, renderYOffset, this.mRenderWidth, this.mRenderHeight)
    }

    /**
     * 处理渲染区域是否发生变化
     */
    private fun processRenderSize(){
        val surfaceRatio = this.surfaceRatio.getRatio()
        var tempRenderWidth = this.mSurfaceWidth
        var tempRenderHeight = if(surfaceRatio != 0F) (this.mSurfaceWidth/this.surfaceRatio.getRatio()).toInt() else this.mSurfaceHeight
        if (tempRenderHeight > this.mSurfaceHeight){
            tempRenderWidth = if(surfaceRatio != 0F) (this.mSurfaceHeight*this.surfaceRatio.getRatio()).toInt() else this.mSurfaceWidth
            tempRenderHeight = this.mSurfaceHeight
        }
        if (tempRenderWidth != this.mRenderWidth || tempRenderHeight != this.mRenderHeight){
            initRenderSize(this.mSurfaceWidth,this.mSurfaceHeight)
        }
    }

    /**
     * 运行所以线程
     */
    private fun runAll(queue: Queue<Runnable>) {
        synchronized(queue) {
            while (!queue.isEmpty()) {
                queue.poll().run()
            }
        }
    }

    /**
     * 添加到线程
     */
    private fun runOnDraw(runnable: Runnable) {
        synchronized(mRunOnDraw) {
            mRunOnDraw.add(runnable)
        }
    }

    private fun addDistance(coordinate: Float, distance: Float): Float {
        return if (coordinate == 0.0f) distance else 1 - distance
    }

    /**
     * 调整视频渲染大小
     */
    private fun adjustVideoSize(rotation: Rotation){
        if (mVideoHeight == 0 || mVideoWidth == 0 || mRenderWidth == 0 || mRenderHeight == 0) return
        if (!mSurfaceChange) return
        mSurfaceChange = false
        var resultWidth = this.mRenderWidth.toFloat()
        var resultHeight = this.mRenderHeight.toFloat()
        if (rotation == Rotation.ROTATION_90 || rotation == Rotation.ROTATION_270){
            resultWidth = this.mRenderHeight.toFloat()
            resultHeight = this.mRenderWidth.toFloat()
        }
        val ratioWidth = resultWidth / mVideoWidth
        val ratioHeight = resultHeight / mVideoHeight
        val resultRatio = Math.max(ratioWidth,ratioHeight)
        var resultVideoWidth = Math.round(mVideoWidth * resultRatio)
        var resultVideoHeight = Math.round(mVideoHeight * resultRatio)
        if (rotation == Rotation.ROTATION_90 || rotation == Rotation.ROTATION_270){
            resultVideoWidth = Math.round(mVideoHeight * resultRatio)
            resultVideoHeight = Math.round(mVideoWidth * resultRatio)
        }
        val ratioX = resultVideoWidth.toFloat() / this.mRenderWidth
        val ratioY = resultVideoHeight.toFloat() / this.mRenderHeight
        val cube: FloatArray
        var textureCords = TextureRotationUtil.getRotation(rotation)
        when(surfaceScaleType){
            SurfaceScaleType.SCALE_TYPE_FIT ->{ //适应视频画面
                cube = floatArrayOf(mCube[0] / ratioY,mCube[1]/ratioX,mCube[2] / ratioY,mCube[3] / ratioX,
                    mCube[4] / ratioY,mCube[5]/ratioX,mCube[6] / ratioY,mCube[7] / ratioX)
            }
            SurfaceScaleType.SCALE_TYPE_CENTER_CROP ->{ //填充满画布
                cube = mCube
                val ratioHorizontal = if (rotation == Rotation.ROTATION_90 || rotation == Rotation.ROTATION_270) ratioY else ratioX
                val ratioVertical = if (rotation == Rotation.ROTATION_90 || rotation == Rotation.ROTATION_270) ratioX else ratioY
                val distHorizontal = (1 - 1 / ratioHorizontal) / 2
                val distVertical = (1 - 1 / ratioVertical) / 2
                textureCords = floatArrayOf(
                    addDistance(textureCords[0], distHorizontal),
                    addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal),
                    addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal),
                    addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal),
                    addDistance(textureCords[7], distVertical)
                )
            }
        }
        //调整渲染大小
        mGLCubeBuffer.clear()
        mGLCubeBuffer.put(cube).position(0)
        //调整绘制界面
        mGLTextureBuffer.clear()
        mGLTextureBuffer.put(textureCords).position(0)
    }

    /**
     * 设置视频宽高
     */
    fun setVideoSize(videoWidth:Int,videoHeight:Int){
        if (this.mVideoHeight != videoHeight || this.mVideoWidth != videoWidth){
            this.mVideoHeight = videoHeight
            this.mVideoWidth = videoWidth
            this.mSurfaceChange = true
        }
    }

    /**
     * 设置视频旋转角度
     */
    fun setRotation(rotation: Rotation){
        this.mRotation = rotation
        this.mSurfaceChange = true
    }

    /**
     * 设置视频适应模式
     */
    fun setScaleType(scaleType: SurfaceScaleType){
        this.surfaceScaleType = scaleType
        this.mSurfaceChange = true
    }

    /**
     * 获取视频填充模式
     */
    fun getScaleType(): SurfaceScaleType = this.surfaceScaleType

    /**
     * 设置画布比例
     */
    fun setSurfaceRatio(surfaceRatio: SurfaceRatio){
        this.surfaceRatio = surfaceRatio
        this.mSurfaceChange = true
    }

    /**
     * 获取画布比例
     */
    fun getSurfaceRatio(): SurfaceRatio = this.surfaceRatio

    /**
     * 设置滤镜
     */
    fun setFilter(filter: GPUVideoFilter) {
        runOnDraw(Runnable {
            val oldFilter = mFilter
            mFilter = filter
            oldFilter.destroyFilter()
            mFilter.initFilter()
            GLES20.glUseProgram(mFilter.getProgram())
            mFilter.setRenderSize(this.mRenderWidth, this.mRenderHeight)
        })
    }

    /**
     * 获取当前滤镜
     */
    fun getFilter():GPUVideoFilter = mFilter

}