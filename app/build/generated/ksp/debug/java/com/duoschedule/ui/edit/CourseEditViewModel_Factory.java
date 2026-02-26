package com.duoschedule.ui.edit;

import com.duoschedule.data.repository.CourseRepository;
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
public final class CourseEditViewModel_Factory implements Factory<CourseEditViewModel> {
  private final Provider<CourseRepository> repositoryProvider;

  public CourseEditViewModel_Factory(Provider<CourseRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CourseEditViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static CourseEditViewModel_Factory create(Provider<CourseRepository> repositoryProvider) {
    return new CourseEditViewModel_Factory(repositoryProvider);
  }

  public static CourseEditViewModel newInstance(CourseRepository repository) {
    return new CourseEditViewModel(repository);
  }
}
