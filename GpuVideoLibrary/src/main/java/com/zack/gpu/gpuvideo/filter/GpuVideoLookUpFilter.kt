package com.zack.gpu.gpuvideo.filter

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.zack.gpu.gpuvideo.R
import com.zack.gpu.gpuvideo.param.Rotation
import com.zack.gpu.gpuvideo.util.OpenGlUtils
import com.zack.gpu.gpuvideo.util.RawResourceReader
import com.zack.gpu.gpuvideo.util.TextureRotationUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * @Author zack
 * @Date 2019-09-22
 * @Description 解析图片的filter
 * @Version 1.0
 */
open class GpuVideoLookUpFilter(context:Context) :GPUVideoFilter(context){

    //滤镜的Coordinate
    var mFilterSecondTextureCoordinate: Int = 0
    //滤镜texture
    var mFilterInputTextureUniform2: Int = 0
    //透明度
    var alphaValue: Int = 0
    //滤镜图片的textureId
    var mFilterSourceTexture2 = OpenGlUtils.NO_TEXTURE
    //textureBuffer
    private var mTexture2CoordinatesBuffer: ByteBuffer
    //解析的图片
    private var filterBitmap: Bitmap? = null

    init {
        setVertexShader(RawResourceReader.readTextFileFromRawResource(context, R.raw.video_lookup_vertex_shader))
        setFragmentShader(RawResourceReader.readTextFileFromRawResource(context, R.raw.video_lookup_fragment_shader))
        val buffer = TextureRotationUtil.getRotation(Rotation.NORMAL)
        val bBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder())
        val fBuffer = bBuffer.asFloatBuffer()
        fBuffer.put(buffer)
        fBuffer.flip()
        mTexture2CoordinatesBuffer = bBuffer
    }

    /**
     * 初始化
     */
    override fun onInit() {
        super.onInit()
        mFilterSecondTextureCoordinate = GLES20.glGetAttribLocation(getProgram(),"inputTextureCoordinate2")
        mFilterInputTextureUniform2 = GLES20.glGetUniformLocation(getProgram(),"inputImageTexture")
        alphaValue = GLES20.glGetUniformLocation(getProgram(), "alphaValue")
        GLES20.glEnableVertexAttribArray(mFilterSecondTextureCoordinate)
        setFilterBitmap(filterBitmap)
        setFilterAlpha(1.0f)
    }

    /**
     * 绘制滤镜
     */
    override fun onDrawArraysPre() {
        super.onDrawArraysPre()
        GLES20.glEnableVertexAttribArray(mFilterSecondTextureCoordinate)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterSourceTexture2)
        GLES20.glUniform1i(mFilterInputTextureUniform2, 3)
        mTexture2CoordinatesBuffer.position(0)
        GLES20.glVertexAttribPointer(mFilterSecondTextureCoordinate,2,
            GLES20.GL_FLOAT,false,0,mTexture2CoordinatesBuffer)
    }

    /**
     * 销毁filter
     */
    override fun destroyFilter() {
        super.destroyFilter()
        GLES20.glDeleteTextures(1, intArrayOf(mFilterSourceTexture2), 0)
        mFilterSourceTexture2 = OpenGlUtils.NO_TEXTURE
    }

    /**
     * 设置透明度0-1
     */
    fun setFilterAlpha(alpha:Float){
        setFloat(alphaValue,alpha)
    }

    /**
     * 设置滤镜图片
     */
    open fun setFilterBitmap(bitmap: Bitmap?) {
        if (bitmap == null || bitmap.isRecycled) return
        this.filterBitmap = bitmap
        runOnDraw(Runnable {
            if (mFilterSourceTexture2 == OpenGlUtils.NO_TEXTURE) {
                filterBitmap?.let {
                    if (it.isRecycled) return@Runnable
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
                    mFilterSourceTexture2 = OpenGlUtils.loadTexture(it)
                }
            }
        })
    }

    /**
     * 回收bitmap
     */
    fun recycleBitmap(){
        if (filterBitmap != null && filterBitmap?.isRecycled == false) {
            filterBitmap?.recycle()
            filterBitmap = null
        }
    }
}