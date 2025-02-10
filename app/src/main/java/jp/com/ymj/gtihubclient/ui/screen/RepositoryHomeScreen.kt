package jp.com.ymj.gtihubclient.ui.screen

import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apollographql.apollo.api.ApolloResponse
import io.github.takahirom.rin.collectAsRetainedState
import io.github.takahirom.rin.rememberRetained
import jp.com.ymj.gtihubclient.InvokeDebounce
import jp.com.ymj.gtihubclient.graphql.RepositoryHomeScreenQuery
import jp.com.ymj.gtihubclient.graphql.type.buildRepository
import jp.com.ymj.gtihubclient.graphql.type.buildUser
import jp.com.ymj.gtihubclient.rememberRetainedCoroutineScope
import jp.com.ymj.gtihubclient.ui.component.Repository
import jp.com.ymj.gtihubclient.ui.component.RepositoryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.parcelize.Parcelize
import kotlin.time.Duration.Companion.seconds

@Composable
fun RepositoryHomeScreen(
    modifier: Modifier = Modifier,
    query: (RepositoryHomeScreenQueryInput) -> Flow<ApolloResponse<RepositoryHomeScreenQuery.Data>?>,
    initialOwner: String,
    initialRepositoryName: String,
    onNavigateToChangeRepositoryDesc: (id: String, owner: String, name: String) -> Unit,
    onRefresh: (owner: String, repositoryName: String) -> Unit,
) {

    var tempInput by rememberSaveable {
        mutableStateOf(RepositoryHomeScreenQueryInput(initialOwner, initialRepositoryName))
    }

    var confirmedInput by rememberSaveable {
        mutableStateOf(tempInput)
    }

    val scope = rememberRetainedCoroutineScope()

    val response by rememberRetained(confirmedInput.toString()) {
        query(confirmedInput).shareIn(scope, SharingStarted.Lazily, replay = 1)
    }
        .collectAsRetainedState(initial = null)

    /*
    A pattern that does not use rin.
    val response by remember(confirmedInput) {
        query(confirmedInput)
    }
        .collectAsState(initial = null)
    */

    val invokeDebounce = remember {
        InvokeDebounce(1.seconds) {
            confirmedInput = tempInput
        }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = CenterHorizontally,
    ) {
        Text(
            "RepositoryHomeScreen",
            fontSize = 24.sp
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                onRefresh(
                    tempInput.ownerName,
                    tempInput.repositoryName
                )
            }
        ) {
            Text("Refresh")
        }
        Button(
            onClick = {
                onNavigateToChangeRepositoryDesc(
                    response?.data?.repository?.repositoryItem?.id ?: "",
                    tempInput.ownerName,
                    tempInput.repositoryName
                )
            }
        ) {
            Text("Go to Update Description Page")
        }
        Spacer(Modifier.height(16.dp))
        TextField(
            value = tempInput.ownerName,
            onValueChange = {
                tempInput = tempInput.copy(ownerName = it)
                invokeDebounce()
            },
            label = {
                Text("Owner Name")
            }
        )
        TextField(
            value = tempInput.repositoryName,
            onValueChange = {
                tempInput = tempInput.copy(repositoryName = it)
                invokeDebounce()
            },
            label = {
                Text("Repository Name")
            }
        )

        when {
            response == null -> CircularProgressIndicator()
            response?.exception != null -> Text(response?.exception.toString())
            response?.hasErrors() == true -> Text(response?.errors.toString())
            else -> {
                val repositoryDescriptionItem =
                    response?.data?.repository?.repositoryItem

                if (repositoryDescriptionItem != null) {
                    Spacer(Modifier.height(16.dp))
                    Repository(repositoryDescriptionItem)
                }

                val repositoryOwnerItem =
                    response?.data?.repository?.owner?.repositoryOwnerItem
                if (repositoryOwnerItem != null) {
                    Spacer(Modifier.height(16.dp))
                    RepositoryOwner(repositoryOwnerItem)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRepositoryHomeScreen() {

    val data = RepositoryHomeScreenQuery.Data {
        repository = buildRepository {
            id = "id"
            name = "GitHubClient"
            description = "This is a GitHub client for Android."
            owner = buildUser {
                id = "id"
                name = "ymj"
                email = ""
                bio = "This is a bio."
            }
        }
    }

    val response =
        ApolloResponse.Builder(
            RepositoryHomeScreenQuery("", "", true),
            com.benasher44.uuid.Uuid.randomUUID()
        ).data(data)
            .build()

    RepositoryHomeScreen(
        modifier = Modifier,
        initialOwner = "ymj",
        initialRepositoryName = "GitHubClient",
        query = { flow { emit(response) } },
        onNavigateToChangeRepositoryDesc = { _, _, _ -> },
        onRefresh = { _, _ -> }
    )
}

@Parcelize
data class RepositoryHomeScreenQueryInput(
    val ownerName: String,
    val repositoryName: String
) : Parcelable

