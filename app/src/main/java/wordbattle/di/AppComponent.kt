package wordbattle.di

import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import wordbattle.WordBattleApp
import javax.inject.Singleton

@Singleton
@Component
    (
    modules = [
        AndroidSupportInjectionModule::class
    ]
)
interface AppComponent {

    fun inject(app: WordBattleApp)
}
