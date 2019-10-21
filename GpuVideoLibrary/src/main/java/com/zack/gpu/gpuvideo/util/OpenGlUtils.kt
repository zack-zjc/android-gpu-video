package com.zack.gpu.gpuvideo.util

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils


/**
 * @Author zack
 * @Date 2019-09-21
 * @Description
 * @Version 1.0
 */
object OpenGlUtils {

    const val NO_TEXTURE = -1

    /**
     * 加载图片到texture
     */
    fun loadTexture(img: Bitmap, usedTexId: Int = NO_TEXTURE, recycle: Boolean = false): Int {
        val textures = IntArray(1)
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0)
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId)
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img)
            textures[0] = usedTexId
        }
        if (recycle) {
            img.recycle()
        }
        return textures[0]
    }

    /**
     * 加载shader
     */
    private fun loadShader(strSource: String, iType: Int): Int {
        val compiled = IntArray(1)
        val iShader = GLES20.glCreateShader(iType)
        GLES20.glShaderSource(iShader, strSource)
        GLES20.glCompileShader(iShader)
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) return 0
        return iShader
    }

    /**
     * 加载program
     */
    fun loadProgram(strVSource: String, strFSource: String): Int {
        val iVShader: Int = loadShader(strVSource, GLES20.GL_VERTEX_SHADER)
        val iFShader: Int = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER)
        val program: Int = GLES20.glCreateProgram()
        val link = IntArray(1)
        if (iVShader == 0 || iFShader == 0) return 0
        GLES20.glAttachShader(program, iVShader)
        GLES20.glAttachShader(program, iFShader)
        GLES20.glLinkProgram(program)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, link, 0)
        if (link[0] <= 0) return 0
        GLES20.glDeleteShader(iVShader)
        GLES20.glDeleteShader(iFShader)
        return program
    }
}