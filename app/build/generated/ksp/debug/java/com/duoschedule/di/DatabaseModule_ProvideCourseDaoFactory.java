package com.duoschedule.di;

import com.duoschedule.data.local.AppDatabase;
import com.duoschedule.data.local.CourseDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DatabaseModule_ProvideCourseDaoFactory implements Factory<CourseDao> {
  private final Provider<AppDatabase> databaseProvider;

  private DatabaseModule_ProvideCourseDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CourseDao get() {
    return provideCourseDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideCourseDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideCourseDaoFactory(databaseProvider);
  }

  public static CourseDao provideCourseDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCourseDao(database));
  }
}
