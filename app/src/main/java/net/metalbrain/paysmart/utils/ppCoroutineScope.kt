package net.metalbrain.paysmart.utils

// Optional alias to be more readable
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppCoroutineScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppContext

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ActivityContext

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FragmentContext

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ServiceContext

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ViewModelContext
