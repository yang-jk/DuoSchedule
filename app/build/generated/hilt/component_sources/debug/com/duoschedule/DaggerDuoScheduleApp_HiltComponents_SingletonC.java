package com.duoschedule;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.duoschedule.data.local.AppDatabase;
import com.duoschedule.data.local.CourseDao;
import com.duoschedule.data.local.SettingsDataStore;
import com.duoschedule.data.repository.CourseRepository;
import com.duoschedule.di.DatabaseModule_ProvideAppDatabaseFactory;
import com.duoschedule.di.DatabaseModule_ProvideCourseDaoFactory;
import com.duoschedule.notification.AutoSilentWorker;
import com.duoschedule.notification.AutoSilentWorker_AssistedFactory;
import com.duoschedule.notification.CourseNotificationManager;
import com.duoschedule.notification.LiveUpdateService;
import com.duoschedule.notification.LiveUpdateService_MembersInjector;
import com.duoschedule.notification.ReminderWorker;
import com.duoschedule.notification.ReminderWorker_AssistedFactory;
import com.duoschedule.notification.RescheduleWorker;
import com.duoschedule.notification.RescheduleWorker_AssistedFactory;
import com.duoschedule.notification.RingerModeManager;
import com.duoschedule.notification.SilentModeReceiver;
import com.duoschedule.notification.SilentModeReceiver_MembersInjector;
import com.duoschedule.ui.edit.CourseEditViewModel;
import com.duoschedule.ui.edit.CourseEditViewModel_HiltModules;
import com.duoschedule.ui.edit.CourseEditViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.duoschedule.ui.edit.CourseEditViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.duoschedule.ui.main.MainViewModel;
import com.duoschedule.ui.main.MainViewModel_HiltModules;
import com.duoschedule.ui.main.MainViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.duoschedule.ui.main.MainViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.duoschedule.ui.schedule.ScheduleViewModel;
import com.duoschedule.ui.schedule.ScheduleViewModel_HiltModules;
import com.duoschedule.ui.schedule.ScheduleViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.duoschedule.ui.schedule.ScheduleViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.duoschedule.ui.settings.SettingsViewModel;
import com.duoschedule.ui.settings.SettingsViewModel_HiltModules;
import com.duoschedule.ui.settings.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.duoschedule.ui.settings.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerDuoScheduleApp_HiltComponents_SingletonC {
  private DaggerDuoScheduleApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public DuoScheduleApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements DuoScheduleApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public DuoScheduleApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements DuoScheduleApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public DuoScheduleApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements DuoScheduleApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public DuoScheduleApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements DuoScheduleApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public DuoScheduleApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements DuoScheduleApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public DuoScheduleApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements DuoScheduleApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public DuoScheduleApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements DuoScheduleApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public DuoScheduleApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends DuoScheduleApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends DuoScheduleApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends DuoScheduleApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends DuoScheduleApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    Map keySetMapOfClassOfAndBooleanBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, Boolean>newMapBuilder(4);
      mapBuilder.put(CourseEditViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CourseEditViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(MainViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MainViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ScheduleViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ScheduleViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide());
      return mapBuilder.build();
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(keySetMapOfClassOfAndBooleanBuilder());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends DuoScheduleApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<CourseEditViewModel> courseEditViewModelProvider;

    Provider<MainViewModel> mainViewModelProvider;

    Provider<ScheduleViewModel> scheduleViewModelProvider;

    Provider<SettingsViewModel> settingsViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    Map hiltViewModelMapMapOfClassOfAndProviderOfViewModelBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(4);
      mapBuilder.put(CourseEditViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (courseEditViewModelProvider)));
      mapBuilder.put(MainViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (mainViewModelProvider)));
      mapBuilder.put(ScheduleViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (scheduleViewModelProvider)));
      mapBuilder.put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (settingsViewModelProvider)));
      return mapBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.courseEditViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.mainViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.scheduleViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(hiltViewModelMapMapOfClassOfAndProviderOfViewModelBuilder());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.duoschedule.ui.edit.CourseEditViewModel
          return (T) new CourseEditViewModel(singletonCImpl.courseRepositoryProvider.get(), singletonCImpl.courseNotificationManagerProvider.get());

          case 1: // com.duoschedule.ui.main.MainViewModel
          return (T) new MainViewModel(singletonCImpl.courseRepositoryProvider.get());

          case 2: // com.duoschedule.ui.schedule.ScheduleViewModel
          return (T) new ScheduleViewModel(singletonCImpl.courseRepositoryProvider.get());

          case 3: // com.duoschedule.ui.settings.SettingsViewModel
          return (T) new SettingsViewModel(singletonCImpl.courseRepositoryProvider.get(), singletonCImpl.courseNotificationManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends DuoScheduleApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends DuoScheduleApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectLiveUpdateService(LiveUpdateService liveUpdateService) {
      injectLiveUpdateService2(liveUpdateService);
    }

    private LiveUpdateService injectLiveUpdateService2(LiveUpdateService instance) {
      LiveUpdateService_MembersInjector.injectCourseDao(instance, singletonCImpl.courseDao());
      LiveUpdateService_MembersInjector.injectSettingsDataStore(instance, singletonCImpl.settingsDataStoreProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends DuoScheduleApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<AppDatabase> provideAppDatabaseProvider;

    Provider<SettingsDataStore> settingsDataStoreProvider;

    Provider<CourseRepository> courseRepositoryProvider;

    Provider<RingerModeManager> ringerModeManagerProvider;

    Provider<CourseNotificationManager> courseNotificationManagerProvider;

    Provider<AutoSilentWorker_AssistedFactory> autoSilentWorker_AssistedFactoryProvider;

    Provider<ReminderWorker_AssistedFactory> reminderWorker_AssistedFactoryProvider;

    Provider<RescheduleWorker_AssistedFactory> rescheduleWorker_AssistedFactoryProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    CourseDao courseDao() {
      return DatabaseModule_ProvideCourseDaoFactory.provideCourseDao(provideAppDatabaseProvider.get());
    }

    Map mapOfStringAndProviderOfWorkerAssistedFactoryOfBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>newMapBuilder(3);
      mapBuilder.put("com.duoschedule.notification.AutoSilentWorker", ((Provider) (autoSilentWorker_AssistedFactoryProvider)));
      mapBuilder.put("com.duoschedule.notification.ReminderWorker", ((Provider) (reminderWorker_AssistedFactoryProvider)));
      mapBuilder.put("com.duoschedule.notification.RescheduleWorker", ((Provider) (rescheduleWorker_AssistedFactoryProvider)));
      return mapBuilder.build();
    }

    Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return mapOfStringAndProviderOfWorkerAssistedFactoryOfBuilder();
    }

    HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 0));
      this.settingsDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<SettingsDataStore>(singletonCImpl, 1));
      this.courseRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<CourseRepository>(singletonCImpl, 2));
      this.ringerModeManagerProvider = DoubleCheck.provider(new SwitchingProvider<RingerModeManager>(singletonCImpl, 4));
      this.courseNotificationManagerProvider = DoubleCheck.provider(new SwitchingProvider<CourseNotificationManager>(singletonCImpl, 3));
      this.autoSilentWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<AutoSilentWorker_AssistedFactory>(singletonCImpl, 5));
      this.reminderWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<ReminderWorker_AssistedFactory>(singletonCImpl, 6));
      this.rescheduleWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<RescheduleWorker_AssistedFactory>(singletonCImpl, 7));
    }

    @Override
    public void injectDuoScheduleApp(DuoScheduleApp duoScheduleApp) {
      injectDuoScheduleApp2(duoScheduleApp);
    }

    @Override
    public AppDatabase getDatabase() {
      return provideAppDatabaseProvider.get();
    }

    @Override
    public SettingsDataStore getSettingsDataStore() {
      return settingsDataStoreProvider.get();
    }

    @Override
    public void injectSilentModeReceiver(SilentModeReceiver silentModeReceiver) {
      injectSilentModeReceiver2(silentModeReceiver);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private DuoScheduleApp injectDuoScheduleApp2(DuoScheduleApp instance) {
      DuoScheduleApp_MembersInjector.injectDatabase(instance, provideAppDatabaseProvider.get());
      DuoScheduleApp_MembersInjector.injectSettingsDataStore(instance, settingsDataStoreProvider.get());
      DuoScheduleApp_MembersInjector.injectRepository(instance, courseRepositoryProvider.get());
      DuoScheduleApp_MembersInjector.injectNotificationManager(instance, courseNotificationManagerProvider.get());
      DuoScheduleApp_MembersInjector.injectRingerModeManager(instance, ringerModeManagerProvider.get());
      DuoScheduleApp_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    private SilentModeReceiver injectSilentModeReceiver2(SilentModeReceiver instance2) {
      SilentModeReceiver_MembersInjector.injectSettingsDataStore(instance2, settingsDataStoreProvider.get());
      SilentModeReceiver_MembersInjector.injectCourseDao(instance2, courseDao());
      SilentModeReceiver_MembersInjector.injectRingerModeManager(instance2, ringerModeManagerProvider.get());
      return instance2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.duoschedule.data.local.AppDatabase
          return (T) DatabaseModule_ProvideAppDatabaseFactory.provideAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.duoschedule.data.local.SettingsDataStore
          return (T) new SettingsDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.duoschedule.data.repository.CourseRepository
          return (T) new CourseRepository(singletonCImpl.courseDao(), singletonCImpl.settingsDataStoreProvider.get());

          case 3: // com.duoschedule.notification.CourseNotificationManager
          return (T) new CourseNotificationManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.courseDao(), singletonCImpl.settingsDataStoreProvider.get(), singletonCImpl.ringerModeManagerProvider.get());

          case 4: // com.duoschedule.notification.RingerModeManager
          return (T) new RingerModeManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.duoschedule.notification.AutoSilentWorker_AssistedFactory
          return (T) new AutoSilentWorker_AssistedFactory() {
            @Override
            public AutoSilentWorker create(Context context, WorkerParameters workerParams) {
              return new AutoSilentWorker(context, workerParams, singletonCImpl.ringerModeManagerProvider.get(), singletonCImpl.settingsDataStoreProvider.get(), singletonCImpl.courseDao());
            }
          };

          case 6: // com.duoschedule.notification.ReminderWorker_AssistedFactory
          return (T) new ReminderWorker_AssistedFactory() {
            @Override
            public ReminderWorker create(Context context2, WorkerParameters workerParams2) {
              return new ReminderWorker(context2, workerParams2, singletonCImpl.courseNotificationManagerProvider.get(), singletonCImpl.settingsDataStoreProvider.get());
            }
          };

          case 7: // com.duoschedule.notification.RescheduleWorker_AssistedFactory
          return (T) new RescheduleWorker_AssistedFactory() {
            @Override
            public RescheduleWorker create(Context context3, WorkerParameters workerParams3) {
              return new RescheduleWorker(context3, workerParams3, singletonCImpl.courseNotificationManagerProvider.get(), singletonCImpl.settingsDataStoreProvider.get(), singletonCImpl.courseDao());
            }
          };

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
