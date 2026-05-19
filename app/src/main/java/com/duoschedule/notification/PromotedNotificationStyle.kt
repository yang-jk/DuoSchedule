package com.duoschedule.notification

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

    companion object {
        fun createOngoingStyle(
            courseName: String,
            location: String,
            remainingMinutes: Int,
            totalMinutes: Int
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
