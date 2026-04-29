package com.duoschedule.ui.edit;

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
public final class CourseEditViewModel_Factory implements Factory<CourseEditViewModel> {
  private final Provider<CourseRepository> repositoryProvider;

  private final Provider<CourseNotificationManager> notificationManagerProvider;

  private CourseEditViewModel_Factory(Provider<CourseRepository> repositoryProvider,
      Provider<CourseNotificationManager> notificationManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.notificationManagerProvider = notificationManagerProvider;
  }

  @Override
  public CourseEditViewModel get() {
    return newInstance(repositoryProvider.get(), notificationManagerProvider.get());
  }

  public static CourseEditViewModel_Factory create(Provider<CourseRepository> repositoryProvider,
      Provider<CourseNotificationManager> notificationManagerProvider) {
    return new CourseEditViewModel_Factory(repositoryProvider, notificationManagerProvider);
  }

  public static CourseEditViewModel newInstance(CourseRepository repository,
      CourseNotificationManager notificationManager) {
    return new CourseEditViewModel(repository, notificationManager);
  }
}
