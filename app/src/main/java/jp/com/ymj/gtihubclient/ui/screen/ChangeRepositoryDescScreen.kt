package jp.com.ymj.gtihubclient.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import jp.com.ymj.gtihubclient.ChangeRepositoryDesc
import jp.com.ymj.gtihubclient.graphql.ChangeRepositoryDescScreenQuery
import jp.com.ymj.gtihubclient.graphql.type.buildRepository
import jp.com.ymj.gtihubclient.rememberRetainedCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

@Composable
fun ChangeRepositoryDescScreen(
    modifier: Modifier = Modifier,
    changeRepositoryDesc: ChangeRepositoryDesc,
    query: (ChangeRepositoryDesc) -> Flow<ApolloResponse<ChangeRepositoryDescScreenQuery.Data>>,
    changeDescMutation: suspend (id: String, descInput: String) -> Unit,
    toLastPage: () -> Unit,
) {

    var descInput by rememberSaveable { mutableStateOf("") }
    val scope = rememberRetainedCoroutineScope()

    val currentDescription by rememberRetained {
        query(changeRepositoryDesc).shareIn(scope, SharingStarted.Lazily, replay = 1)
    }
        .map { it.data?.repository?.description }
        .collectAsRetainedState(initial = null)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = CenterHorizontally
    ) {
        Text(
            "ChangeRepositoryDescScreen",
            fontSize = 24.sp
        )
        Spacer(Modifier.height(16.dp))
        TextField(
            value = descInput,
            onValueChange = { descInput = it },
        )
        Button(
            onClick = {
                scope.launch { changeDescMutation(changeRepositoryDesc.repositoryId, descInput) }
            }
        ) {
            Text("Update Description")
        }
        Text("Repository ID: ${changeRepositoryDesc.repositoryId}")
        Text("Current Description: $currentDescription")
        Button(toLastPage) { Text("To LastPage") }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewChangeRepositoryDescScreen() {

    val data = ChangeRepositoryDescScreenQuery.Data {
        repository = buildRepository {
            id = "id"
            description = "This is a GitHub client for Android."
        }
    }

    val response =
        ApolloResponse.Builder(
            ChangeRepositoryDescScreenQuery("", ""),
            com.benasher44.uuid.Uuid.randomUUID()
        ).data(data)
            .build()

    ChangeRepositoryDescScreen(
        changeRepositoryDesc = ChangeRepositoryDesc(
            repositoryId = "id",
            owner = "ymj",
            repositoryName = "GitHubClient"
        ),
        query = { flowOf(response) },
        changeDescMutation = { _, _ -> },
        toLastPage = {}
    )
}
