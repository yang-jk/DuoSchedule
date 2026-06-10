package com.duoschedule.data.sync

/**
 * 纯逻辑辅助类，从 SyncManager 中提取 Todo 智能合并的核心匹配逻辑，
 * 不依赖 Android 框架或数据库操作，便于单元测试。
 */
object SmartMergeTodoHelper {

    /**
     * Todo 内容匹配键，用于首次同步智能合并
     */
    fun todoContentMatchKey(todo: CloudTodo): String {
        return "${todo.title}|${todo.date}|${todo.startHour}|${todo.startMinute}|${todo.endHour}|${todo.endMinute}"
    }

    /**
     * 纯逻辑版本的 smartMergeTodosForFirstSync。
     *
     * @param localTodos 本地 Todo 列表（已转为 CloudTodo，带 ownerProfileId）
     * @param cloudTodos 云端 Todo 列表
     * @param myProfileId 我的 profileId
     * @param partnerProfileId 伙伴的 profileId，可为 null
     * @return 合并后的 Todo 列表（包含匹配结果信息）
     */
    fun smartMergeTodosLogic(
        localTodos: List<CloudTodo>,
        cloudTodos: List<CloudTodo>,
        myProfileId: String,
        partnerProfileId: String?
    ): SmartMergeTodoResult {
        val cloudByProfile = cloudTodos.groupBy { it.ownerProfileId }
        val merged = mutableListOf<CloudTodo>()
        val matchedCloudSyncIds = mutableSetOf<String>()

        // 遍历 myProfileId 和 partnerProfileId 对应的 profile
        val profileIds = listOfNotNull(myProfileId, partnerProfileId)
        for (profileId in profileIds) {
            val localForProfile = localTodos.filter { it.ownerProfileId == profileId }
            val cloudForProfile = cloudByProfile[profileId].orEmpty()

            // 使用 contentMatchKey 匹配
            val cloudKeyMap = mutableMapOf<String, CloudTodo>()
            for (cloudTodo in cloudForProfile) {
                cloudKeyMap[todoContentMatchKey(cloudTodo)] = cloudTodo
            }

            for (localTodo in localForProfile) {
                val key = todoContentMatchKey(localTodo)
                val matchedCloud = cloudKeyMap.remove(key)
                if (matchedCloud != null) {
                    matchedCloudSyncIds.add(matchedCloud.syncId)
                    // 复用云端 syncId，保留本地 Todo 内容
                    merged.add(localTodo.copy(syncId = matchedCloud.syncId))
                } else {
                    merged.add(localTodo)
                }
            }

            for (remainingCloud in cloudKeyMap.values) {
                merged.add(remainingCloud)
            }
        }

        // 保留无法映射 profile 的云端 Todo
        for ((profileId, todos) in cloudByProfile) {
            if (profileId != myProfileId && profileId != partnerProfileId) {
                for (todo in todos) {
                    if (!matchedCloudSyncIds.contains(todo.syncId)) {
                        merged.add(todo)
                    }
                }
            }
        }

        return SmartMergeTodoResult(mergedTodos = merged, matchedCloudSyncIds = matchedCloudSyncIds)
    }
}

/**
 * smartMergeTodosLogic 的返回结果
 */
data class SmartMergeTodoResult(
    val mergedTodos: List<CloudTodo>,
    val matchedCloudSyncIds: Set<String>
)
