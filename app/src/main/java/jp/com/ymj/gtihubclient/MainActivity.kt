package jp.com.ymj.gtihubclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.optimisticUpdates
import com.apollographql.apollo.cache.normalized.watch
import dagger.hilt.android.AndroidEntryPoint
import jp.com.ymj.gtihubclient.graphql.ChangeRepositoryDescScreenQuery
import jp.com.ymj.gtihubclient.graphql.RepositoryHomeScreenQuery
import jp.com.ymj.gtihubclient.graphql.UpdateRepositoryDescMutation
import jp.com.ymj.gtihubclient.graphql.type.Repository
import jp.com.ymj.gtihubclient.ui.screen.ChangeRepositoryDescScreen
import jp.com.ymj.gtihubclient.ui.screen.RepositoryHomeScreen
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class RepositoryHome(
    val owner: String = "YusukeMoriJapan",
    val repositoryName: String = "github-client-kotlin"
)

@Serializable
object Home

@Serializable
data class ChangeRepositoryDesc(
    val repositoryId: String,
    val owner: String,
    val repositoryName: String
)

@Serializable
object LastPage

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var apolloClient: ApolloClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = Home) {

                composable<Home> {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate(RepositoryHome())
                                }
                            ) {
                                Text("Go to RepositoryHome")
                            }
                        }
                    }
                }

                restorableComposable<RepositoryHome> { backStackEntry, isRestored ->
                    val route: RepositoryHome = backStackEntry.toRoute()

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        RepositoryHomeScreen(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxWidth(),
                            initialOwner = route.owner,
                            initialRepositoryName = route.repositoryName,
                            onNavigateToChangeRepositoryDesc = { repositoryId, owner, name ->
                                navController.navigate(
                                    ChangeRepositoryDesc(
                                        repositoryId = repositoryId,
                                        owner = owner,
                                        repositoryName = name
                                    )
                                )
                            },
                            query = { confirmedInput ->
                                apolloClient.query(
                                    RepositoryHomeScreenQuery(
                                        confirmedInput.ownerName,
                                        confirmedInput.repositoryName,
                                        showBio = true
                                    )
                                )
//                                A pattern that does not use rin.
//                                    .fetchPolicy(
//                                        if (isRestored)
//                                            FetchPolicy.CacheFirst
//                                        else
//                                            FetchPolicy.NetworkFirst
//                                    )
                                    .fetchPolicy(
                                        FetchPolicy.NetworkFirst
                                    )
                                    .watch()
                            },
                            onRefresh = { owner, repositoryName ->
                                navController.navigate(
                                    RepositoryHome(
                                        owner = owner,
                                        repositoryName = repositoryName
                                    )
                                ) {
                                    popUpTo<RepositoryHome> {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                }

                composable<ChangeRepositoryDesc> { backStackEntry ->
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        ChangeRepositoryDescScreen(
                            modifier = Modifier.padding(innerPadding),
                            query = { input ->
                                apolloClient.query(
                                    ChangeRepositoryDescScreenQuery(
                                        owner = input.owner,
                                        name = input.repositoryName,
                                    )
                                )
                                    .fetchPolicy(FetchPolicy.CacheFirst)
                                    .watch()
                            },
                            changeDescMutation = { id, descInput ->
                                apolloClient.mutation(
                                    UpdateRepositoryDescMutation(
                                        repositoryId = id,
                                        description = descInput,
                                    )
                                )
                                    .optimisticUpdates(
                                        UpdateRepositoryDescMutation.Data(
                                            updateRepository = UpdateRepositoryDescMutation.UpdateRepository(
                                                repository = UpdateRepositoryDescMutation.Repository(
                                                    id = id,
                                                    description = descInput,
                                                    __typename = Repository.type.name
                                                )
                                            )
                                        )
                                    )
                                    .execute()
                            },
                            changeRepositoryDesc = backStackEntry.toRoute(),
                            toLastPage = {
                                navController.navigate(LastPage)
                            }
                        )
                    }
                }

                composable<LastPage> {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(Modifier.padding(innerPadding)) { Text("LastPage") }
                    }
                }
            }
        }
    }
}

