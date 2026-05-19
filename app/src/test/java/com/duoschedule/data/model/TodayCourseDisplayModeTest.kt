package com.duoschedule.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TodayCourseDisplayModeTest {

    @Test
    fun displayMode_values_hasThree() {
        assertEquals(3, TodayCourseDisplayMode.values().size)
    }

    @Test
    fun selfOnly_mapsToPersonA() {
        val mode = TodayCourseDisplayMode.SELF_ONLY
        val personType = when (mode) {
            TodayCourseDisplayMode.SELF_ONLY -> PersonType.PERSON_A
            TodayCourseDisplayMode.TA_ONLY -> PersonType.PERSON_B
            TodayCourseDisplayMode.BOTH -> PersonType.PERSON_A
        }
        assertEquals("SELF_ONLY should map to PERSON_A (我)", PersonType.PERSON_A, personType)
    }

    @Test
    fun taOnly_mapsToPersonB() {
        val mode = TodayCourseDisplayMode.TA_ONLY
        val personType = when (mode) {
            TodayCourseDisplayMode.SELF_ONLY -> PersonType.PERSON_A
            TodayCourseDisplayMode.TA_ONLY -> PersonType.PERSON_B
            TodayCourseDisplayMode.BOTH -> PersonType.PERSON_A
        }
        assertEquals("TA_ONLY should map to PERSON_B (Ta)", PersonType.PERSON_B, personType)
    }

    @Test
    fun selfOnly_labelIsSelf() {
        val label = when (TodayCourseDisplayMode.SELF_ONLY) {
            TodayCourseDisplayMode.SELF_ONLY -> "仅我的"
            TodayCourseDisplayMode.TA_ONLY -> "仅Ta的"
            TodayCourseDisplayMode.BOTH -> "全部"
        }
        assertEquals("仅我的", label)
    }

    @Test
    fun taOnly_labelIsOther() {
        val label = when (TodayCourseDisplayMode.TA_ONLY) {
            TodayCourseDisplayMode.SELF_ONLY -> "仅我的"
            TodayCourseDisplayMode.TA_ONLY -> "仅Ta的"
            TodayCourseDisplayMode.BOTH -> "全部"
        }
        assertEquals("仅Ta的", label)
    }
}
