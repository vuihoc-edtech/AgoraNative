package io.agora.flat.ui.activity.dev

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
// import dagger.hilt.android.AndroidEntryPoint
import io.agora.flat.Config
import io.agora.vuihoc.agora_native.R
import io.agora.flat.data.AppEnv
import io.agora.flat.data.AppKVCenter
import io.agora.flat.ui.activity.base.BaseComposeActivity
import io.agora.flat.ui.compose.BackTopAppBar
import io.agora.flat.ui.compose.FlatColumnPage
import io.agora.flat.ui.compose.FlatPrimaryTextButton
import io.agora.flat.ui.compose.FlatSecondaryTextButton
import io.agora.flat.ui.compose.FlatTextBodyOne
import io.agora.flat.ui.compose.FlatTextBodyTwo
import io.agora.flat.ui.compose.FlatTextOnButton
import io.agora.flat.ui.compose.FlatTextTitle
import io.agora.flat.ui.theme.Shapes
import io.agora.flat.ui.viewmodel.UserViewModel
import io.agora.flat.util.showDebugToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


class DevToolsActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FlatColumnPage {
                BackTopAppBar(title = "DevTools", onBackPressed = { finish() })

                LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                    item {
                        ResumeCheckVersion()
                        ClearLastCancelUpdate()
                        ProjectorEnableFlag()
                        UserLoginFlag()
                        MockEnableFlag()
                        EnvSwitch()
                        RemoveAllRoom()
                    }
                }
            }
        }
    }
}

@Composable
fun ResumeCheckVersion() {
    var interval by remember { mutableStateOf("${Config.callVersionCheckInterval}") }
    val context = LocalContext.current

    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(16.dp))
        FlatTextBodyTwo(text = "checkVersion")
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = interval, onValueChange = { interval = it }, modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = {
            Config.callVersionCheckInterval = interval.toLong() * 1000
            if (context is Activity) {
                context.finish()
            }
        }) {
            FlatTextOnButton("确定")
        }
    }
}

@Composable
private fun EnvSwitch() {
    val context = LocalContext.current
    val appEnv = AppEnv.getInstance()
    val curEnv = appEnv.getEnv()
    val flatServiceUrl = appEnv.flatServiceUrl
    val userViewModel: UserViewModel = viewModel()

    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            FlatTextBodyOne(text = "current $curEnv $flatServiceUrl")
            Spacer(modifier = Modifier.width(16.dp))
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                appEnv.envMap.forEach { (k, v) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable {
                                expanded = !expanded
                                if (k != curEnv) {
                                    appEnv.setEnv(k)

                                    scope.launch {
                                        (context as DevToolsActivity).showDebugToast("退出应用中...")
                                        userViewModel.logout()
                                        delay(2000)
                                        // exit application
                                        context.finishAffinity()
                                        android.os.Process.killProcess(android.os.Process.myPid())
                                        exitProcess(1)
                                    }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(16.dp))
                        FlatTextBodyOne(text = "$k ${v.serviceUrl}", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoveAllRoom() {
    val devToolViewModel: DevToolViewModel = viewModel()
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { showDialog = !showDialog },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            FlatTextBodyOne(text = "Remove all rooms")
            Spacer(modifier = Modifier.width(16.dp))
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(shape = Shapes.large) {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                    FlatTextTitle(
                        "REMOVE ALL ?",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(24.dp))
                    Row {
                        FlatSecondaryTextButton(
                            text = stringResource(R.string.cancel),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                        ) { showDialog = false }
                        Spacer(Modifier.width(12.dp))
                        FlatPrimaryTextButton(
                            text = stringResource(R.string.confirm), modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            scope.launch {
                                devToolViewModel.removeAllRooms()
                            }
                            showDialog = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserLoginFlag() {
    val userViewModel: UserViewModel = viewModel()
    val loggedInData = userViewModel.loggedInData.observeAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        FlatTextBodyOne(text = "设置User")
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = loggedInData.value ?: false,
            enabled = loggedInData.value ?: true,
            onCheckedChange = { userViewModel.logout() })
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun MockEnableFlag() {
    val userViewModel: UserViewModel = viewModel()
    var mockEnable by remember { mutableStateOf(AppKVCenter.MockData.mockEnable) }

    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        FlatTextBodyTwo(text = "登录Mock")
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = mockEnable, onCheckedChange = {
            mockEnable = it
            AppKVCenter.MockData.mockEnable = it
            userViewModel.logout()
        })
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun ProjectorEnableFlag() {
    val context = LocalContext.current
    val appKVCenter = AppKVCenter.getInstance()
    var useProjector by remember { mutableStateOf(appKVCenter.useProjectorConvertor()) }

    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        FlatTextBodyTwo(text = "使用 Projector")
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = useProjector, onCheckedChange = {
            useProjector = it
            appKVCenter.setUseProjectorConvertor(useProjector)
        })
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun ClearLastCancelUpdate() {
    val context = LocalContext.current
    val appKVCenter = AppKVCenter.getInstance()
    var lastCancelUpdate by remember { mutableStateOf(appKVCenter.getLastCancelUpdate()) }

    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        FlatTextBodyTwo(text = "清除 LastCancelUpdate")
        Spacer(modifier = Modifier.weight(1f))
        Switch(enabled = lastCancelUpdate != 0L, checked = lastCancelUpdate != 0L, onCheckedChange = {
            lastCancelUpdate = 0
            appKVCenter.setLastCancelUpdate(0)
        })
        Spacer(modifier = Modifier.width(16.dp))
    }
}


@Preview
@Composable
fun PreviewDevTools() {
    FlatColumnPage {
        BackTopAppBar(title = "DevTools", onBackPressed = { })
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            item {
                ClearLastCancelUpdate()
                ProjectorEnableFlag()
                UserLoginFlag()
                MockEnableFlag()
                EnvSwitch()
            }
        }
    }
}