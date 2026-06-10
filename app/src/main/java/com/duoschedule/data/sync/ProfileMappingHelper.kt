package com.duoschedule.data.sync

import com.duoschedule.data.model.PersonType

data class ProfileMapping(
    val myProfileId: String,
    val partnerProfileId: String?
) {
    fun profileIdFor(personType: PersonType): String? {
        return if (personType == PersonType.PERSON_A) myProfileId else partnerProfileId
    }

    fun personTypeFor(profileId: String): PersonType? {
        return when (profileId) {
            myProfileId -> PersonType.PERSON_A
            partnerProfileId -> PersonType.PERSON_B
            else -> null
        }
    }
}
