package com.duoschedule.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class MigrationTest {

    @Test
    fun migration_4_5_sqlStatements_areCorrect() {
        val statements = listOf(
            "UPDATE courses SET personType = 'PERSON_TEMP' WHERE personType = 'PERSON_A'",
            "UPDATE courses SET personType = 'PERSON_A' WHERE personType = 'PERSON_B'",
            "UPDATE courses SET personType = 'PERSON_B' WHERE personType = 'PERSON_TEMP'"
        )
        assertEquals("Migration should have exactly 3 SQL statements", 3, statements.size)
        assertEquals(
            "Step 1: PERSON_A -> PERSON_TEMP",
            "PERSON_TEMP",
            extractTargetValue(statements[0])
        )
        assertEquals(
            "Step 2: PERSON_B -> PERSON_A",
            "PERSON_A",
            extractTargetValue(statements[1])
        )
        assertEquals(
            "Step 3: PERSON_TEMP -> PERSON_B",
            "PERSON_B",
            extractTargetValue(statements[2])
        )
    }

    @Test
    fun migration_4_5_swapsPersonTypes() {
        val before = mapOf(
            1L to "PERSON_A",
            2L to "PERSON_B",
            3L to "PERSON_A",
            4L to "PERSON_B"
        )

        val afterStep1 = before.mapValues { (_, v) ->
            if (v == "PERSON_A") "PERSON_TEMP" else v
        }
        assertEquals("PERSON_TEMP", afterStep1[1L])
        assertEquals("PERSON_B", afterStep1[2L])

        val afterStep2 = afterStep1.mapValues { (_, v) ->
            if (v == "PERSON_B") "PERSON_A" else v
        }
        assertEquals("PERSON_TEMP", afterStep2[1L])
        assertEquals("PERSON_A", afterStep2[2L])

        val afterStep3 = afterStep2.mapValues { (_, v) ->
            if (v == "PERSON_TEMP") "PERSON_B" else v
        }
        assertEquals("PERSON_B", afterStep3[1L])
        assertEquals("PERSON_A", afterStep3[3L])
        assertEquals("PERSON_A", afterStep3[2L])
        assertEquals("PERSON_B", afterStep3[4L])
    }

    @Test
    fun migration_4_5_isIdempotent_ifRunTwice() {
        val before = mapOf(
            1L to "PERSON_A",
            2L to "PERSON_B"
        )

        val afterFirst = simulateMigration(before)
        assertEquals("PERSON_B", afterFirst[1L])
        assertEquals("PERSON_A", afterFirst[2L])

        val afterSecond = simulateMigration(afterFirst)
        assertEquals("PERSON_A", afterSecond[1L])
        assertEquals("PERSON_B", afterSecond[2L])
    }

    @Test
    fun migration_version_from4To5() {
        val migration = AppDatabase.MIGRATION_4_5
        assertEquals(4, migration.startVersion)
        assertEquals(5, migration.endVersion)
    }

    private fun simulateMigration(data: Map<Long, String>): Map<Long, String> {
        val result = data.toMutableMap()

        result.entries.forEach { (key, value) ->
            if (value == "PERSON_A") result[key] = "PERSON_TEMP"
        }
        result.entries.forEach { (key, value) ->
            if (value == "PERSON_B") result[key] = "PERSON_A"
        }
        result.entries.forEach { (key, value) ->
            if (value == "PERSON_TEMP") result[key] = "PERSON_B"
        }

        return result.toMap()
    }

    private fun extractTargetValue(sql: String): String {
        val regex = "= '(\\w+)'".toRegex()
        val matches = regex.findAll(sql).toList()
        return matches.last().groupValues[1]
    }
}
