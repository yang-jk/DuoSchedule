package com.duoschedule.ui.settings;

import com.duoschedule.data.repository.CourseRepository;
import com.duoschedule.notification.NotificationTestHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "deprecation"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<CourseRepository> repositoryProvider;

  private final Provider<NotificationTestHelper> testHelperProvider;

  public SettingsViewModel_Factory(Provider<CourseRepository> repositoryProvider,
      Provider<NotificationTestHelper> testHelperProvider) {
    this.repositoryProvider = repositoryProvider;
    this.testHelperProvider = testHelperProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(repositoryProvider.get(), testHelperProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<CourseRepository> repositoryProvider,
      Provider<NotificationTestHelper> testHelperProvider) {
    return new SettingsViewModel_Factory(repositoryProvider, testHelperProvider);
  }

  public static SettingsViewModel newInstance(CourseRepository repository,
      NotificationTestHelper testHelper) {
    return new SettingsViewModel(repository, testHelper);
  }
}
