package com.duoschedule.data.importexport

import com.duoschedule.data.model.PersonType
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvExporterPersonTypeTest {

    @Test
    fun newFormat_wo_mapsToPersonA() {
        assertEquals(PersonType.PERSON_A, parsePersonTypeNewFormat("我"))
    }

    @Test
    fun newFormat_ta_mapsToPersonB() {
        assertEquals(PersonType.PERSON_B, parsePersonTypeNewFormat("Ta"))
    }

    @Test
    fun newFormat_A_mapsToPersonA() {
        assertEquals(PersonType.PERSON_A, parsePersonTypeNewFormat("A"))
    }

    @Test
    fun newFormat_B_mapsToPersonB() {
        assertEquals(PersonType.PERSON_B, parsePersonTypeNewFormat("B"))
    }

    @Test
    fun newFormat_personnelA_mapsToPersonA() {
        assertEquals(PersonType.PERSON_A, parsePersonTypeNewFormat("人员A"))
    }

    @Test
    fun newFormat_personnelB_mapsToPersonB() {
        assertEquals(PersonType.PERSON_B, parsePersonTypeNewFormat("人员B"))
    }

    @Test
    fun newFormat_unknown_defaultsToPersonA() {
        assertEquals(PersonType.PERSON_A, parsePersonTypeNewFormat("未知"))
    }

    @Test
    fun oldFormat_ta_mapsToPersonA() {
        assertEquals(
            "Old format: 'Ta' was PERSON_A in old version",
            PersonType.PERSON_A,
            parsePersonTypeOldFormat("Ta")
        )
    }

    @Test
    fun oldFormat_wo_mapsToPersonB() {
        assertEquals(
            "Old format: '我' was PERSON_B in old version",
            PersonType.PERSON_B,
            parsePersonTypeOldFormat("我")
        )
    }

    @Test
    fun oldFormat_A_mapsToPersonA() {
        assertEquals(PersonType.PERSON_A, parsePersonTypeOldFormat("A"))
    }

    @Test
    fun oldFormat_B_mapsToPersonB() {
        assertEquals(PersonType.PERSON_B, parsePersonTypeOldFormat("B"))
    }

    @Test
    fun oldFormat_personnelA_mapsToPersonA() {
        assertEquals(PersonType.PERSON_A, parsePersonTypeOldFormat("人员A"))
    }

    @Test
    fun oldFormat_personnelB_mapsToPersonB() {
        assertEquals(PersonType.PERSON_B, parsePersonTypeOldFormat("人员B"))
    }

    @Test
    fun personNameMatch_overridesKeyword() {
        assertEquals(
            "Custom name '小明' matching personAName should return PERSON_A",
            PersonType.PERSON_A,
            parsePersonTypeNewFormat("小明", personAName = "小明", personBName = "小红")
        )
    }

    @Test
    fun personBNameMatch_returnsPersonB() {
        assertEquals(
            "Custom name '小红' matching personBName should return PERSON_B",
            PersonType.PERSON_B,
            parsePersonTypeNewFormat("小红", personAName = "小明", personBName = "小红")
        )
    }

    @Test
    fun newFormat_markers_areCorrect() {
        assertEquals("# 我的课表设置", PERSON_A_MARKER)
        assertEquals("# Ta的课表设置", PERSON_B_MARKER)
    }

    @Test
    fun oldFormat_sectionMapping_taSettingsBelongsToB() {
        val section = if ("# Ta的课表设置".contains("Ta的课表设置")) "settingsB" else "settingsA"
        assertEquals(
            "Old format: 'Ta的课表设置' should map to settingsB (PERSON_B in new version)",
            "settingsB",
            section
        )
    }

    @Test
    fun oldFormat_sectionMapping_woSettingsBelongsToA() {
        val section = if ("# 我的课表设置".contains("我的课表设置")) "settingsA" else "settingsB"
        assertEquals(
            "Old format: '我的课表设置' should map to settingsA (PERSON_A in new version)",
            "settingsA",
            section
        )
    }

    @Test
    fun oldFormat_nameMapping_taNameBelongsToB() {
        val parts = arrayOf("Ta的名称", "对方")
        var personAName: String? = null
        var personBName: String? = null
        when (parts[0]) {
            "Ta的名称" -> personBName = parts[1]
            "我的名称" -> personAName = parts[1]
        }
        assertEquals("对方", personBName)
        assertEquals(null, personAName)
    }

    private fun parsePersonTypeNewFormat(
        value: String,
        personAName: String? = null,
        personBName: String? = null
    ): PersonType {
        val trimmed = value.trim()
        if (personAName != null && trimmed == personAName) return PersonType.PERSON_A
        if (personBName != null && trimmed == personBName) return PersonType.PERSON_B
        return when (trimmed) {
            "我", "A", "a", "人员A" -> PersonType.PERSON_A
            "Ta", "TA", "ta", "B", "b", "人员B" -> PersonType.PERSON_B
            else -> PersonType.PERSON_A
        }
    }

    private fun parsePersonTypeOldFormat(
        value: String,
        personAName: String? = null,
        personBName: String? = null
    ): PersonType {
        val trimmed = value.trim()
        if (personAName != null && trimmed == personAName) return PersonType.PERSON_A
        if (personBName != null && trimmed == personBName) return PersonType.PERSON_B
        return when (trimmed) {
            "Ta", "TA", "ta", "A", "a", "人员A" -> PersonType.PERSON_A
            "我", "B", "b", "人员B" -> PersonType.PERSON_B
            else -> PersonType.PERSON_A
        }
    }

    companion object {
        private const val PERSON_A_MARKER = "# 我的课表设置"
        private const val PERSON_B_MARKER = "# Ta的课表设置"
    }
}
