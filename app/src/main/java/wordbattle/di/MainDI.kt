package wordbattle.di

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import wordbattle.api.WordApi
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MainActivityScope

@Module
class ApiModule {

    companion object {
        private const val URL_PRODUCTS_API =
            "https://gist.githubusercontent.com/"
    }

    @Provides
    @MainActivityScope
    fun provideStoreApi(retrofitBuilder: Retrofit.Builder): WordApi =
        retrofitBuilder
            .baseUrl(URL_PRODUCTS_API)
            .build()
            .create(WordApi::class.java)
}
