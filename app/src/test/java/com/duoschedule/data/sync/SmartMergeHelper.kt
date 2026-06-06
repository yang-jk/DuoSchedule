package com.duoschedule.data.sync

/**
 * 纯逻辑辅助类，从 SyncManager 中提取 contentMatchKey 和 smartMergeForFirstSync 的核心匹配逻辑，
 * 不依赖 Android 框架或数据库操作，便于单元测试。
 */
object SmartMergeHelper {

    fun contentMatchKey(course: CloudCourse): String {
        return "${course.name}|${course.dayOfWeek}|${course.startHour}|${course.startMinute}|" +
            "${course.endHour}|${course.endMinute}|${course.location}|${course.teacher}|" +
            "${course.weekType}|${course.startWeek}|${course.endWeek}|${course.customWeeks}|" +
            "${course.startPeriod}|${course.endPeriod}|${course.isCustomTime}"
    }

    /**
     * 纯逻辑版本的 smartMergeForFirstSync。
     *
     * @param localCourses 本地课程列表（已转为 CloudCourse，带 ownerProfileId）
     * @param cloudCourses 云端课程列表
     * @param myProfileId 我的 profileId
     * @param partnerProfileId 伙伴的 profileId，可为 null
     * @return 合并后的课程列表
     */
    fun smartMergeLogic(
        localCourses: List<CloudCourse>,
        cloudCourses: List<CloudCourse>,
        myProfileId: String,
        partnerProfileId: String?
    ): List<CloudCourse> {
        val cloudByProfile = cloudCourses.groupBy { it.ownerProfileId }
        val merged = mutableListOf<CloudCourse>()
        val matchedCloudSyncIds = mutableSetOf<String>()

        // 遍历 myProfileId 和 partnerProfileId 对应的 profile
        val profileIds = listOfNotNull(myProfileId, partnerProfileId)
        for (profileId in profileIds) {
            val localForProfile = localCourses.filter { it.ownerProfileId == profileId }
            val cloudForProfile = cloudByProfile[profileId].orEmpty()

            val cloudKeyMap = mutableMapOf<String, CloudCourse>()
            for (cloudCourse in cloudForProfile) {
                cloudKeyMap[contentMatchKey(cloudCourse)] = cloudCourse
            }

            for (localCourse in localForProfile) {
                val key = contentMatchKey(localCourse)
                val matchedCloud = cloudKeyMap.remove(key)
                if (matchedCloud != null) {
                    matchedCloudSyncIds.add(matchedCloud.syncId)
                    // 复用云端 syncId，保留本地课程内容
                    merged.add(localCourse.copy(syncId = matchedCloud.syncId))
                } else {
                    merged.add(localCourse)
                }
            }

            for (remainingCloud in cloudKeyMap.values) {
                merged.add(remainingCloud)
            }
        }

        // 添加不属于 myProfileId 和 partnerProfileId 的云端课程
        for ((profileId, courses) in cloudByProfile) {
            if (profileId != myProfileId && profileId != partnerProfileId) {
                for (course in courses) {
                    if (!matchedCloudSyncIds.contains(course.syncId)) {
                        merged.add(course)
                    }
                }
            }
        }

        return merged
    }
}
