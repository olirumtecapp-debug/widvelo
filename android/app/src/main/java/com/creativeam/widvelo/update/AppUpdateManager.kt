package com.creativeam.widvelo.update

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class AppUpdateManager(
    private val versionEndpointUrl: String = "https://raw.githubusercontent.com/olirumtecapp-debug/widvelo/main/version.json"
) {

    suspend fun checkForUpdates(currentVersionCode: Int): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(versionEndpointUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "WidVelo-App-UpdateChecker")
                useCaches = false
            }

            if (connection.responseCode !in 200..299) {
                return@withContext null
            }

            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            val obj = JSONObject(jsonString)

            val remoteVersionCode = obj.optInt("versionCode", 0)
            if (remoteVersionCode <= currentVersionCode) {
                return@withContext null
            }

            val changelogList = mutableListOf<String>()
            val changelogArray = obj.optJSONArray("changelog")
            if (changelogArray != null) {
                for (i in 0 until changelogArray.length()) {
                    changelogList.add(changelogArray.getString(i))
                }
            }

            AppUpdateInfo(
                versionName = obj.optString("versionName", "1.0.0"),
                versionCode = remoteVersionCode,
                releaseDate = obj.optString("releaseDate", ""),
                apkUrl = obj.optString("apkUrl", ""),
                directDownloadUrl = obj.optString("directDownloadUrl", obj.optString("apkUrl", "")),
                fileSizeBytes = obj.optLong("fileSizeBytes", 15_000_000L),
                fileSizeFormatted = obj.optString("fileSizeFormatted", "15 MB"),
                mandatory = obj.optBoolean("mandatory", false),
                title = obj.optString("title", "Nova Atualização Disponível!"),
                changelog = changelogList
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        apkUrl: String,
        onProgress: (UpdateDownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val apkFile = File(updateDir, "widvelo_update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            val url = URL(apkUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 12000
                readTimeout = 25000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "WidVelo-App-Downloader")
            }

            val totalBytes = connection.contentLength.toLong()
            val totalMbFormatted = String.format(Locale.US, "%.1f MB", if (totalBytes > 0) totalBytes / (1024.0 * 1024.0) else 15.0)

            var downloadedBytes = 0L
            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var lastPercent = -1

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val percent = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0
                        if (percent != lastPercent) {
                            lastPercent = percent
                            val currentMb = String.format(Locale.US, "%.1f MB", downloadedBytes / (1024.0 * 1024.0))
                            withContext(Dispatchers.Main) {
                                onProgress(UpdateDownloadState.Downloading(percent, currentMb, totalMbFormatted))
                            }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(UpdateDownloadState.ReadyToInstall)
                launchPackageInstaller(context, apkFile)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onProgress(UpdateDownloadState.Error("Falha no download: ${e.localizedMessage ?: "Erro de rede"}"))
            }
        }
    }

    private fun launchPackageInstaller(context: Context, apkFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
        } catch (_: Exception) {
            // Em caso de falha de permissão de instalação direta, pode abrir navegador
        }
    }
}
