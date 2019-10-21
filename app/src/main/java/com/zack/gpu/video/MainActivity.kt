package com.zack.gpu.video

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import com.zack.gpu.gpuvideo.callback.SurfaceListener
import com.zack.gpu.gpuvideo.param.Rotation
import com.zack.gpu.gpuvideo.param.SurfaceRatio
import com.zack.gpu.gpuvideo.param.SurfaceScaleType
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SurfaceListener {

    var rotation:Rotation = Rotation.NORMAL

    var surfaceRatio = SurfaceRatio.RATIO_16_9

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        id_surface.setSurfaceListener(this)
        fit.setOnClickListener {
            id_surface.setScaleType(SurfaceScaleType.SCALE_TYPE_FIT)
        }
        crop.setOnClickListener {
            id_surface.setScaleType(SurfaceScaleType.SCALE_TYPE_CENTER_CROP)
        }
        rotate.setOnClickListener {
            when(rotation){
                Rotation.ROTATION_90 ->{
                    id_surface.setRotation(Rotation.ROTATION_180)
                    rotation = Rotation.ROTATION_180
                }
                Rotation.ROTATION_180 ->{
                    id_surface.setRotation(Rotation.ROTATION_270)
                    rotation = Rotation.ROTATION_270
                }
                Rotation.ROTATION_270 ->{
                    id_surface.setRotation(Rotation.NORMAL)
                    rotation = Rotation.NORMAL
                }
                else ->{
                    id_surface.setRotation(Rotation.ROTATION_90)
                    rotation = Rotation.ROTATION_90
                }
            }
        }
        surface.setOnClickListener {
            when(surfaceRatio){
                SurfaceRatio.RATIO_16_9 -> {
                    id_surface.setSurfaceRatio(SurfaceRatio.RATIO_1_1)
                    surfaceRatio = SurfaceRatio.RATIO_1_1
                }
                SurfaceRatio.RATIO_1_1 -> {
                    id_surface.setSurfaceRatio(SurfaceRatio.RATIO_9_16)
                    surfaceRatio = SurfaceRatio.RATIO_9_16
                }
                else ->{
                    surfaceRatio = SurfaceRatio.RATIO_16_9
                    id_surface.setSurfaceRatio(SurfaceRatio.RATIO_16_9)
                }
            }
        }
    }

    override fun onSurfaceAvailable(surfaceTexture: SurfaceTexture) {
        val mediaPlayer = MediaPlayer.create(this,R.raw.test)
        mediaPlayer.setSurface(Surface(surfaceTexture))
        id_surface.setVideoSize(mediaPlayer.videoWidth,mediaPlayer.videoHeight)
        mediaPlayer.start()
    }
}
