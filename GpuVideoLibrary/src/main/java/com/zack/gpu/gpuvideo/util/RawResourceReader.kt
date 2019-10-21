package com.zack.gpu.gpuvideo.util

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


/**
 * @Author zack
 * @Date 2019/9/19
 * @Description 文本读取
 * @Version 1.0
 */
object RawResourceReader {

    fun readTextFileFromRawResource(context: Context,resourceId: Int): String {
        val inputStream = context.resources.openRawResource(resourceId)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        val body = StringBuilder()
        try {
            var nextLine = bufferedReader.readLine()
            while (nextLine != null) {
                body.append(nextLine)
                body.append('\n')
                nextLine = bufferedReader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return body.toString()
    }

}