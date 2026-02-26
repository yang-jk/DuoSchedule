package com.duoschedule.data.local

import androidx.room.*
import com.duoschedule.data.model.Course
import com.duoschedule.data.model.PersonType
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE personType = :personType ORDER BY startHour, startMinute")
    fun getCoursesByPerson(personType: PersonType): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE personType = :personType ORDER BY startHour, startMinute")
    suspend fun getCoursesByPersonSync(personType: PersonType): List<Course>

    @Query("SELECT * FROM courses ORDER BY personType, dayOfWeek, startHour, startMinute")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE dayOfWeek = :dayOfWeek AND personType = :personType ORDER BY startHour, startMinute")
    fun getCoursesByDayAndPerson(dayOfWeek: Int, personType: PersonType): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE dayOfWeek = :dayOfWeek ORDER BY personType, startHour, startMinute")
    fun getCoursesByDay(dayOfWeek: Int): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE dayOfWeek = :dayOfWeek AND personType = :personType ORDER BY startHour, startMinute")
    suspend fun getCoursesForDaySync(dayOfWeek: Int, personType: PersonType): List<Course>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): Course?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Long)

    @Query("DELETE FROM courses WHERE personType = :personType")
    suspend fun deleteCoursesByPerson(personType: PersonType)

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()

    @Query("SELECT COUNT(*) FROM courses WHERE personType = :personType")
    fun getCourseCountByPerson(personType: PersonType): Flow<Int>
}
