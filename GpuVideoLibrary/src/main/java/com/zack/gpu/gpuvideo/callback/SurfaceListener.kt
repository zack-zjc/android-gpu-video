package com.zack.gpu.gpuvideo.callback

import android.graphics.SurfaceTexture


/**
 * @Author zack
 * @Date 2019-09-21
 * @Description
 * @Version 1.0
 */
interface SurfaceListener {

    fun onSurfaceAvailable(surfaceTexture: SurfaceTexture)

}