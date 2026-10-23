package com.creativeam.widvelo.update

data class AppUpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val releaseDate: String,
    val apkUrl: String,
    val directDownloadUrl: String,
    val fileSizeBytes: Long,
    val fileSizeFormatted: String,
    val mandatory: Boolean,
    val title: String,
    val changelog: List<String>
)

sealed class UpdateDownloadState {
    object Idle : UpdateDownloadState()
    data class Downloading(
        val progressPercent: Int,
        val downloadedMb: String,
        val totalMb: String
    ) : UpdateDownloadState()
    object ReadyToInstall : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}
