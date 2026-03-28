package com.muhwezi.networkbridge.di

import com.muhwezi.networkbridge.data.remote.AccountingService
import com.muhwezi.networkbridge.data.remote.AnalyticsService
import com.muhwezi.networkbridge.data.remote.AuthInterceptor
import com.muhwezi.networkbridge.data.remote.AuthService
import com.muhwezi.networkbridge.data.remote.BillingService
import com.muhwezi.networkbridge.data.remote.FirewallService
import com.muhwezi.networkbridge.data.remote.MikrotikLocalService
import com.muhwezi.networkbridge.data.remote.MikrotikService
import com.muhwezi.networkbridge.data.remote.RouterService
import com.muhwezi.networkbridge.data.remote.SubscriptionService
import com.muhwezi.networkbridge.data.remote.TemplateService
import com.muhwezi.networkbridge.data.remote.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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

    /**
     * OkHttp client for direct LAN communication with MikroTik routers.
     * - No AuthInterceptor (uses per-request Basic Auth)
     * - Trusts all certificates (MikroTik uses self-signed certs)
     * - Shorter timeouts (LAN is fast)
     */
    @Provides
    @Singleton
    @Named("local")
    fun provideLocalOkHttpClient(): OkHttpClient {
        // Trust all certificates for local MikroTik connections
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val logging = HttpLoggingInterceptor().apply {
            level = if (com.muhwezi.networkbridge.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
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

    @Provides
    @Singleton
    fun provideFirewallService(retrofit: Retrofit): FirewallService {
        return retrofit.create(FirewallService::class.java)
    }

    @Provides
    @Singleton
    fun provideBillingService(retrofit: Retrofit): BillingService {
        return retrofit.create(BillingService::class.java)
    }

    @Provides
    @Singleton
    fun provideTemplateService(retrofit: Retrofit): TemplateService {
        return retrofit.create(TemplateService::class.java)
    }

    @Provides
    @Singleton
    fun provideAnalyticsService(retrofit: Retrofit): AnalyticsService {
        return retrofit.create(AnalyticsService::class.java)
    }
}
