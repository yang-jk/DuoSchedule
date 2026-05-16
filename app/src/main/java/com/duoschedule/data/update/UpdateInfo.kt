package com.duoschedule.data.update

data class UpdateInfo(
    val latestVersion: String,
    val latestVersionCode: Int,
    val minSupportedVersionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
    val forceUpdate: Boolean
)

enum class UpdateStatus {
    NO_UPDATE,
    OPTIONAL_UPDATE,
    FORCE_UPDATE
}
