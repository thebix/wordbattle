package wordbattle.di


import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Singleton

@Module(
    includes = [
        OkHttpClientModule::class
    ]
)
class NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofitBuilder(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
        provideRxJava2CallAdapterFactory: RxJava2CallAdapterFactory
    ): Retrofit.Builder =
        Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(provideRxJava2CallAdapterFactory)
            .addConverterFactory(gsonConverterFactory)

    @Provides
    @Singleton
    fun provideGsonConverterFactory(): GsonConverterFactory =
        GsonConverterFactory.create()

    @Provides
    @Singleton
    fun provideRxJava2CallAdapterFactory(): RxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

}

@Module
class OkHttpClientModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addNetworkInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor {
            Timber.d(it)
        }
            .apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

}
