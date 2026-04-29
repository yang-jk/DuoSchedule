package com.duoschedule.notification

import org.json.JSONObject

data class PromotedNotificationStyle(
    val collapsedState: CollapsedState,
    val expandedState: ExpandedState
) {
    data class CollapsedState(
        val leftIcon: IconInfo,
        val rightProgress: ProgressTextInfo
    )

    data class ExpandedState(
        val baseInfo: BaseInfo,
        val picInfo: PicInfo
    )

    data class IconInfo(
        val type: Int = 1,
        val pic: String = ""
    )

    data class ProgressTextInfo(
        val textInfo: TextInfo,
        val progressInfo: ProgressInfo,
        val picInfo: IconInfo? = null
    )

    data class TextInfo(
        val frontTitle: String = "",
        val title: String,
        val content: String = "",
        val showHighlightColor: Boolean = false,
        val narrowFont: Boolean = false
    )

    data class ProgressInfo(
        val progress: Int,
        val colorReach: String = "#34C759",
        val colorUnReach: String = "#E5E5EA",
        val isCCW: Boolean = true
    )

    data class BaseInfo(
        val type: Int = 2,
        val title: String,
        val subTitle: String = "",
        val extraTitle: String = "",
        val specialTitle: String = "",
        val content: String,
        val subContent: String = "",
        val picFunction: String = "",
        val colorTitle: String = "#000000",
        val colorTitleDark: String = "#FFFFFF",
        val colorSubTitle: String = "#000000",
        val colorSubTitleDark: String = "#FFFFFF",
        val colorExtraTitle: String = "#000000",
        val colorExtraTitleDark: String = "#FFFFFF",
        val colorSpecialTitle: String = "#000000",
        val colorSpecialTitleDark: String = "#FFFFFF",
        val colorSpecialBg: String = "#FF6666",
        val colorContent: String = "#000000",
        val colorContentDark: String = "#FFFFFF",
        val colorSubContent: String = "#000000",
        val colorSubContentDark: String = "#FFFFFF",
        val showDivider: Boolean = true,
        val showContentDivider: Boolean = true
    )

    data class PicInfo(
        val type: Int = 1,
        val pic: String = "",
        val picDark: String = ""
    )

    fun toJson(): String {
        return JSONObject().apply {
            put("collapsedState", JSONObject().apply {
                put("leftIcon", JSONObject().apply {
                    put("type", collapsedState.leftIcon.type)
                    put("pic", collapsedState.leftIcon.pic)
                })
                put("rightProgress", JSONObject().apply {
                    put("textInfo", JSONObject().apply {
                        put("frontTitle", collapsedState.rightProgress.textInfo.frontTitle)
                        put("title", collapsedState.rightProgress.textInfo.title)
                        put("content", collapsedState.rightProgress.textInfo.content)
                        put("showHighlightColor", collapsedState.rightProgress.textInfo.showHighlightColor)
                        put("narrowFont", collapsedState.rightProgress.textInfo.narrowFont)
                    })
                    put("progressInfo", JSONObject().apply {
                        put("progress", collapsedState.rightProgress.progressInfo.progress)
                        put("colorReach", collapsedState.rightProgress.progressInfo.colorReach)
                        put("colorUnReach", collapsedState.rightProgress.progressInfo.colorUnReach)
                        put("isCCW", collapsedState.rightProgress.progressInfo.isCCW)
                    })
                    collapsedState.rightProgress.picInfo?.let {
                        put("picInfo", JSONObject().apply {
                            put("type", it.type)
                            put("pic", it.pic)
                        })
                    }
                })
            })
            put("expandedState", JSONObject().apply {
                put("baseInfo", JSONObject().apply {
                    put("type", expandedState.baseInfo.type)
                    put("title", expandedState.baseInfo.title)
                    put("subTitle", expandedState.baseInfo.subTitle)
                    put("extraTitle", expandedState.baseInfo.extraTitle)
                    put("specialTitle", expandedState.baseInfo.specialTitle)
                    put("content", expandedState.baseInfo.content)
                    put("subContent", expandedState.baseInfo.subContent)
                    put("picFunction", expandedState.baseInfo.picFunction)
                    put("colorTitle", expandedState.baseInfo.colorTitle)
                    put("colorTitleDark", expandedState.baseInfo.colorTitleDark)
                    put("colorSubTitle", expandedState.baseInfo.colorSubTitle)
                    put("colorSubTitleDark", expandedState.baseInfo.colorSubTitleDark)
                    put("colorExtraTitle", expandedState.baseInfo.colorExtraTitle)
                    put("colorExtraTitleDark", expandedState.baseInfo.colorExtraTitleDark)
                    put("colorSpecialTitle", expandedState.baseInfo.colorSpecialTitle)
                    put("colorSpecialTitleDark", expandedState.baseInfo.colorSpecialTitleDark)
                    put("colorSpecialBg", expandedState.baseInfo.colorSpecialBg)
                    put("colorContent", expandedState.baseInfo.colorContent)
                    put("colorContentDark", expandedState.baseInfo.colorContentDark)
                    put("colorSubContent", expandedState.baseInfo.colorSubContent)
                    put("colorSubContentDark", expandedState.baseInfo.colorSubContentDark)
                    put("showDivider", expandedState.baseInfo.showDivider)
                    put("showContentDivider", expandedState.baseInfo.showContentDivider)
                })
                put("picInfo", JSONObject().apply {
                    put("type", expandedState.picInfo.type)
                    put("pic", expandedState.picInfo.pic)
                    put("picDark", expandedState.picInfo.picDark)
                })
            })
        }.toString()
    }

    companion object {
        fun createOngoingStyle(
            courseName: String,
            location: String,
            remainingMinutes: Int,
            totalMinutes: Int = 45
        ): PromotedNotificationStyle {
            val progress = if (totalMinutes > 0) {
                ((totalMinutes - remainingMinutes) * 100 / totalMinutes).coerceIn(0, 100)
            } else {
                0
            }

            val remainingText = if (remainingMinutes <= 0) {
                "即将结束"
            } else {
                "剩余 ${remainingMinutes}分钟"
            }

            return PromotedNotificationStyle(
                collapsedState = CollapsedState(
                    leftIcon = IconInfo(type = 1, pic = ""),
                    rightProgress = ProgressTextInfo(
                        textInfo = TextInfo(
                            title = remainingText,
                            showHighlightColor = true
                        ),
                        progressInfo = ProgressInfo(
                            progress = progress,
                            colorReach = "#34C759",
                            colorUnReach = "#E5E5EA",
                            isCCW = true
                        )
                    )
                ),
                expandedState = ExpandedState(
                    baseInfo = BaseInfo(
                        type = 2,
                        title = courseName,
                        subTitle = remainingText,
                        content = location,
                        colorTitle = "#000000",
                        colorTitleDark = "#FFFFFF",
                        colorSubTitle = "#34C759",
                        colorSubTitleDark = "#34C759",
                        colorContent = "#8E8E93",
                        colorContentDark = "#8E8E93",
                        showDivider = true,
                        showContentDivider = false
                    ),
                    picInfo = PicInfo(
                        type = 1,
                        pic = "",
                        picDark = ""
                    )
                )
            )
        }

        fun createReminderStyle(
            courseName: String,
            location: String,
            advanceMinutes: Int
        ): PromotedNotificationStyle {
            return PromotedNotificationStyle(
                collapsedState = CollapsedState(
                    leftIcon = IconInfo(type = 1, pic = ""),
                    rightProgress = ProgressTextInfo(
                        textInfo = TextInfo(
                            title = "还有 ${advanceMinutes}分钟",
                            showHighlightColor = true
                        ),
                        progressInfo = ProgressInfo(
                            progress = 0,
                            colorReach = "#FF9500",
                            colorUnReach = "#E5E5EA",
                            isCCW = true
                        )
                    )
                ),
                expandedState = ExpandedState(
                    baseInfo = BaseInfo(
                        type = 2,
                        title = courseName,
                        subTitle = "还有 ${advanceMinutes}分钟",
                        content = location,
                        colorTitle = "#000000",
                        colorTitleDark = "#FFFFFF",
                        colorSubTitle = "#FF9500",
                        colorSubTitleDark = "#FF9500",
                        colorContent = "#8E8E93",
                        colorContentDark = "#8E8E93",
                        showDivider = true,
                        showContentDivider = false
                    ),
                    picInfo = PicInfo(
                        type = 1,
                        pic = "",
                        picDark = ""
                    )
                )
            )
        }
    }
}
