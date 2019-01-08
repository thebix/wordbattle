package wordbattle.di

import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import wordbattle.MainActivity
import wordbattle.WordBattleApp
import javax.inject.Singleton

@Singleton
@Component
    (
    modules = [
        ActivityModule::class,
        AndroidSupportInjectionModule::class,
        AppModule::class,
        NetworkModule::class
    ]
)
interface AppComponent {

    fun inject(app: WordBattleApp)
}

@Module
class AppModule {

    @Provides
    @Singleton
    @IoScheduler
    fun provideIoScheduler(): Scheduler =
        Schedulers.io()

}

@Module
abstract class ActivityModule {

    @MainActivityScope
    @ContributesAndroidInjector(
        modules = [
            ApiModule::class
        ]
    )
    internal abstract fun contributeMainActivity(): MainActivity

}
