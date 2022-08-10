package com.pp.app.tool

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

class FileTool {
    companion object {

        fun copyScript(context: Context, script_path: Uri): String? {
            val root_path = context.getExternalFilesDir("scripts");
            delete(root_path.absolutePath)
            root_path.mkdirs();

            val filename = File(script_path.path).name
            val newFile = File(root_path.absolutePath, filename);
            val inputStream = context.contentResolver.openInputStream(script_path)
            val outputStream = FileOutputStream(newFile)

            //write
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream.available()
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()

            if (newFile.extension == "zip") {
                ZipFile(newFile).use { zip ->
                    zip.entries().asSequence().forEach { entry ->
                        zip.getInputStream(entry).use { input ->
                            val filePath = root_path.absolutePath + File.separator + entry.name
                            if (!entry.isDirectory) {
                                // if the entry is a file, extracts it
                                extractFile(input, filePath)
                            } else {
                                // if the entry is a directory, make the directory
                                val dir = File(filePath)
                                dir.mkdir()
                            }

                        }

                    }
                }
                val newFile = File(root_path.absolutePath, "main.js");
                if (newFile.exists()) {
                    return newFile.absolutePath
                }
                Log.d("MainActivity", "================ copyScript fail, root_path.absolutePath = " + root_path.absolutePath);
                return null
            } else {
                return newFile.absolutePath
            }
        }

        private fun extractFile(inputStream: InputStream, destFilePath: String) {
            val bos = BufferedOutputStream(FileOutputStream(destFilePath))
            val bytesIn = ByteArray(4096)
            var read: Int
            while (inputStream.read(bytesIn).also { read = it } != -1) {
                bos.write(bytesIn, 0, read)
            }
            bos.close()
        }

        fun delete(fileName: String): Boolean {
            val file = File(fileName)
            return if (!file.exists()) {
                Log.e("FileTool","删除文件失败:" + fileName + "不存在！")
                false
            } else {
                if (file.isFile()) deleteFile(fileName) else deleteDirectory(fileName)
            }
        }

        fun deleteFile(fileName: String): Boolean {
            val file = File(fileName)
            // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
            return if (file.exists() && file.isFile) {
                if (file.delete()) {
                    true
                } else {
                    Log.e("FileTool","删除单个文件" + fileName + "失败！")
                    false
                }
            } else {
                Log.e("FileTool","删除单个文件失败：" + fileName + "不存在！")
                false
            }
        }

        fun deleteDirectory(dir: String): Boolean {
            // 如果dir不以文件分隔符结尾，自动添加文件分隔符
            var dir = dir
            if (!dir.endsWith(File.separator)) dir = dir + File.separator
            val dirFile = File(dir)
            // 如果dir对应的文件不存在，或者不是一个目录，则退出
            if (!dirFile.exists() || !dirFile.isDirectory) {
                Log.e("FileTool","删除目录失败：" + dir + "不存在！")
                return false
            }
            var flag = true
            // 删除文件夹中的所有文件包括子目录
            val files = dirFile.listFiles()
            for (i in files.indices) {
                // 删除子文件
                if (files[i].isFile) {
                    flag = deleteFile(files[i].absolutePath)
                    if (!flag) break
                } else if (files[i].isDirectory) {
                    flag = deleteDirectory(
                        files[i]
                            .absolutePath
                    )
                    if (!flag) break
                }
            }
            if (!flag) {
                Log.e("FileTool","删除目录失败！")
                return false
            }
            // 删除当前目录
            return dirFile.delete()
        }
    }
}