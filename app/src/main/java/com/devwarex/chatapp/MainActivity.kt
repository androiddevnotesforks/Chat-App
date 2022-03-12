package com.devwarex.chatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.devwarex.chatapp.models.MessageModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = "ad"
        setContent {
            ChatAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainLayout()

                }
            }
        }
    }
}


fun Modifier.firstBaselineToTop(
    firstBaselineToTop: Dp
) = this.then(
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
        val firstBaseline = placeable[FirstBaseline]

        // Height of the composable with padding - first baseline
        val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
        val height = placeable.height + placeableY
        layout(placeable.width, height) {
            placeable.placeRelative(0,placeableY)
        }
    }
)

@Preview
@Composable
fun TextWithNormalPaddingPreview(modifier: Modifier = Modifier) {
    ChatAppTheme {
      MainLayout()
    }
}


@Composable
fun MainLayout(modifier: Modifier = Modifier){
    Log.e("text","inside main")
    val viewModel =  hiltViewModel<MessagesViewModel>()
    Scaffold(
        topBar = {
            TopAppBar(
                title ={ Text(text = "Chat") }
            )
            Log.e("text","inside top bar")
        }
    ) {
        ConstraintLayout{
            val ( list , edit ) = createRefs()
            Log.e("text","inside constraint")
            LazyColumn(modifier = modifier
                .constrainAs(list) {
                    top.linkTo(parent.top)
                    bottom.linkTo(edit.top)
                    height = Dimension.preferredWrapContent

                }
                .fillMaxWidth()){
                Log.e("text","inside lazy")
                items(viewModel.messages){
                    MessageCard(msg = it)
                }
            }
            Row(modifier = modifier
                .constrainAs(edit) {
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom, margin = 4.dp)
                }
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                MessageEditText(modifier.weight(1f), viewModel = viewModel)
               // Spacer(modifier = modifier.width(16.dp))
                FloatingActionButton(
                    onClick = { viewModel.onClick() },
                    modifier = modifier
                        .padding(end = 8.dp)
                        .align(alignment = Alignment.CenterVertically)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_send) , contentDescription = "send", modifier = modifier)
                }
            }

        }

    }
}

@Composable
fun MessageEditText(modifier: Modifier,viewModel: MessagesViewModel){
    val t = viewModel.text.collectAsState()
    TextField(
        value = t.value,
        onValueChange = viewModel::setText ,
        modifier = modifier.padding(end = 8.dp),
        maxLines = 3,
        placeholder = { Text(text = "Message") }

    )
    Log.e("text","inside edit fun")
}
@Composable
fun MessageCard(msg: MessageModel) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = rememberImagePainter(
                data = msg.img
            ),
            contentDescription = "image profile",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                /*.border(1.dp, MaterialTheme.colors.secondary, CircleShape)*/
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = msg.auth,
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(shape = MaterialTheme.shapes.medium, elevation = 2.dp) {
                Text(
                    text = msg.body,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(all = 4.dp)
                )
            }
        }
    }
}
