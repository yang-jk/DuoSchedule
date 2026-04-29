package com.duoschedule.data.repository;

import com.duoschedule.data.local.CourseDao;
import com.duoschedule.data.local.SettingsDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CourseRepository_Factory implements Factory<CourseRepository> {
  private final Provider<CourseDao> courseDaoProvider;

  private final Provider<SettingsDataStore> settingsDataStoreProvider;

  private CourseRepository_Factory(Provider<CourseDao> courseDaoProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider) {
    this.courseDaoProvider = courseDaoProvider;
    this.settingsDataStoreProvider = settingsDataStoreProvider;
  }

  @Override
  public CourseRepository get() {
    return newInstance(courseDaoProvider.get(), settingsDataStoreProvider.get());
  }

  public static CourseRepository_Factory create(Provider<CourseDao> courseDaoProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider) {
    return new CourseRepository_Factory(courseDaoProvider, settingsDataStoreProvider);
  }

  public static CourseRepository newInstance(CourseDao courseDao,
      SettingsDataStore settingsDataStore) {
    return new CourseRepository(courseDao, settingsDataStore);
  }
}
