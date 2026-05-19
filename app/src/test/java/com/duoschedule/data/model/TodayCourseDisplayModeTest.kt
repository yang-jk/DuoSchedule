package com.duoschedule.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TodayCourseDisplayModeTest {

    @Test
    fun displayMode_entries_hasThree() {
        assertEquals(3, TodayCourseDisplayMode.entries.size)
    }

    @Suppress("WHEN_WITHOUT_ELSE")
    private fun mapToPersonType(mode: TodayCourseDisplayMode): PersonType = when (mode) {
        TodayCourseDisplayMode.SELF_ONLY -> PersonType.PERSON_A
        TodayCourseDisplayMode.TA_ONLY -> PersonType.PERSON_B
        TodayCourseDisplayMode.BOTH -> PersonType.PERSON_A
    }

    @Test
    fun selfOnly_mapsToPersonA() {
        assertEquals(PersonType.PERSON_A, mapToPersonType(TodayCourseDisplayMode.SELF_ONLY))
    }

    @Test
    fun taOnly_mapsToPersonB() {
        assertEquals(PersonType.PERSON_B, mapToPersonType(TodayCourseDisplayMode.TA_ONLY))
    }

    @Test
    fun both_mapsToPersonA() {
        assertEquals(PersonType.PERSON_A, mapToPersonType(TodayCourseDisplayMode.BOTH))
    }

    @Test
    fun displayMode_labels() {
        val labels = mapOf(
            TodayCourseDisplayMode.SELF_ONLY to "仅我的",
            TodayCourseDisplayMode.TA_ONLY to "仅Ta的",
            TodayCourseDisplayMode.BOTH to "全部"
        )
        assertEquals("仅我的", labels[TodayCourseDisplayMode.SELF_ONLY])
        assertEquals("仅Ta的", labels[TodayCourseDisplayMode.TA_ONLY])
        assertEquals("全部", labels[TodayCourseDisplayMode.BOTH])
    }
}
