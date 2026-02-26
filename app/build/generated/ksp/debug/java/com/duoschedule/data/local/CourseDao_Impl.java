package com.duoschedule.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.duoschedule.data.model.Course;
import com.duoschedule.data.model.PersonType;
import com.duoschedule.data.model.WeekType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CourseDao_Impl implements CourseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Course> __insertionAdapterOfCourse;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Course> __deletionAdapterOfCourse;

  private final EntityDeletionOrUpdateAdapter<Course> __updateAdapterOfCourse;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCourseById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCoursesByPerson;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllCourses;

  public CourseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCourse = new EntityInsertionAdapter<Course>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `courses` (`id`,`name`,`location`,`teacher`,`dayOfWeek`,`startHour`,`startMinute`,`endHour`,`endMinute`,`weekType`,`startWeek`,`endWeek`,`customWeeks`,`personType`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Course entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getLocation());
        statement.bindString(4, entity.getTeacher());
        statement.bindLong(5, entity.getDayOfWeek());
        statement.bindLong(6, entity.getStartHour());
        statement.bindLong(7, entity.getStartMinute());
        statement.bindLong(8, entity.getEndHour());
        statement.bindLong(9, entity.getEndMinute());
        final String _tmp = __converters.fromWeekType(entity.getWeekType());
        statement.bindString(10, _tmp);
        statement.bindLong(11, entity.getStartWeek());
        statement.bindLong(12, entity.getEndWeek());
        statement.bindString(13, entity.getCustomWeeks());
        final String _tmp_1 = __converters.fromPersonType(entity.getPersonType());
        statement.bindString(14, _tmp_1);
      }
    };
    this.__deletionAdapterOfCourse = new EntityDeletionOrUpdateAdapter<Course>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `courses` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Course entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfCourse = new EntityDeletionOrUpdateAdapter<Course>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `courses` SET `id` = ?,`name` = ?,`location` = ?,`teacher` = ?,`dayOfWeek` = ?,`startHour` = ?,`startMinute` = ?,`endHour` = ?,`endMinute` = ?,`weekType` = ?,`startWeek` = ?,`endWeek` = ?,`customWeeks` = ?,`personType` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Course entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getLocation());
        statement.bindString(4, entity.getTeacher());
        statement.bindLong(5, entity.getDayOfWeek());
        statement.bindLong(6, entity.getStartHour());
        statement.bindLong(7, entity.getStartMinute());
        statement.bindLong(8, entity.getEndHour());
        statement.bindLong(9, entity.getEndMinute());
        final String _tmp = __converters.fromWeekType(entity.getWeekType());
        statement.bindString(10, _tmp);
        statement.bindLong(11, entity.getStartWeek());
        statement.bindLong(12, entity.getEndWeek());
        statement.bindString(13, entity.getCustomWeeks());
        final String _tmp_1 = __converters.fromPersonType(entity.getPersonType());
        statement.bindString(14, _tmp_1);
        statement.bindLong(15, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteCourseById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM courses WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteCoursesByPerson = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM courses WHERE personType = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllCourses = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM courses";
        return _query;
      }
    };
  }

  @Override
  public Object insertCourse(final Course course, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCourse.insertAndReturnId(course);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCourse(final Course course, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCourse.handle(course);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCourse(final Course course, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCourse.handle(course);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCourseById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCourseById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCourseById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCoursesByPerson(final PersonType personType,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCoursesByPerson.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromPersonType(personType);
        _stmt.bindString(_argIndex, _tmp);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCoursesByPerson.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllCourses(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllCourses.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllCourses.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Course>> getCoursesByPerson(final PersonType personType) {
    final String _sql = "SELECT * FROM courses WHERE personType = ? ORDER BY startHour, startMinute";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromPersonType(personType);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"courses"}, new Callable<List<Course>>() {
      @Override
      @NonNull
      public List<Course> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfTeacher = CursorUtil.getColumnIndexOrThrow(_cursor, "teacher");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endHour");
          final int _cursorIndexOfEndMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endMinute");
          final int _cursorIndexOfWeekType = CursorUtil.getColumnIndexOrThrow(_cursor, "weekType");
          final int _cursorIndexOfStartWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "startWeek");
          final int _cursorIndexOfEndWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "endWeek");
          final int _cursorIndexOfCustomWeeks = CursorUtil.getColumnIndexOrThrow(_cursor, "customWeeks");
          final int _cursorIndexOfPersonType = CursorUtil.getColumnIndexOrThrow(_cursor, "personType");
          final List<Course> _result = new ArrayList<Course>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Course _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final String _tmpTeacher;
            _tmpTeacher = _cursor.getString(_cursorIndexOfTeacher);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpEndHour;
            _tmpEndHour = _cursor.getInt(_cursorIndexOfEndHour);
            final int _tmpEndMinute;
            _tmpEndMinute = _cursor.getInt(_cursorIndexOfEndMinute);
            final WeekType _tmpWeekType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfWeekType);
            _tmpWeekType = __converters.toWeekType(_tmp_1);
            final int _tmpStartWeek;
            _tmpStartWeek = _cursor.getInt(_cursorIndexOfStartWeek);
            final int _tmpEndWeek;
            _tmpEndWeek = _cursor.getInt(_cursorIndexOfEndWeek);
            final String _tmpCustomWeeks;
            _tmpCustomWeeks = _cursor.getString(_cursorIndexOfCustomWeeks);
            final PersonType _tmpPersonType;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfPersonType);
            _tmpPersonType = __converters.toPersonType(_tmp_2);
            _item = new Course(_tmpId,_tmpName,_tmpLocation,_tmpTeacher,_tmpDayOfWeek,_tmpStartHour,_tmpStartMinute,_tmpEndHour,_tmpEndMinute,_tmpWeekType,_tmpStartWeek,_tmpEndWeek,_tmpCustomWeeks,_tmpPersonType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getCoursesByPersonSync(final PersonType personType,
      final Continuation<? super List<Course>> $completion) {
    final String _sql = "SELECT * FROM courses WHERE personType = ? ORDER BY startHour, startMinute";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromPersonType(personType);
    _statement.bindString(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Course>>() {
      @Override
      @NonNull
      public List<Course> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfTeacher = CursorUtil.getColumnIndexOrThrow(_cursor, "teacher");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endHour");
          final int _cursorIndexOfEndMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endMinute");
          final int _cursorIndexOfWeekType = CursorUtil.getColumnIndexOrThrow(_cursor, "weekType");
          final int _cursorIndexOfStartWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "startWeek");
          final int _cursorIndexOfEndWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "endWeek");
          final int _cursorIndexOfCustomWeeks = CursorUtil.getColumnIndexOrThrow(_cursor, "customWeeks");
          final int _cursorIndexOfPersonType = CursorUtil.getColumnIndexOrThrow(_cursor, "personType");
          final List<Course> _result = new ArrayList<Course>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Course _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final String _tmpTeacher;
            _tmpTeacher = _cursor.getString(_cursorIndexOfTeacher);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpEndHour;
            _tmpEndHour = _cursor.getInt(_cursorIndexOfEndHour);
            final int _tmpEndMinute;
            _tmpEndMinute = _cursor.getInt(_cursorIndexOfEndMinute);
            final WeekType _tmpWeekType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfWeekType);
            _tmpWeekType = __converters.toWeekType(_tmp_1);
            final int _tmpStartWeek;
            _tmpStartWeek = _cursor.getInt(_cursorIndexOfStartWeek);
            final int _tmpEndWeek;
            _tmpEndWeek = _cursor.getInt(_cursorIndexOfEndWeek);
            final String _tmpCustomWeeks;
            _tmpCustomWeeks = _cursor.getString(_cursorIndexOfCustomWeeks);
            final PersonType _tmpPersonType;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfPersonType);
            _tmpPersonType = __converters.toPersonType(_tmp_2);
            _item = new Course(_tmpId,_tmpName,_tmpLocation,_tmpTeacher,_tmpDayOfWeek,_tmpStartHour,_tmpStartMinute,_tmpEndHour,_tmpEndMinute,_tmpWeekType,_tmpStartWeek,_tmpEndWeek,_tmpCustomWeeks,_tmpPersonType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Course>> getAllCourses() {
    final String _sql = "SELECT * FROM courses ORDER BY personType, dayOfWeek, startHour, startMinute";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"courses"}, new Callable<List<Course>>() {
      @Override
      @NonNull
      public List<Course> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfTeacher = CursorUtil.getColumnIndexOrThrow(_cursor, "teacher");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endHour");
          final int _cursorIndexOfEndMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endMinute");
          final int _cursorIndexOfWeekType = CursorUtil.getColumnIndexOrThrow(_cursor, "weekType");
          final int _cursorIndexOfStartWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "startWeek");
          final int _cursorIndexOfEndWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "endWeek");
          final int _cursorIndexOfCustomWeeks = CursorUtil.getColumnIndexOrThrow(_cursor, "customWeeks");
          final int _cursorIndexOfPersonType = CursorUtil.getColumnIndexOrThrow(_cursor, "personType");
          final List<Course> _result = new ArrayList<Course>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Course _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final String _tmpTeacher;
            _tmpTeacher = _cursor.getString(_cursorIndexOfTeacher);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpEndHour;
            _tmpEndHour = _cursor.getInt(_cursorIndexOfEndHour);
            final int _tmpEndMinute;
            _tmpEndMinute = _cursor.getInt(_cursorIndexOfEndMinute);
            final WeekType _tmpWeekType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfWeekType);
            _tmpWeekType = __converters.toWeekType(_tmp);
            final int _tmpStartWeek;
            _tmpStartWeek = _cursor.getInt(_cursorIndexOfStartWeek);
            final int _tmpEndWeek;
            _tmpEndWeek = _cursor.getInt(_cursorIndexOfEndWeek);
            final String _tmpCustomWeeks;
            _tmpCustomWeeks = _cursor.getString(_cursorIndexOfCustomWeeks);
            final PersonType _tmpPersonType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfPersonType);
            _tmpPersonType = __converters.toPersonType(_tmp_1);
            _item = new Course(_tmpId,_tmpName,_tmpLocation,_tmpTeacher,_tmpDayOfWeek,_tmpStartHour,_tmpStartMinute,_tmpEndHour,_tmpEndMinute,_tmpWeekType,_tmpStartWeek,_tmpEndWeek,_tmpCustomWeeks,_tmpPersonType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Course>> getCoursesByDayAndPerson(final int dayOfWeek,
      final PersonType personType) {
    final String _sql = "SELECT * FROM courses WHERE dayOfWeek = ? AND personType = ? ORDER BY startHour, startMinute";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dayOfWeek);
    _argIndex = 2;
    final String _tmp = __converters.fromPersonType(personType);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"courses"}, new Callable<List<Course>>() {
      @Override
      @NonNull
      public List<Course> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfTeacher = CursorUtil.getColumnIndexOrThrow(_cursor, "teacher");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endHour");
          final int _cursorIndexOfEndMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endMinute");
          final int _cursorIndexOfWeekType = CursorUtil.getColumnIndexOrThrow(_cursor, "weekType");
          final int _cursorIndexOfStartWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "startWeek");
          final int _cursorIndexOfEndWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "endWeek");
          final int _cursorIndexOfCustomWeeks = CursorUtil.getColumnIndexOrThrow(_cursor, "customWeeks");
          final int _cursorIndexOfPersonType = CursorUtil.getColumnIndexOrThrow(_cursor, "personType");
          final List<Course> _result = new ArrayList<Course>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Course _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final String _tmpTeacher;
            _tmpTeacher = _cursor.getString(_cursorIndexOfTeacher);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpEndHour;
            _tmpEndHour = _cursor.getInt(_cursorIndexOfEndHour);
            final int _tmpEndMinute;
            _tmpEndMinute = _cursor.getInt(_cursorIndexOfEndMinute);
            final WeekType _tmpWeekType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfWeekType);
            _tmpWeekType = __converters.toWeekType(_tmp_1);
            final int _tmpStartWeek;
            _tmpStartWeek = _cursor.getInt(_cursorIndexOfStartWeek);
            final int _tmpEndWeek;
            _tmpEndWeek = _cursor.getInt(_cursorIndexOfEndWeek);
            final String _tmpCustomWeeks;
            _tmpCustomWeeks = _cursor.getString(_cursorIndexOfCustomWeeks);
            final PersonType _tmpPersonType;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfPersonType);
            _tmpPersonType = __converters.toPersonType(_tmp_2);
            _item = new Course(_tmpId,_tmpName,_tmpLocation,_tmpTeacher,_tmpDayOfWeek,_tmpStartHour,_tmpStartMinute,_tmpEndHour,_tmpEndMinute,_tmpWeekType,_tmpStartWeek,_tmpEndWeek,_tmpCustomWeeks,_tmpPersonType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Course>> getCoursesByDay(final int dayOfWeek) {
    final String _sql = "SELECT * FROM courses WHERE dayOfWeek = ? ORDER BY personType, startHour, startMinute";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dayOfWeek);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"courses"}, new Callable<List<Course>>() {
      @Override
      @NonNull
      public List<Course> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfTeacher = CursorUtil.getColumnIndexOrThrow(_cursor, "teacher");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endHour");
          final int _cursorIndexOfEndMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endMinute");
          final int _cursorIndexOfWeekType = CursorUtil.getColumnIndexOrThrow(_cursor, "weekType");
          final int _cursorIndexOfStartWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "startWeek");
          final int _cursorIndexOfEndWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "endWeek");
          final int _cursorIndexOfCustomWeeks = CursorUtil.getColumnIndexOrThrow(_cursor, "customWeeks");
          final int _cursorIndexOfPersonType = CursorUtil.getColumnIndexOrThrow(_cursor, "personType");
          final List<Course> _result = new ArrayList<Course>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Course _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final String _tmpTeacher;
            _tmpTeacher = _cursor.getString(_cursorIndexOfTeacher);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpEndHour;
            _tmpEndHour = _cursor.getInt(_cursorIndexOfEndHour);
            final int _tmpEndMinute;
            _tmpEndMinute = _cursor.getInt(_cursorIndexOfEndMinute);
            final WeekType _tmpWeekType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfWeekType);
            _tmpWeekType = __converters.toWeekType(_tmp);
            final int _tmpStartWeek;
            _tmpStartWeek = _cursor.getInt(_cursorIndexOfStartWeek);
            final int _tmpEndWeek;
            _tmpEndWeek = _cursor.getInt(_cursorIndexOfEndWeek);
            final String _tmpCustomWeeks;
            _tmpCustomWeeks = _cursor.getString(_cursorIndexOfCustomWeeks);
            final PersonType _tmpPersonType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfPersonType);
            _tmpPersonType = __converters.toPersonType(_tmp_1);
            _item = new Course(_tmpId,_tmpName,_tmpLocation,_tmpTeacher,_tmpDayOfWeek,_tmpStartHour,_tmpStartMinute,_tmpEndHour,_tmpEndMinute,_tmpWeekType,_tmpStartWeek,_tmpEndWeek,_tmpCustomWeeks,_tmpPersonType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getCoursesForDaySync(final int dayOfWeek, final PersonType personType,
      final Continuation<? super List<Course>> $completion) {
    final String _sql = "SELECT * FROM courses WHERE dayOfWeek = ? AND personType = ? ORDER BY startHour, startMinute";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dayOfWeek);
    _argIndex = 2;
    final String _tmp = __converters.fromPersonType(personType);
    _statement.bindString(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Course>>() {
      @Override
      @NonNull
      public List<Course> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfTeacher = CursorUtil.getColumnIndexOrThrow(_cursor, "teacher");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endHour");
          final int _cursorIndexOfEndMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endMinute");
          final int _cursorIndexOfWeekType = CursorUtil.getColumnIndexOrThrow(_cursor, "weekType");
          final int _cursorIndexOfStartWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "startWeek");
          final int _cursorIndexOfEndWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "endWeek");
          final int _cursorIndexOfCustomWeeks = CursorUtil.getColumnIndexOrThrow(_cursor, "customWeeks");
          final int _cursorIndexOfPersonType = CursorUtil.getColumnIndexOrThrow(_cursor, "personType");
          final List<Course> _result = new ArrayList<Course>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Course _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final String _tmpTeacher;
            _tmpTeacher = _cursor.getString(_cursorIndexOfTeacher);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpEndHour;
            _tmpEndHour = _cursor.getInt(_cursorIndexOfEndHour);
            final int _tmpEndMinute;
            _tmpEndMinute = _cursor.getInt(_cursorIndexOfEndMinute);
            final WeekType _tmpWeekType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfWeekType);
            _tmpWeekType = __converters.toWeekType(_tmp_1);
            final int _tmpStartWeek;
            _tmpStartWeek = _cursor.getInt(_cursorIndexOfStartWeek);
            final int _tmpEndWeek;
            _tmpEndWeek = _cursor.getInt(_cursorIndexOfEndWeek);
            final String _tmpCustomWeeks;
            _tmpCustomWeeks = _cursor.getString(_cursorIndexOfCustomWeeks);
            final PersonType _tmpPersonType;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfPersonType);
            _tmpPersonType = __converters.toPersonType(_tmp_2);
            _item = new Course(_tmpId,_tmpName,_tmpLocation,_tmpTeacher,_tmpDayOfWeek,_tmpStartHour,_tmpStartMinute,_tmpEndHour,_tmpEndMinute,_tmpWeekType,_tmpStartWeek,_tmpEndWeek,_tmpCustomWeeks,_tmpPersonType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCourseById(final long id, final Continuation<? super Course> $completion) {
    final String _sql = "SELECT * FROM courses WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Course>() {
      @Override
      @Nullable
      public Course call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfTeacher = CursorUtil.getColumnIndexOrThrow(_cursor, "teacher");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endHour");
          final int _cursorIndexOfEndMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endMinute");
          final int _cursorIndexOfWeekType = CursorUtil.getColumnIndexOrThrow(_cursor, "weekType");
          final int _cursorIndexOfStartWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "startWeek");
          final int _cursorIndexOfEndWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "endWeek");
          final int _cursorIndexOfCustomWeeks = CursorUtil.getColumnIndexOrThrow(_cursor, "customWeeks");
          final int _cursorIndexOfPersonType = CursorUtil.getColumnIndexOrThrow(_cursor, "personType");
          final Course _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLocation;
            _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            final String _tmpTeacher;
            _tmpTeacher = _cursor.getString(_cursorIndexOfTeacher);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpEndHour;
            _tmpEndHour = _cursor.getInt(_cursorIndexOfEndHour);
            final int _tmpEndMinute;
            _tmpEndMinute = _cursor.getInt(_cursorIndexOfEndMinute);
            final WeekType _tmpWeekType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfWeekType);
            _tmpWeekType = __converters.toWeekType(_tmp);
            final int _tmpStartWeek;
            _tmpStartWeek = _cursor.getInt(_cursorIndexOfStartWeek);
            final int _tmpEndWeek;
            _tmpEndWeek = _cursor.getInt(_cursorIndexOfEndWeek);
            final String _tmpCustomWeeks;
            _tmpCustomWeeks = _cursor.getString(_cursorIndexOfCustomWeeks);
            final PersonType _tmpPersonType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfPersonType);
            _tmpPersonType = __converters.toPersonType(_tmp_1);
            _result = new Course(_tmpId,_tmpName,_tmpLocation,_tmpTeacher,_tmpDayOfWeek,_tmpStartHour,_tmpStartMinute,_tmpEndHour,_tmpEndMinute,_tmpWeekType,_tmpStartWeek,_tmpEndWeek,_tmpCustomWeeks,_tmpPersonType);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getCourseCountByPerson(final PersonType personType) {
    final String _sql = "SELECT COUNT(*) FROM courses WHERE personType = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromPersonType(personType);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"courses"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(0);
            _result = _tmp_1;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
