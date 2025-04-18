package io.agora.flat.ui.activity.cloud.list

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import io.agora.vuihoc.agora_native.R
import io.agora.flat.common.Navigator
import io.agora.flat.data.model.*
import io.agora.flat.ui.compose.EmptyView
import io.agora.flat.ui.compose.*
import io.agora.flat.ui.theme.FlatColorWhite
import io.agora.flat.ui.theme.FlatTheme
import io.agora.flat.ui.theme.Shapes
import io.agora.flat.ui.theme.isDarkTheme
import io.agora.flat.ui.theme.isTabletMode
import io.agora.flat.util.*

@Composable
fun CloudScreen(
    onOpenUploading: () -> Unit,
    viewModel: CloudStorageViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val viewState by viewModel.state.collectAsState()

    CloudScreen(
        viewState = viewState,
        onOpenUploading = {
            viewModel.clearBadgeFlag()
            onOpenUploading()
        },
        onDeleteClick = viewModel::deleteChecked,
        onReload = viewModel::reloadFileList,
        onLoadMore = viewModel::loadMoreFileList,
        onUploadFile = viewModel::uploadFile,
        onItemChecked = viewModel::checkItem,
        onItemClick = { file ->
            if (file.resourceType == ResourceType.Directory) {
                viewModel.enterFolder(file.fileName)
            } else {
                Navigator.launchPreviewActivity(context, file)
            }
        },
        onItemDelete = { viewModel.delete(it.fileUUID) },
        onItemPreview = { Navigator.launchPreviewActivity(context, it) },
        onItemRename = viewModel::rename,
        onPreviewRestrict = { context.showToast(R.string.cloud_preview_transcoding_hint) },
        onNewFolder = viewModel::createFolder,
        onFolderBack = viewModel::backFolder
    )
}

@Composable
internal fun CloudScreen(
    viewState: CloudStorageUiState,
    onOpenUploading: () -> Unit,
    onDeleteClick: () -> Unit,
    onReload: () -> Unit,
    onLoadMore: () -> Unit,
    onUploadFile: (uri: Uri, info: ContentInfo) -> Unit,
    onItemChecked: (index: Int, checked: Boolean) -> Unit,
    onItemClick: (file: CloudFile) -> Unit,
    onItemPreview: (file: CloudFile) -> Unit,
    onItemRename: (fileUuid: String, fileName: String) -> Unit,
    onItemDelete: (file: CloudFile) -> Unit,
    onPreviewRestrict: () -> Unit,
    onNewFolder: (String) -> Unit,
    onFolderBack: () -> Unit
) {
    var editMode by rememberSaveable { mutableStateOf(false) }
    var showNewFolder by rememberSaveable { mutableStateOf(false) }
    val refreshing = viewState.loadUiState.refresh == LoadState.Loading

    Box {
        Column {
            CloudTopAppBar(
                viewState,
                editMode = editMode,
                onOpenUploading = onOpenUploading,
                onEditClick = { editMode = true },
                onNewFolder = { showNewFolder = true },
                onDeleteClick = onDeleteClick,
                onDoneClick = { editMode = false },
                onFolderBack = onFolderBack,
            )
            FlatSwipeRefresh(refreshing, onRefresh = onReload) {
                CloudFileList(
                    modifier = Modifier.fillMaxSize(),
                    loadState = viewState.loadUiState.append,
                    files = viewState.files,
                    editMode = editMode,
                    onItemChecked = onItemChecked,
                    onItemClick = onItemClick,
                    onItemPreview = onItemPreview,
                    onItemRename = onItemRename,
                    onItemDelete = onItemDelete,
                    onPreviewRestrict = onPreviewRestrict,
                    onLoadMore = onLoadMore,
                )
            }
        }
        if (isTabletMode()) {
            FileAddLayoutPad(onUploadFile)
        } else {
            FileAddLayout(onUploadFile)
        }
    }
    if (showNewFolder) {
        NewFolderDialog(value = "", onCancel = { showNewFolder = false }, onConfirm = {
            showNewFolder = false
            onNewFolder(it)
        })
    }
}

@Composable
private fun CloudTopAppBar(
    viewState: CloudStorageUiState,
    editMode: Boolean,
    onOpenUploading: () -> Unit,
    onEditClick: () -> Unit,
    onNewFolder: () -> Unit,
    onDeleteClick: () -> Unit,
    onDoneClick: () -> Unit,
    onFolderBack: () -> Unit,
) {
    if (viewState.dirPath == CLOUD_ROOT_DIR) {
        FlatMainTopAppBar(
            title = stringResource(R.string.title_cloud_storage),
            actions = cloudAppBarAction(
                viewState,
                editMode,
                onDeleteClick,
                onDoneClick,
                onOpenUploading,
                onEditClick,
                onNewFolder,
            ),
        )
    } else {
        val title = viewState.dirPath.split('/').findLast { it != "" } ?: ""
        BackTopAppBar(
            title = title,
            onBackPressed = onFolderBack,
            actions = cloudAppBarAction(
                viewState,
                editMode,
                onDeleteClick,
                onDoneClick,
                onOpenUploading,
                onEditClick,
                onNewFolder,
            ),
        )
    }
    BackHandler(viewState.dirPath != CLOUD_ROOT_DIR, onBack = onFolderBack)
}

private fun cloudAppBarAction(
    viewState: CloudStorageUiState,
    editMode: Boolean,
    onDeleteClick: () -> Unit,
    onDoneClick: () -> Unit,
    onOpenUploading: () -> Unit,
    onEditClick: () -> Unit,
    onNewFolder: () -> Unit
): @Composable (RowScope.() -> Unit) = {
    val deletable = viewState.deletable
    val uploadingSize = viewState.uploadFiles.size
    val showBadge = viewState.showBadge

    val deleteColor = if (deletable) {
        MaterialTheme.colors.error
    } else {
        MaterialTheme.colors.error.copy(ContentAlpha.disabled)
    }

    if (editMode) {
        TextButton(onClick = onDeleteClick, enabled = deletable) {
            FlatTextBodyOne(stringResource(R.string.delete), color = deleteColor)
        }
        TextButton(onClick = onDoneClick) {
            FlatTextBodyOne(stringResource(R.string.done))
        }
    } else {
        UploadingIcon(showBadge, uploadingSize, onClick = onOpenUploading)
        IconButton(onClick = onEditClick) {
            FlatIcon(id = R.drawable.ic_cloud_list_edit)
        }
        IconButton(onClick = onNewFolder) {
            FlatIcon(id = R.drawable.ic_cloud_list_new_folder)
        }
    }
}

@Composable
private fun UploadingIcon(showBadge: Boolean, size: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (showBadge) Badge { Text("$size") }
            },
        ) {
            FlatIcon(id = R.drawable.ic_cloud_list_unloading)
        }
    }
}

@Composable
private fun BoxScope.FileAddLayout(onUploadFile: (uri: Uri, info: ContentInfo) -> Unit) {
    var showPick by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { showPick = true }, modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    ) {
        Icon(painterResource(R.drawable.ic_cloud_list_add), null, tint = FlatColorWhite)
    }

    val aniValue: Float by animateFloatAsState(if (showPick) 1f else 0f)
    if (aniValue > 0) {
        UploadPickLayout(
            aniValue,
            onUploadFile = { uri, info ->
                onUploadFile(uri, info)
                showPick = false
            },
        ) {
            showPick = false
        }
    }
}

@Composable
private fun BoxScope.FileAddLayoutPad(onUploadFile: (uri: Uri, info: ContentInfo) -> Unit) {
    var showPick by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { showPick = true }, modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    ) {
        Icon(painterResource(R.drawable.ic_cloud_list_add), null, tint = FlatColorWhite)
    }

    if (showPick) {
        UploadPickDialog(
            onUploadFile = { uri, info ->
                showPick = false
                onUploadFile(uri, info)
            },
            onCancel = { showPick = false },
        )
    }
}

@Composable
private fun UploadPickLayout(
    aniValue: Float,
    onUploadFile: (uri: Uri, info: ContentInfo) -> Unit,
    onCoverClick: () -> Unit,
) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer(alpha = aniValue)
                .background(Color(0x52000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) { onCoverClick() }) {}
        Box(
            Modifier
                .fillMaxWidth()
                .height(PickFileItemHeight * 2 * aniValue)
                .background(MaterialTheme.colors.surface)
        ) {
            CloudPickFileGrid(onUploadFile)
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .clickable { onCoverClick() }) {
                Image(
                    painterResource(R.drawable.ic_record_arrow_down), "", Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun UploadPickDialog(onUploadFile: (uri: Uri, info: ContentInfo) -> Unit, onCancel: () -> Unit) {
    Dialog(onCancel) {
        Surface(shape = Shapes.large) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                FlatTextTitle(
                    stringResource(R.string.title_cloud_pick), modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(24.dp))
                Box {
                    CloudPickFileGrid(onUploadFile)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
internal fun CloudFileList(
    modifier: Modifier,
    loadState: LoadState,
    files: List<CloudUiFile>,
    editMode: Boolean,
    onItemChecked: (index: Int, checked: Boolean) -> Unit,
    onItemClick: (CloudFile) -> Unit,
    onItemPreview: (CloudFile) -> Unit,
    onItemRename: (fileUuid: String, fileName: String) -> Unit,
    onItemDelete: (CloudFile) -> Unit,
    onPreviewRestrict: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val scrollState = rememberLazyListState()
    var renaming by remember { mutableStateOf<CloudFile?>(null) }

    val imgRes = if (isDarkTheme()) R.drawable.img_cloud_list_empty_dark else R.drawable.img_cloud_list_empty_light

    LaunchedEffect(files.firstOrNull()) {
        scrollState.animateScrollToItem(0)
    }

    if (files.isEmpty()) {
        EmptyView(
            modifier = modifier.verticalScroll(rememberScrollState()),
            imgRes = imgRes,
            message = R.string.cloud_storage_no_files
        )
    } else {
        LazyColumn(modifier, state = scrollState) {
            items(
                count = files.size,
                key = { index: Int -> files[index].file.fileUUID },
            ) { index ->
                val item = files[index]
                CloudFileItem(
                    item = item,
                    editMode = editMode,
                    onCheckedChange = { checked -> onItemChecked(index, checked) },
                    onClick = {
                        when (item.file.convertStep) {
                            FileConvertStep.Done, FileConvertStep.None -> {
                                onItemClick(item.file)
                            }

                            else -> onPreviewRestrict()
                        }
                    },
                    onPreview = { onItemPreview(item.file) },
                    onRename = { renaming = item.file },
                    onDelete = { onItemDelete(item.file) },
                )
            }

            item {
                CloudListFooter(loadState, onLoadMore = onLoadMore)
            }
        }
    }
    if (renaming != null) {
        FileRenameDialog(value = renaming!!.fileName.nameWithoutExtension(),
            onCancel = { renaming = null },
            onConfirm = {
                val fileName = when (renaming!!.resourceType) {
                    ResourceType.Directory -> it
                    else -> "$it.${renaming!!.fileName.fileExtension()}"
                }
                onItemRename(renaming!!.fileUUID, fileName)
                renaming = null
            })
    }
}

@Composable
private fun CloudListFooter(loadState: LoadState, onLoadMore: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable {
                if (loadState is LoadState.Error) onLoadMore()
            }
            .padding(top = 12.dp, bottom = 20.dp), Alignment.TopCenter) {
        when (loadState) {
            LoadState.Loading -> {
                FlatTextCaption(stringResource(R.string.loaded_loading))
            }

            is LoadState.NotLoading -> {
                if (loadState.end) {
                    FlatTextCaption(stringResource(R.string.loaded_all))
                } else {
                    FlatTextCaption(stringResource(R.string.loaded_loading))
                }
            }

            is LoadState.Error -> {
                FlatTextCaption(stringResource(R.string.loaded_retry))
            }
        }
    }
    if (loadState is LoadState.NotLoading && !loadState.end) {
        LaunchedEffect(loadState) { onLoadMore() }
    }
}

@Composable
private fun FileRenameDialog(value: String, onCancel: () -> Unit, onConfirm: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(value) }

    Dialog(onCancel) {
        Surface(shape = Shapes.large) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                FlatTextTitle(stringResource(R.string.rename), modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(24.dp))
                CloudDialogTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(64.dp),
                    placeholderValue = stringResource(R.string.cloud_rename_hint)
                )
                Spacer(Modifier.height(24.dp))
                Row {
                    FlatSecondaryTextButton(
                        stringResource(R.string.cancel),
                        Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) { onCancel() }
                    Spacer(Modifier.width(12.dp))
                    FlatPrimaryTextButton(
                        stringResource(R.string.confirm),
                        Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) { onConfirm(name) }
                }
            }
        }
    }
}

@Composable
private fun NewFolderDialog(value: String, onCancel: () -> Unit, onConfirm: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(value) }

    Dialog(onCancel) {
        Surface(shape = Shapes.large) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                FlatTextTitle(
                    stringResource(R.string.cloud_new_folder_title),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(24.dp))
                CloudDialogTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(64.dp),
                    placeholderValue = stringResource(R.string.cloud_new_folder_hint)
                )
                Spacer(Modifier.height(24.dp))
                Row {
                    FlatSecondaryTextButton(
                        text = stringResource(R.string.cancel),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                    ) { onCancel() }
                    Spacer(Modifier.width(12.dp))
                    FlatPrimaryTextButton(
                        text = stringResource(R.string.confirm), modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) { onConfirm(name) }
                }
            }
        }
    }
}

@Composable
private fun CloudFileItem(
    item: CloudUiFile,
    editMode: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit),
    onClick: () -> Unit,
    onPreview: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val file = item.file
    val imageId = file.fileIcon()
    val canPreview = file.resourceType != ResourceType.Directory

    Box(Modifier.clickable(onClick = onClick)) {
        Row(Modifier.height(70.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(16.dp))
            Box {
                Image(painterResource(imageId), contentDescription = "", modifier = Modifier.size(24.dp))
                when (item.file.convertStep) {
                    FileConvertStep.Converting -> ConvertingImage(Modifier.align(Alignment.BottomEnd))
                    FileConvertStep.Failed -> Icon(
                        painterResource(R.drawable.ic_cloud_storage_convert_failure),
                        "",
                        Modifier.align(Alignment.BottomEnd),
                        Color.Unspecified,
                    )

                    else -> {; }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                FlatTextBodyOne(
                    file.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis, color = FlatTheme.colors.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    FlatTextCaption(FlatFormatter.longDate(file.createAt))
                    Spacer(Modifier.width(16.dp))
                    FlatTextCaption(FlatFormatter.size(file.fileSize))
                }
            }
            if (editMode) {
                Checkbox(checked = item.checked, onCheckedChange = onCheckedChange, Modifier.padding(3.dp))
                Spacer(Modifier.width(12.dp))
            } else {
                ItemMoreButton(canPreview, onPreview, onRename, onDelete)
                Spacer(Modifier.width(12.dp))
            }
        }
        FlatDivider(startIndent = 52.dp, endIndent = 12.dp, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun ItemMoreButton(canPreview: Boolean, onPreview: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit) {
    Box {
        var expanded by remember { mutableStateOf(false) }

        IconButton({ expanded = true }) {
            FlatIcon(R.drawable.ic_cloud_list_option)
        }
        DropdownMenu(
            modifier = Modifier.wrapContentSize(),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (canPreview) {
                DropdownMenuItem({
                    expanded = false
                    onPreview()
                }) {
                    FlatTextBodyOne(stringResource(R.string.preview))
                }
            }
            DropdownMenuItem({
                expanded = false
                onRename()
            }) {
                FlatTextBodyOne(stringResource(R.string.rename))
            }
            DropdownMenuItem({
                expanded = false
                onDelete()
            }) {
                FlatTextBodyOne(stringResource(R.string.delete))
            }
        }
    }
}

@Composable
fun ConvertingImage(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()

    val angle: Float by infiniteTransition.animateFloat(
        initialValue = 0F, targetValue = 360F, animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
        )
    )

    Icon(
        painter = painterResource(R.drawable.ic_cloud_storage_converting),
        contentDescription = "",
        modifier.rotate(angle),
        tint = Color.Unspecified
    )
}

@Composable
@Preview(widthDp = 400, uiMode = 0x10, locale = "zh")
@Preview(widthDp = 400, uiMode = 0x20)
private fun EditNameDialogPreview() {
    FlatPage {
        NewFolderDialog("HHHH", {}, {})
    }
}

@Composable
@Preview(widthDp = 400, uiMode = 0x10, locale = "zh")
@Preview(widthDp = 400, uiMode = 0x20)
private fun CloudFileItemPreview() {
    FlatPage {
        Column {
            CloudFileItem(ComposePreviewData.CloudListFiles[0], false, {}, {}, {}, {}, {})
            CloudFileItem(ComposePreviewData.CloudListFiles[1], true, {}, {}, {}, {}, {})
            CloudFileItem(ComposePreviewData.CloudListFiles[2], true, {}, {}, {}, {}, {})
        }
    }
}

@Composable
@Preview
private fun CloudStoragePreview() {
    val files = ComposePreviewData.CloudListFiles
    val viewState = CloudStorageUiState(files = files)
    FlatPage {
        CloudScreen(
            viewState = viewState,
            onOpenUploading = { },
            onDeleteClick = { },
            onReload = { },
            onLoadMore = { },
            onUploadFile = { _, _ -> },
            onItemChecked = { _, _ -> },
            onItemClick = {},
            onItemDelete = {},
            onItemPreview = {},
            onItemRename = { _, _ -> },
            onPreviewRestrict = {},
            onNewFolder = { },
            onFolderBack = {},
        )
    }
}

@Composable
@Preview
private fun UploadPickLayoutPreview() {
    UploadPickLayout(1f, { _, _ -> }) {}
}

@Composable
@Preview(device = Devices.PIXEL_C)
private fun UpdatePickDialogPreview() {
    FlatTheme {
        UploadPickDialog({ _, _ -> }) {}
    }
}