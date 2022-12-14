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
                Log.e("FileTool","??????????????????:" + fileName + "????????????")
                false
            } else {
                if (file.isFile()) deleteFile(fileName) else deleteDirectory(fileName)
            }
        }

        fun deleteFile(fileName: String): Boolean {
            val file = File(fileName)
            // ????????????????????????????????????????????????????????????????????????????????????
            return if (file.exists() && file.isFile) {
                if (file.delete()) {
                    true
                } else {
                    Log.e("FileTool","??????????????????" + fileName + "?????????")
                    false
                }
            } else {
                Log.e("FileTool","???????????????????????????" + fileName + "????????????")
                false
            }
        }

        fun deleteDirectory(dir: String): Boolean {
            // ??????dir?????????????????????????????????????????????????????????
            var dir = dir
            if (!dir.endsWith(File.separator)) dir = dir + File.separator
            val dirFile = File(dir)
            // ??????dir???????????????????????????????????????????????????????????????
            if (!dirFile.exists() || !dirFile.isDirectory) {
                Log.e("FileTool","?????????????????????" + dir + "????????????")
                return false
            }
            var flag = true
            // ????????????????????????????????????????????????
            val files = dirFile.listFiles()
            for (i in files.indices) {
                // ???????????????
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
                Log.e("FileTool","?????????????????????")
                return false
            }
            // ??????????????????
            return dirFile.delete()
        }
    }
}