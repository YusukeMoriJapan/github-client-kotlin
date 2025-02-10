package jp.com.ymj.gtihubclient.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.normalizedCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.com.ymj.gtihubclient.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class Module {
    @Provides
    @Singleton
    fun provideApolloClient(
    ): ApolloClient {
        val cacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)


        return ApolloClient.Builder()
            .serverUrl("https://api.github.com/graphql")
            .httpHeaders(
                listOf(
                    HttpHeader(
                        "Authorization",
                        "Bearer " + BuildConfig.GITHUB_TOKEN
                    )
                )
            )
            .normalizedCache(cacheFactory)
            .fetchPolicy(FetchPolicy.NetworkFirst)
            .build()

    }
}
