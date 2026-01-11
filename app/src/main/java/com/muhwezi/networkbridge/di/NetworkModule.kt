package com.muhwezi.networkbridge.di

import com.muhwezi.networkbridge.data.remote.AccountingService
import com.muhwezi.networkbridge.data.remote.AuthInterceptor
import com.muhwezi.networkbridge.data.remote.AuthService
import com.muhwezi.networkbridge.data.remote.MikrotikService
import com.muhwezi.networkbridge.data.remote.RouterService
import com.muhwezi.networkbridge.data.remote.SubscriptionService
import com.muhwezi.networkbridge.data.remote.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // Only log in debug builds to prevent security issues
            level = if (com.muhwezi.networkbridge.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Add auth token to requests
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.muhwezi.networkbridge.BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson {
        return com.google.gson.Gson()
    }

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideSubscriptionService(retrofit: Retrofit): SubscriptionService {
        return retrofit.create(SubscriptionService::class.java)
    }

    @Provides
    @Singleton
    fun provideRouterService(retrofit: Retrofit): RouterService {
        return retrofit.create(RouterService::class.java)
    }

    @Provides
    @Singleton
    fun provideMikrotikService(retrofit: Retrofit): MikrotikService {
        return retrofit.create(MikrotikService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @Provides
    @Singleton
    fun provideAccountingService(retrofit: Retrofit): AccountingService {
        return retrofit.create(AccountingService::class.java)
    }
}
