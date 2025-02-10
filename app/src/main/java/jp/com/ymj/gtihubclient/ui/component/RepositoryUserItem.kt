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
import jp.com.ymj.gtihubclient.graphql.fragment.RepositoryOwnerItem

@Composable
fun RepositoryOwner(
    item: RepositoryOwnerItem,
) {
    Column(
        modifier = Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RepositoryOwner",
            fontSize = 24.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "id:" + if (item.onUser != null) item.onUser.id else item.onOrganization?.id,
        )
        Text(
            text = "name:" + if (item.onUser != null) item.onUser.name else item.onOrganization?.name,
        )
        Text(
            text = "email:" + if (item.onUser != null) item.onUser.userEmail else item.onOrganization?.organizationEmail
        )
    }
}


@Preview(showBackground = true)
@Composable
fun RepositoryOwnerPreview() {
    RepositoryOwner(
        item = RepositoryOwnerItem(
            onUser = RepositoryOwnerItem.OnUser(
                id = "id",
                name = "Your Name",
                userEmail = "email",
                bio = "bio"
            ),
            __typename = "User",
            onOrganization = RepositoryOwnerItem.OnOrganization(
                id = "id",
                name = "name",
                organizationEmail = "email"
            )
        )
    )
}
