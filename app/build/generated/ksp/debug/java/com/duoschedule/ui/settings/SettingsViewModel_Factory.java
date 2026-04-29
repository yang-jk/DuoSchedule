package com.duoschedule.ui.settings;

import com.duoschedule.data.repository.CourseRepository;
import com.duoschedule.notification.CourseNotificationManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<CourseRepository> repositoryProvider;

  private final Provider<CourseNotificationManager> notificationManagerProvider;

  private SettingsViewModel_Factory(Provider<CourseRepository> repositoryProvider,
      Provider<CourseNotificationManager> notificationManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.notificationManagerProvider = notificationManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(repositoryProvider.get(), notificationManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<CourseRepository> repositoryProvider,
      Provider<CourseNotificationManager> notificationManagerProvider) {
    return new SettingsViewModel_Factory(repositoryProvider, notificationManagerProvider);
  }

  public static SettingsViewModel newInstance(CourseRepository repository,
      CourseNotificationManager notificationManager) {
    return new SettingsViewModel(repository, notificationManager);
  }
}
