package com.sharapov.notes.presentation.screens.creation

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.sharapov.notes.domain.entities.ContentItem
import com.sharapov.notes.presentation.ui.theme.CustomIcons
import com.sharapov.notes.presentation.utils.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateNoteViewModel = hiltViewModel(),
    onFinished: () -> Unit
) {
    val state = viewModel.state.collectAsState()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.processCommand(CreateNoteCommand.AddImage(it))
            }
        }
    )

    when (val currentState = state.value) {
        is CreateNoteState.Creation -> {
            Scaffold(
                modifier = modifier,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Create Note",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        navigationIcon = {
                            Icon(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 8.dp)
                                    .clickable {
                                        viewModel.processCommand(CreateNoteCommand.Back)
                                    },
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        actions = {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .clickable {
                                        imagePicker.launch("image/*")
                                    },
                                imageVector = CustomIcons.addPhoto,
                                contentDescription = "Add photo from gallery",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        value = currentState.title,
                        onValueChange = { viewModel.processCommand(CreateNoteCommand.InputTitle(it)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = {
                            Text(
                                text = "Title",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                        }
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = DateFormatter.formatCurrentDate(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Content(
                        modifier = Modifier
                            .weight(1f),
                        content = currentState.content,
                        onDeleteImageClick = {
                            viewModel.processCommand(CreateNoteCommand.DeleteImage(it))
                        },
                        onTextChange = { index, content ->
                            viewModel.processCommand(
                                CreateNoteCommand.InputContent(
                                    index = index,
                                    content = content
                                )
                            )
                        }
                    )
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                        onClick = { viewModel.processCommand(CreateNoteCommand.Save) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        enabled = currentState.isSavable
                    ) {
                        Text(
                            text = "Save note"
                        )
                    }
                }
            }
        }

        CreateNoteState.Finish -> {
            LaunchedEffect(key1 = Unit) {
                onFinished()
            }
        }
    }
}


@Composable
fun Content(
    modifier: Modifier = Modifier,
    content: List<ContentItem>,
    onDeleteImageClick: (Int) -> Unit,
    onTextChange: (Int, String) -> Unit
) {
    LazyColumn(modifier = modifier) {
        content.forEachIndexed { index, contentItem ->
            item(key = index) {
                when (contentItem) {
                    is ContentItem.Image -> {
                        val isAlreadyDisplayed =
                            index > 0 && content[index - 1] is ContentItem.Image
                        content.takeIf { !isAlreadyDisplayed }
                            ?.drop(index)
                            ?.takeWhile { it is ContentItem.Image }
                            ?.map { (it as ContentItem.Image).url }
                            ?.let { urls ->
                                ImageGroup(
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    imageUrls = urls,
                                    onDeleteImageClick = { imageIndex ->
                                        onDeleteImageClick(index + imageIndex)
                                    }
                                )
                            }
                    }

                    is ContentItem.Text -> {
                        TextContent(
                            text = contentItem.content,
                            onTextChange = {
                                onTextChange(index, it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageGroup(
    modifier: Modifier = Modifier,
    imageUrls: List<String>,
    onDeleteImageClick: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        imageUrls.forEachIndexed { index, url ->
            ImageContent(
                modifier = Modifier.weight(1f),
                imageUrl = url,
                onDeleteImageClick = {
                    onDeleteImageClick(index)
                }
            )
        }
    }
}

@Composable
fun ImageContent(
    modifier: Modifier = Modifier,
    imageUrl: String,
    onDeleteImageClick: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            model = imageUrl,
            contentDescription = "Image from gallery.",
            contentScale = ContentScale.FillWidth
        )
        Icon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp)
                .clickable {
                    onDeleteImageClick()
                },
            imageVector = Icons.Default.Close,
            contentDescription = "Remove image",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
private fun TextContent(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit
) {
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        value = text,
        onValueChange = onTextChange,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        placeholder = {
            Text(
                text = "Note something down",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        }
    )
}