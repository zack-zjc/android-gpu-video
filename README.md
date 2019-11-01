## AndroidGPUVideo
安卓视频添加滤镜使用GLSurface实现视频渲染，可自行添加播放器实现播放,其他参数滤镜等添加可自行查看代码方法设置

```groovy
	//添加surface回调，实现最简单的播放
	id_surface.setSurfaceListener(object :SurfaceListener{
            override fun onSurfaceAvailable(surfaceTexture: SurfaceTexture) {
                val mediaPlayer = MediaPlayer.create(this@MainActivity,R.raw.test)
                mediaPlayer.setSurface(Surface(surfaceTexture))
				//设置视频大小
                id_surface.setVideoSize(mediaPlayer.videoWidth,mediaPlayer.videoHeight)
                mediaPlayer.start()
            }
        })
```	

## 功能

### 功能实现参照-添加的视频滤镜
[GPUImage](https://github.com/cats-oss/android-gpuimage)

### 滤镜添加可参照GPUImage实现
1.基础滤镜无特效 (GPUVideoFilter.kt)

2.参照图片实现特效与GPUImage的LookUpFilter一样的使用方式 (GpuVideoLookUpFilter.kt)

3.滤镜组合特效 (GpuVideoGroupFilter.kt)

4.自定义添加美颜滤镜 （GPUVideoBeautyFilter.kt）

### 参数功能
1.设置画面比例 （9-16,1-1,16-9，默认，可自行添加设定）

2.设置视频裁剪样式（Fit-Center,Center-crop，可自行添加设定）

3.设置旋转角度 （0，90，180，270，角度可自行添加）

### 说明
1.功能扩展可查看代码自行添加，有注释可在GpuRender.adjustVideoSize中修改

## 注意
自定义滤镜时视频播放器的片元着色器与GPUImage的着色器存在差异，主要注意samplerExternalOES与sampler2D的差异，参考具体代码实现

视频画面的渲染使用的是Android的拓展纹理#extension GL_OES_EGL_image_external : require,我们已经知道，视频的画面色彩空间是YUV，而要显示到屏幕上，画面是RGB的，所以，要把视频画面渲染到屏幕上，必须把YUV转换为RGB。拓展纹理就起到了这个转换的作用。着色器中纹理单元也换成了拓展纹理单元。
部分说明可参照：https://juejin.im/post/5db94f73e51d452a401ce102

```groovy
	//视频着色器
	#extension GL_OES_EGL_image_external : require
	varying highp vec2 textureCoordinate;
	uniform samplerExternalOES inputTexture;
	void main(){
		gl_FragColor = texture2D(inputTexture, textureCoordinate);
	}
```	
```groovy
	//GPUImage的着色器
	varying highp vec2 textureCoordinate;
	uniform sampler2D inputTexture;
	void main(){
		gl_FragColor = texture2D(inputTexture, textureCoordinate);
	}
```	

### Demo功能
<img src="https://github.com/zack-zjc/android-gpu-video/blob/master/1.jpg?raw=true"  height="720" width="360">
