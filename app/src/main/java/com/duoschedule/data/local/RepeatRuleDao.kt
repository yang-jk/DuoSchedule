package com.duoschedule.data.local

import androidx.room.*
import com.duoschedule.data.model.RepeatRule
import kotlinx.coroutines.flow.Flow

@Dao
interface RepeatRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepeatRule(rule: RepeatRule): Long

    @Update
    suspend fun updateRepeatRule(rule: RepeatRule)

    @Query("DELETE FROM repeat_rules WHERE id = :id")
    suspend fun deleteRepeatRuleById(id: String)

    @Query("SELECT * FROM repeat_rules WHERE id = :id")
    suspend fun getRepeatRuleById(id: String): RepeatRule?

    @Query("SELECT * FROM repeat_rules")
    suspend fun getAllRepeatRulesSync(): List<RepeatRule>

    @Query("SELECT * FROM repeat_rules ORDER BY id")
    fun getAllRepeatRules(): Flow<List<RepeatRule>>

    @Query("DELETE FROM repeat_rules")
    suspend fun deleteAllRepeatRules()
}
