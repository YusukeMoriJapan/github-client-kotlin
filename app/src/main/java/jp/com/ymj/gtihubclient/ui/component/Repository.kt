package jp.com.ymj.gtihubclient.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.com.ymj.gtihubclient.graphql.fragment.RepositoryItem

@Composable
fun Repository(
    item: RepositoryItem
) {
    Column(
        modifier = Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Repository",
            fontSize = 24.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "id:" + item.id,
        )
        Text(
            text = "name:" + item.name,
        )
        Text(
            text = "description:" + item.description,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RepositoryPreview() {
    Repository(
        item = RepositoryItem(
            id = "id",
            name = "Repository Name",
            description = "description",
            __typename = "Repository"
        )
    )
}
