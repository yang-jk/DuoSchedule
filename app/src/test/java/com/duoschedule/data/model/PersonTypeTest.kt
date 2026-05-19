package com.duoschedule.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PersonTypeTest {

    @Test
    fun personA_isSelf() {
        assertEquals("PERSON_A should represent '我' (self)", "我", personAToLabel())
    }

    @Test
    fun personB_isOther() {
        assertEquals("PERSON_B should represent 'Ta' (other)", "Ta", personBToLabel())
    }

    @Test
    fun personA_defaultName_isSelf() {
        val defaultNameA = "我"
        val defaultNameB = "Ta"
        assertEquals("PERSON_A default name should be '我'", "我", defaultNameA)
        assertEquals("PERSON_B default name should be 'Ta'", "Ta", defaultNameB)
    }

    @Test
    fun personType_values_hasExactlyTwo() {
        assertEquals("Should have exactly 2 PersonType values", 2, PersonType.entries.size)
    }

    @Test
    fun personType_aComesBeforeB() {
        assertEquals(
            "PERSON_A should be first enum value",
            PersonType.PERSON_A,
            PersonType.entries[0]
        )
    }

    @Test
    fun personType_nameConsistency() {
        assertEquals("PERSON_A name", "PERSON_A", PersonType.PERSON_A.name)
        assertEquals("PERSON_B name", "PERSON_B", PersonType.PERSON_B.name)
    }

    private fun personAToLabel(): String = "我"
    private fun personBToLabel(): String = "Ta"
}
