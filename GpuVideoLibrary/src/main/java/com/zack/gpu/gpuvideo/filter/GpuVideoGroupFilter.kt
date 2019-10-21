package com.zack.gpu.gpuvideo.filter

import android.content.Context
import android.opengl.GLES20
import com.zack.gpu.gpuvideo.param.Rotation
import com.zack.gpu.gpuvideo.util.TextureRotationUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*


/**
 * @Author zack
 * @Date 2019-09-22
 * @Description 多滤镜的叠加
 * @Version 1.0
 */
class GpuVideoGroupFilter(context: Context): GPUVideoFilter(context) {

    //布局矩阵
    private val mCube = floatArrayOf(-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f)
    //当前滤镜列表
    private var mFilters: MutableList<GPUVideoFilter> = ArrayList()
    //merge的滤镜
    private var mMergedFilters: MutableList<GPUVideoFilter> = ArrayList()
    //frame_buffer
    private var mFrameBuffers: IntArray? = null
    //frame_texture
    private var mFrameBufferTextures: IntArray? = null

    //CUBE_BUFFER
    private val mGLCubeBuffer: FloatBuffer
    //TEXTURE_BUFFER
    private val mGLTextureBuffer: FloatBuffer
    //FLIP_BUFFER
    private val mGLTextureFlipBuffer: FloatBuffer

    init {
        mGLCubeBuffer = ByteBuffer.allocateDirect(mCube.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mGLCubeBuffer.put(mCube).position(0)
        mGLTextureBuffer = ByteBuffer.allocateDirect(8 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mGLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL)).position(0)
        val flipTexture = TextureRotationUtil.getRotation(Rotation.NORMAL, flipHorizontal = false, flipVertical = true)
        mGLTextureFlipBuffer = ByteBuffer.allocateDirect(flipTexture.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mGLTextureFlipBuffer.put(flipTexture).position(0)
    }

    /**
     * 初始化
     */
    override fun onInit() {
        super.onInit()
        for (filter in mFilters){
            filter.initFilter()
        }
    }

    /**
     * 设置界面大小
     */
    override fun setRenderSize(width: Int, height: Int) {
        super.setRenderSize(width, height)
        if (mFrameBuffers != null) {
            destroyTextureBuffer()
        }
        mFilters.forEach {
            it.setRenderSize(width, height)
        }
        if (mMergedFilters.isNotEmpty()){
            mFrameBuffers = IntArray(mMergedFilters.size - 1)
            mFrameBufferTextures = IntArray(mMergedFilters.size - 1)
            for (index in 0 until mMergedFilters.size-1){
                GLES20.glGenFramebuffers(1, mFrameBuffers, index)
                GLES20.glGenTextures(1, mFrameBufferTextures, index)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures!![index])
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers!![index])
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, mFrameBufferTextures!![index], 0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
            }
        }
    }

    /**
     * 绘制texture
     */
    override fun onDraw(textureId: Int, cubeBuffer: FloatBuffer, textureBuffer: FloatBuffer) {
        runPendingOnDrawTasks()
        if (!isInitialized() || mFrameBuffers == null || mFrameBufferTextures == null) return
        var previousTexture = textureId
        val size = mMergedFilters.size
        for (index in 0 until size) {
            val filter = mMergedFilters[index]
            val isNotLast = index < size - 1
            if (isNotLast) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers!![index])
                GLES20.glClearColor(0f, 0f, 0f, 0f)
            }
            when (index) {
                0 -> {
                    filter.onDraw(previousTexture, cubeBuffer, textureBuffer)
                }
                size - 1 -> {
                    filter.onDraw(previousTexture,mGLCubeBuffer,
                        if (size % 2 == 0) mGLTextureFlipBuffer else mGLTextureBuffer)
                }
                else -> {
                    filter.onDraw(previousTexture, mGLCubeBuffer, mGLTextureBuffer)
                }
            }
            if (isNotLast) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
                previousTexture = mFrameBufferTextures!![index]
            }
        }
    }


    /**
     * 销毁滤镜
     */
    override fun destroyFilter() {
        destroyTextureBuffer()
        for (filter in mFilters){
            filter.destroyFilter()
        }
        super.destroyFilter()
    }

    /**
     * 销毁buffer
     */
    private fun destroyTextureBuffer() {
        mFrameBufferTextures?.let {
            GLES20.glDeleteTextures(it.size, it, 0)
        }
        mFrameBuffers?.let {
            GLES20.glDeleteFramebuffers(it.size, it, 0)
        }
    }

    /**
     * 更新滤镜列表
     */
    private fun updateMergedFilters() {
        mMergedFilters.clear()
        var filters: List<GPUVideoFilter>
        for (filter in mFilters) {
            if (filter is GpuVideoGroupFilter) {
                filter.updateMergedFilters()
                filters = filter.mMergedFilters
                if (filters.isEmpty()) continue
                mMergedFilters.addAll(filters)
            }else{
                mMergedFilters.add(filter)
            }
        }
    }

    /**
     * 添加滤镜
     */
    fun addFilter(aFilter: GPUVideoFilter) {
        mFilters.add(aFilter)
        updateMergedFilters()
    }


}