package com.zack.gpu.gpuvideo.filter

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.zack.gpu.gpuvideo.R
import com.zack.gpu.gpuvideo.util.OpenGlUtils
import com.zack.gpu.gpuvideo.util.RawResourceReader
import java.nio.FloatBuffer
import java.util.*


/**
 * @Author zack
 * @Date 2019-09-21
 * @Description 模仿gpuImageFilter实现的filter
 * @Version 1.0
 */
open class GPUVideoFilter(context: Context) {

    //vertexShader
    private var mVertexShader: String = RawResourceReader.readTextFileFromRawResource(context, R.raw.normal_vertex_shader)
    //着色器
    private var mFragmentShader: String = RawResourceReader.readTextFileFromRawResource(context,R.raw.normal_fragment_shader)
    //ProgramId
    private var mGLProgramId: Int = 0
    //Position
    private var mGLPosition: Int = 0
    //UniformTexture
    private var mGLUniformTexture: Int = 0
    //TextureCoordinate
    private var mGLTextureCoordinate: Int = 0
    //渲染宽度
    private var mOutputWidth: Int = 0
    //渲染高度
    private var mOutputHeight: Int = 0
    //是否初始化
    private var mIsInitialized: Boolean = false
    //渲染线程
    private val mRunOnDraw: LinkedList<Runnable> = LinkedList()

    /**
     * 设置fragmentShader
     */
    fun setFragmentShader(shader:String){
        this.mFragmentShader = shader
    }

    /**
     * 设置shader
     */
    fun setVertexShader(shader: String){
        this.mVertexShader = shader
    }

    /**
     * 初始化滤镜
     */
    fun initFilter() {
        onInit()
        mIsInitialized = true
        onInitialized()
    }

    /**
     * 初始化program
     */
    open fun onInit() {
        mGLProgramId = OpenGlUtils.loadProgram(mVertexShader, mFragmentShader)
        mGLPosition = GLES20.glGetAttribLocation(mGLProgramId, "position")
        mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgramId, "inputTexture")
        mGLTextureCoordinate = GLES20.glGetAttribLocation(mGLProgramId,"inputTextureCoordinate")
    }

    /**
     *
     * 其他初始化事件
     */
    open fun onInitialized() = Unit

    /**
     * destroy当前filter
     */
    open fun destroyFilter() {
        mIsInitialized = false
        GLES20.glDeleteProgram(mGLProgramId)
    }

    /**
     * 设置当前渲染宽高
     */
    open fun setRenderSize(width: Int, height: Int) {
        mOutputWidth = width
        mOutputHeight = height
    }

    /**
     * 渲染滤镜
     */
    open fun onDraw(textureId: Int, cubeBuffer: FloatBuffer,textureBuffer: FloatBuffer) {
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        }
        GLES20.glUseProgram(mGLProgramId)
        runPendingOnDrawTasks()
        if (!mIsInitialized) return
        GLES20.glUniform1i(mGLUniformTexture, 0)
        cubeBuffer.position(0)
        GLES20.glVertexAttribPointer(mGLPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer)
        GLES20.glEnableVertexAttribArray(mGLPosition)
        textureBuffer.position(0)
        GLES20.glVertexAttribPointer(mGLTextureCoordinate, 2, GLES20.GL_FLOAT,
            false, 0,textureBuffer)
        GLES20.glEnableVertexAttribArray(mGLTextureCoordinate)
        onDrawArraysPre()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mGLPosition)
        GLES20.glDisableVertexAttribArray(mGLTextureCoordinate)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }

    /**
     * 渲染界面前的操作用于子类扩展使用
     */
    open fun onDrawArraysPre() = Unit

    /**
     * 执行渲染线程
     */
    fun runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run()
        }
    }

    /**
     * 是否初始化
     */
    fun isInitialized(): Boolean = mIsInitialized

    /**
     * 获取渲染宽度
     */
    fun getOutputWidth(): Int = mOutputWidth

    /**
     * 获取渲染高度
     */
    fun getOutputHeight(): Int = mOutputHeight

    /**
     * 获取program
     */
    fun getProgram(): Int = mGLProgramId

    /**
     * 设置着色器中对象float值
     */
    fun setFloat(location: Int, floatValue: Float) {
        runOnDraw(Runnable { GLES20.glUniform1f(location, floatValue) })
    }

    /**
     * 设置着色器中对象组值float值
     */
    fun setFloatVec2(location: Int, arrayValue: FloatArray) {
        runOnDraw(Runnable {
            GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue))
        })
    }

    /**
     * 设置着色中数组值
     */
    fun setFloatVec3(location: Int, arrayValue: FloatArray) {
        runOnDraw(Runnable { GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue)) })
    }

    /**
     * 设置着色器中对象组值float值
     */
    fun setFloatVec4(location: Int, arrayValue: FloatArray) {
        runOnDraw(Runnable {
            GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue))
        })
    }

    /**
     * 设置着色器中3维矩阵的值
     */
    fun setUniformMatrix3f(location: Int, matrix: FloatArray) {
        runOnDraw(Runnable { GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0) })
    }

    /**
     * 设置着色器中4维矩阵的值
     */
    fun setUniformMatrix4f(location: Int, matrix: FloatArray) {
        runOnDraw(Runnable { GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0) })
    }

    /**
     * 添加绘制线程
     */
    fun runOnDraw(runnable: Runnable) {
        synchronized(mRunOnDraw) {
            mRunOnDraw.addLast(runnable)
        }
    }

}