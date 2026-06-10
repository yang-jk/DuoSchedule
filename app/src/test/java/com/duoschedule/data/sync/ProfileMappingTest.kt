package com.duoschedule.data.sync

import com.duoschedule.data.model.PersonType
import org.junit.Assert.*
import org.junit.Test

class ProfileMappingTest {

    @Test
    fun profileIdFor_PERSON_A_returnsMyProfileId() {
        val mapping = ProfileMapping(myProfileId = "my-id", partnerProfileId = "partner-id")
        assertEquals("my-id", mapping.profileIdFor(PersonType.PERSON_A))
    }

    @Test
    fun profileIdFor_PERSON_B_returnsPartnerProfileId() {
        val mapping = ProfileMapping(myProfileId = "my-id", partnerProfileId = "partner-id")
        assertEquals("partner-id", mapping.profileIdFor(PersonType.PERSON_B))
    }

    @Test
    fun personTypeFor_myProfileId_returnsPERSON_A() {
        val mapping = ProfileMapping(myProfileId = "my-id", partnerProfileId = "partner-id")
        assertEquals(PersonType.PERSON_A, mapping.personTypeFor("my-id"))
    }

    @Test
    fun personTypeFor_partnerProfileId_returnsPERSON_B() {
        val mapping = ProfileMapping(myProfileId = "my-id", partnerProfileId = "partner-id")
        assertEquals(PersonType.PERSON_B, mapping.personTypeFor("partner-id"))
    }

    @Test
    fun personTypeFor_unknownProfileId_returnsNull() {
        val mapping = ProfileMapping(myProfileId = "my-id", partnerProfileId = "partner-id")
        assertNull(mapping.personTypeFor("unknown"))
    }

    @Test
    fun profileIdFor_PERSON_B_whenPartnerNull_returnsNull() {
        val mapping = ProfileMapping(myProfileId = "my-id", partnerProfileId = null)
        assertNull(mapping.profileIdFor(PersonType.PERSON_B))
    }

    @Test
    fun joinAsPersonA_mappingCorrect() {
        val mapping = ProfileMapping(myProfileId = "my-new-id", partnerProfileId = "creator-id")
        assertEquals(PersonType.PERSON_A, mapping.personTypeFor("my-new-id"))
        assertEquals(PersonType.PERSON_B, mapping.personTypeFor("creator-id"))
    }

    @Test
    fun joinAsPersonB_mappingCorrect() {
        val mapping = ProfileMapping(myProfileId = "creator-id", partnerProfileId = "my-new-id")
        assertEquals(PersonType.PERSON_A, mapping.personTypeFor("creator-id"))
        assertEquals(PersonType.PERSON_B, mapping.personTypeFor("my-new-id"))
    }
}
