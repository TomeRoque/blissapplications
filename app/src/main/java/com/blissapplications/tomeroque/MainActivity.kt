package com.blissapplications.tomeroque

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.blissapplications.tomeroque.ui.theme.TomeRoqueTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.blissapplications.tomeroque.objects.Emoji
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.blissapplications.tomeroque.utilities.ViewModel
import kotlinx.coroutines.launch
import coil.compose.rememberImagePainter
import com.blissapplications.tomeroque.objects.Avatar
import com.blissapplications.tomeroque.objects.Repo
import kotlinx.coroutines.CoroutineScope
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TomeRoqueTheme {
                AppNavHost()
            }
        }
    }
}

//API stuff
val moshi = Moshi.Builder().build()
val jsonAdapter: JsonAdapter<Map<String, Any>> = moshi.adapter(
    Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController() // This is the NavController

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController) // Your home screen
        }
        composable("emojiListScreen/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") // Access the category parameter
            ListScreen(name = category!!, navController = navController)
        }
    }
}

@Composable
fun HomeScreen(viewModel: ViewModel = hiltViewModel(), navController: NavController) {

    val yOffsetTitle = with(LocalDensity.current) { (11.52).sp.toPx().toDp() }
    val yOffsetBody = with(LocalDensity.current) { (10.97).sp.toPx().toDp() }

    var userInfo by remember { mutableStateOf<String>("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val emojiList by viewModel.emojis
    var avatarList by remember { mutableStateOf<List<Avatar>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val emojis = viewModel.getAllEmojis()
                if (emojis.isNotEmpty()) viewModel.updateEmojis(emojis) else viewModel.updateEmojis(emptyList())

                val avatars = viewModel.getAllAvatars()
                avatarList = if (avatars.isNotEmpty()) avatars else emptyList()

                Log.e("avatares", viewModel.checkAvatarExists("blissapps").toString())

            } catch (e: Exception) {
                Log.e("EmojiError", "Error fetching emojis", e)
            }
        }
    }

    var imageUrl by remember {
        mutableStateOf(emojiList.randomOrNull()?.url)
    }

    val randomEmojiUrls by remember(emojiList) {
        derivedStateOf {
            List(3) {
                if (emojiList.isNotEmpty()) {
                    emojiList.random().url
                } else {
                    null
                }
            }
        }
    }

    var textState by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(R.drawable.bliss),
                contentDescription = null,
                modifier = Modifier
                    .size(97.dp, 101.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 92.dp)
            )

            Text(
                text = stringResource(R.string.title),
                modifier = Modifier
                    .size(width = 146.dp, height = 47.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 202.dp, y = 40.dp),
                style = TextStyle(
                    fontSize = 16.sp,
                    letterSpacing = 0.01.em,
                    color = Color(0xFF1C1C1C),
                    lineHeight = 20.sp,
                    fontFeatureSettings = "pnum, lnum",
                    textAlign = TextAlign.Start
                )
            )

            Canvas(
                modifier = Modifier
                    .size(width = 335.dp, height = 1.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 91.dp)
            ) {
                drawLine(
                    color = Color.Black,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = if (imageUrl == null) {
                    painterResource(R.drawable.emoji) // Default image
                } else {
                    rememberImagePainter(imageUrl) // Image from URL
                },
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp, 200.dp)
                    .clickable {
                        if (emojiList.isEmpty()) {
                            coroutineScope.launch {
                                if (emojiList.isEmpty()) {
                                    val fetchedEmojis = getEmojis()?.map { (name, url) ->
                                        Emoji(name, url.toString())
                                    } ?: emptyList()

                                    viewModel.saveEmojis(fetchedEmojis)

                                    val randomEmoji = fetchedEmojis.random()
                                    imageUrl = randomEmoji.url
                                }
                            }
                        } else {
                            val randomEmoji = emojiList.random()
                            imageUrl = randomEmoji.url
                        }
                    }
            )

            Spacer(modifier = Modifier.height(16.dp)) // Optional space between elements

            Box(
                modifier = Modifier
                    .size(width = 139.dp, height = 68.dp)
                    .offset(x = 118.dp, y = 45.dp)
            ) {
            // Beak (White)
                Image(
                    painter = painterResource(id = R.drawable.tooltipbox),
                    contentDescription = null, // No content description as it's decorative
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize() // Make Image fill the Box
                )


                Text(
                    text = stringResource(R.string.clickme), // "click me!"
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .width(100.dp)
                        .height(22.dp)
                        .align(Alignment.TopStart) // Align to top center of the box
                        .offset(x = 17.dp, y = yOffsetTitle) // Text position adjustment
                )


                Text(
                    text = if (emojiList.isEmpty()) {
                        stringResource(R.string.clickmeGet)
                    } else {
                        stringResource(R.string.clickmeBody)
                    }, // "for a random emoji"
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Black,
                        lineHeight = 16.sp,
                        letterSpacing = 0.03.sp
                    ),
                    modifier = Modifier
                        .width(158.dp)
                        .height(16.dp)
                        .align(Alignment.CenterStart) // Align to bottom center of the box
                        .offset(x = 17.dp, y = yOffsetBody) // Text position adjustment
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp)
                .clickable {
                    navController.navigate("emojiListScreen/Emoji")
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 59.dp, y = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                for (i in 0..2) {
                    val randomEmojiUrl = randomEmojiUrls[i]

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Image(
                            painter = if (randomEmojiUrl != null) {
                                rememberImagePainter(randomEmojiUrl)
                            } else {
                                painterResource(R.drawable.emoji)
                            },
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .wrapContentSize()
                        .animateContentSize()
                        .clip(RoundedCornerShape(4.dp)) // Clip to outline (can adjust shape here if needed)
                        .background(Color(0xFFF5F5F5)) // Set the background color

                ) {
                    Text(
                        text = "+" + emojiList.size.toString(),
                        style = TextStyle(
                            fontSize = 14.sp, // Equivalent to textSize in styles.xml
                            color = Color(0xFF757575), // Equivalent to textColor in styles.xml
                            lineHeight = 20.sp // Line height equal to the height of the Box (20sp)
                        ),
                        modifier = Modifier
                            .height(20.dp) // Equivalent to the size (width and height) of the TextView
                            .align(Alignment.Center) // Align the text in the center (horizontal and vertical)
                    )
                }

                Text(
                    text = stringResource(id = R.string.more), // "See this and more..."
                    style = TextStyle(
                        fontSize = 14.sp, // Equivalent to android:textSize="14sp"
                        color = Color.Black, // Equivalent to android:textColor="#000000"
                        lineHeight = 20.sp // Equivalent to lineSpacingExtra="3sp" and line height 20sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth() // Equivalent to android:layout_width="135dp"
                        .height(32.dp) // Equivalent to android:layout_height="32dp"
                        .padding(start = 0.dp, top = 0.dp) // Equivalent to layout_marginLeft and layout_marginTop
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .offset(x = 10.dp)
                )
            }

        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            contentAlignment = Alignment.Center

        ){
            Row(
                modifier = Modifier
                    .size(300.dp, 60.dp) // Set width and height
                    .align(Alignment.TopStart) // Align to the top-left of the parent
                    .offset(x = 59.dp, y = 0.dp), // Add margins
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Column(
                    modifier = Modifier
                        .size(239.dp, 70.dp)
                        .background(
                            color = Color(0xFFE6E0E9), // Background color
                            shape = RectangleShape // Ensures rounded corners apply uniformly
                        )
                        .drawBehind {
                            // Draw bottom stroke
                            val strokePath = Path().apply {
                                moveTo(0f, size.height) // Start bottom left
                                lineTo(size.width, size.height) // End bottom right
                            }
                            drawPath(
                                path = strokePath,
                                color = Color(0xFF49454F), // Stroke color
                                style = Stroke(width = 2.dp.toPx()) // Stroke width
                            )
                        }
                        .background(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp) // Rounded corners
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .size(239.dp, 56.dp) // Set width and height of the box
                    ) {
                        Column(
                            modifier = Modifier
                                .size(175.dp, 60.dp) // Set width and height of the box
                                .offset(x = 10.dp, y = 0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.inputLabel), // "Insert a GitHub username"
                                style = TextStyle(
                                    fontSize = 12.sp, // Equivalent to textSize in styles.xml
                                    color = Color(0xFF49454F), // Equivalent to textColor in styles.xml
                                    lineHeight = 16.sp, // Line height
                                    letterSpacing = 0.03.sp, // Letter spacing
                                    fontFamily = FontFamily.Default // Use Roboto or Default for simplicity
                                ),
                                modifier = Modifier
                                    .size(width = 175.dp, height = 16.dp) // Equivalent to layout_width and layout_height
                                    .offset(y = yOffsetTitle)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            BasicTextField(
                                value = textState,
                                onValueChange = { newText -> textState = newText },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    letterSpacing = 0.03.sp,
                                    color = Color(0xFF79747E)
                                ),
                                modifier = Modifier
                                    .width(175.dp)
                                    .height(56.dp)  // Adjust height to avoid cut-off
                            )
                        }
                    }

                }

                Button(
                    onClick = {
                        if (textState.text.isNotEmpty()) {
                            coroutineScope.launch {
                                val userExists = viewModel.checkAvatarExists(textState.text)
                                withContext(Dispatchers.Main) {
                                    if (!userExists) {
                                        isLoading = true
                                        getUserInfo(textState.text, "Avatar",coroutineScope) { result ->
                                            if (result != null) {
                                                viewModel.saveAvatar(result as Avatar)
                                                Toast.makeText(context, "Avatar Added", Toast.LENGTH_SHORT).show()
                                                imageUrl = result.avatar_url
                                            } else {
                                                Toast.makeText(context, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
                                            }
                                            isLoading = false
                                        }
                                        getUserInfo(textState.text, "Repos",coroutineScope) { result ->
                                            if (result != null) {
                                                    viewModel.saveRepos(result as List<Repo>)
                                                Toast.makeText(context, "Avatar Added", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
                                            }
                                            isLoading = false
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "This user has already been loaded",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please enter a username",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search), // Use painter instead
                        contentDescription = "Button Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp) // Adjust icon size
                    )
                }

            }

        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = stringResource(R.string.inputTitle), // "Github Api"
            style = TextStyle(
                fontSize = 12.sp, // Equivalent to textSize
                color = Color(0xFF49454F), // Equivalent to textColor
                letterSpacing = 0.03.sp, // Equivalent to letterSpacing
                lineHeight = 16.sp // Equivalent to lineHeight (box height)
            ),
            modifier = Modifier
                .offset(x = 70.dp), // TranslationY for line spacing compensation
            maxLines = 1,
            overflow = TextOverflow.Ellipsis, // Optional: Handles text overflow gracefully
            textAlign = TextAlign.Start // Gravity = top aligns content at the start
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .offset(x = 65.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(115.dp, 115.dp)
                    .shadow(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        navController.navigate("emojiListScreen/Avatar")
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatarlist),
                    contentDescription = null, // Decorative background
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize() // Ensure it fills the parent
                )
            }

            Spacer(modifier = Modifier.width(30.dp))

            Box(
                modifier = Modifier
                    .size(115.dp, 115.dp)
                    .shadow(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        navController.navigate("emojiListScreen/Repo")
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.googlerepos),
                    contentDescription = null, // Decorative background
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize() // Ensure it fills the parent
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxSize() // Matches alignParentLeft, Right, Top, and Bottom
        ) {
            Image(
                painter = painterResource(id = R.drawable.footer),
                contentDescription = null, // Decorative background
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize() // Ensure it fills the parent
            )
        }


    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TomeRoqueTheme {
        AppNavHost()
    }
}

@Composable
fun ListScreen(name: String, viewModel: ViewModel = hiltViewModel(), navController: NavController){

    var emojiList by remember { mutableStateOf<List<Emoji>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(true) }

    var avatarList by remember { mutableStateOf<List<Avatar>>(emptyList()) }

    val repoList by viewModel.repoList.collectAsState()
    val repoFlow by remember(repoList) {
        mutableStateOf(viewModel.getRepoFlow())
    }
    val repoItems = repoFlow.collectAsLazyPagingItems()

    val coroutineScope = rememberCoroutineScope()

    suspend fun fetchData(name: String) {
        when (name) {
            "Emoji" -> {
                try {
                    val emojis = viewModel.getAllEmojis()
                    emojiList = if (emojis.isNotEmpty()) emojis else emptyList()
                    Log.d("EmojiList", "Emojis fetched: $emojiList")
                } catch (e: Exception) {
                    Log.e("EmojiError", "Error fetching emojis", e)
                }
            }
            "Avatar" -> {
                try {
                    val avatars = viewModel.getAllAvatars()
                    avatarList = if (avatars.isNotEmpty()) avatars else emptyList()
                } catch (e: Exception) {
                    Log.e("AvatarError", "Error fetching avatars", e)
                }
            }
            "Repo" -> {
                try {
                    val repos = viewModel.getAllRepos()
                    val newRepoList = if (repos.isNotEmpty()) repos else emptyList()
                    viewModel.setRepos(newRepoList)
                } catch (e: Exception) {
                    Log.e("ReposError", "Error fetching repos", e)
                }
            }
        }
    }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            coroutineScope.launch {
                fetchData(name)
            }
            refreshTrigger = !refreshTrigger
        }
    }

    LaunchedEffect(repoList) {
        repoItems.refresh()
        snapshotFlow { repoItems.loadState.refresh }
            .collect { loadState ->
                if (loadState is LoadState.NotLoading) {
                    Log.e("repoList", repoItems.itemSnapshotList.items.toString())
                    snapshotFlow { repoItems.itemSnapshotList.items }
                        .collect { Log.e("LoadedItems", "Items: ${it.size}") }
                } else if (loadState is LoadState.Loading) {
                    Log.e("repoList", "Loading...")
                } else if (loadState is LoadState.Error) {
                    Log.e("repoList", "Error loading items")
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(color = Color(0xFFF5F5F5))
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp),
                    clip = false,
                    ambientColor = Color.Gray.copy(alpha = 0.2f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                ),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = null, // Decorative background
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(20.dp, 20.dp)
                    .offset(20.dp, 0.dp)
                    .clickable {
                        navController.popBackStack() // Navigate to the previous page
                    }
            )

            Spacer(modifier = Modifier.width(20.dp))

            Image(
                painter = painterResource(id = R.drawable.bliss),
                contentDescription = null, // Decorative background
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(50.dp,50.dp)
                    .offset(20.dp,0.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = stringResource(R.string.title),
                modifier = Modifier
                    .offset(10.dp,0.dp),
                style = TextStyle(
                    fontSize = 11.sp,
                    letterSpacing = 0.01.em,
                    color = Color(0xFF1C1C1C),
                    lineHeight = 20.sp,
                    fontFeatureSettings = "pnum, lnum",
                    textAlign = TextAlign.Start
                )
            )

            if (name == "Emoji"){
                Spacer(modifier = Modifier.width(40.dp))

                Image(
                    painter = painterResource(id = R.drawable.refresh),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(20.dp, 20.dp)
                        .offset(20.dp, 0.dp)
                        .clickable {
                            refreshTrigger = !refreshTrigger
                        }
                )

            }

        }
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = name + " " + stringResource(R.string.list),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            style = TextStyle(
                fontSize = 20.sp,
                letterSpacing = 0.01.em,
                color = Color(0xFF1C1C1C),
                lineHeight = 20.sp,
                fontFeatureSettings = "pnum, lnum",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .size(width = 335.dp, height = 1.dp)
                .offset(x = 30.dp, y = 0.dp)
        ) {
            drawLine(
                color = Color.Black,
                start = Offset.Zero,
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (name == "Repo") {
            LazyColumn {
                items(repoItems.itemCount) { index ->
                    val repo = repoItems[index]
                    if (repo != null) {
                        Text(
                            text = repo.full_name,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                when (repoItems.loadState.append) {
                    is LoadState.Error -> {
                        item { Text("Error loading more items") }
                    }
                    LoadState.Loading -> {
                        item { CircularProgressIndicator(
                            color = Color.Black, // Customize the color
                            strokeWidth = 4.dp  // Set the thickness of the indicator
                        ) }
                    }
                    else -> {}
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .padding(8.dp)
                    .height(500.dp),
                content = {
                    items(
                        when (name) {
                            "Emoji" -> emojiList
                            "Avatar" -> avatarList
                            else -> emptyList() // Default case if neither "Emoji" nor "Avatar"
                        }
                    ) { item ->

                            GridItem(
                                item,
                                onClick = { clickedItem ->
                                    when (name) {
                                        "Emoji" -> {
                                            emojiList = emojiList.toMutableList().apply {
                                                remove(clickedItem)
                                            }
                                        }
                                        "Avatar" -> {
                                            if (clickedItem is Avatar) {
                                                viewModel.deleteAvatar(clickedItem.login)
                                                avatarList = avatarList.filter { it.login != clickedItem.login }
                                            }
                                        }
                                    }
                                })
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.footer),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )
        }

    }
}

@Composable
fun GridItem(item: Any, onClick: (Any) -> Unit) {
    // Determine whether the item is of type Emoji or Avatar
    val imageUrl = when (item) {
        is Emoji -> item.url // For Emoji, use the 'url' property
        is Avatar -> item.avatar_url // For Avatar, use the 'avatarUrl' property
        else -> "" // Default case if the item is neither Emoji nor Avatar
    }

    // Ensure the imageUrl is valid before trying to load the image
    val painter = rememberImagePainter(imageUrl)

    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick(item) }, // Call the onClick lambda when clicked
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth() // Makes the Box take up the full width of the Card
                .padding(8.dp),  // Optional padding for spacing inside the Card
            contentAlignment = Alignment.Center // Centers the content horizontally and vertically
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(50.dp, 50.dp) // Size of the image
            )
        }
    }
}

suspend fun getEmojis(): Map<String, Any>? {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://api.github.com/emojis")
        .build()

    Log.e("request", request.body.toString())

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                return@withContext jsonAdapter.fromJson(jsonResponse)
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("error", e.toString())
            return@withContext null
        }
    }
}

fun getUserInfo(username: String, type: String, coroutineScope: CoroutineScope, callback: (Any?) -> Unit) {
    val client = OkHttpClient()

    if (type == "Repos"){
        val url = "https://api.github.com/users/$username/repos"

        val request = Request.Builder()
            .url(url)
            .build()

        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute() // Ensure this runs on IO thread
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    // If you expect a JSON array, use JSONArray
                    val jsonArray = JSONArray(responseBody)

                    val repos = mutableListOf<Repo>()
                    for (i in 0 until jsonArray.length()) {
                        val repo = jsonArray.getJSONObject(i)
                        val id = repo.getInt("id")
                        val fullName = repo.getString("full_name")
                        val private = repo.getBoolean("private")
                        // Process the repo data as needed
                        repos.add(Repo(id, fullName, private.toString()))
                    }

                    withContext(Dispatchers.Main) {
                        callback(repos)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("Error", "${response.message}")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("Network Error", "${e.localizedMessage}")
                }
            }
        }
    } else {

        val url = "https://api.github.com/users/$username"

        val request = Request.Builder()
            .url(url)
            .build()

        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute() // Ensure this runs on IO thread
                }

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val userName = jsonResponse.optString("login", "N/A")
                    val userId = jsonResponse.optInt("id", 0)
                    val avatarUrl = jsonResponse.optString("avatar_url", "N/A")

                    val avatar = Avatar(userName, userId, avatarUrl)

                    withContext(Dispatchers.Main) {
                        callback(avatar)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("Error", "${response.message}")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("Network Error", "${e.localizedMessage}")
                }
            }
        }
    }
}