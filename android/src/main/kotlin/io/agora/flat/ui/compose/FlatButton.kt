package io.agora.flat.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.agora.flat.ui.theme.*

@Composable
fun FlatPrimaryTextButton(
    text: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(40.dp),
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val darkMode = isDarkTheme()
    val colors = ButtonDefaults.textButtonColors(
        backgroundColor = if (enabled) MaterialTheme.colors.primary else if (darkMode) Gray_9 else Gray_2,
        contentColor = if (darkMode) Gray_0 else Gray_0,
        disabledContentColor = if (darkMode) Gray_7 else Gray_5
    )
    TextButton(
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shape = Shapes.small,
        onClick = onClick
    ) {
        FlatTextOnButton(text)
    }
}

@Composable
fun FlatSecondaryTextButton(
    text: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(40.dp),
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val darkMode = isDarkTheme()
    val colors = ButtonDefaults.outlinedButtonColors(
        contentColor = if (darkMode) Gray_3 else Gray_6,
        disabledContentColor = if (darkMode) Gray_7 else Gray_5,
        backgroundColor = MaterialTheme.colors.surface
    )

    OutlinedButton(
        modifier = modifier,
        enabled = enabled,
        shape = Shapes.small,
        border = BorderStroke(1.dp, if (darkMode) Gray_6 else Gray_3),
        colors = colors,
        onClick = onClick
    ) {
        FlatTextOnButton(text)
    }
}

@Composable
fun FlatSmallPrimaryTextButton(
    text: String,
    modifier: Modifier = Modifier.defaultMinSize(minWidth = 86.dp),
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val darkMode = isDarkTheme()
    val colors = ButtonDefaults.textButtonColors(
        backgroundColor = if (enabled) MaterialTheme.colors.primary else if (darkMode) Gray_9 else Gray_2,
        contentColor = if (darkMode) Gray_0 else Gray_0,
        disabledContentColor = if (darkMode) Gray_7 else Gray_5
    )

    TextButton(
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shape = Shapes.small,
        onClick = onClick
    ) {
        FlatTextOnButton(text)
    }
}

@Composable
fun FlatSmallSecondaryTextButton(
    text: String,
    modifier: Modifier = Modifier.defaultMinSize(minWidth = 86.dp),
    enabled: Boolean = true,
    colors: ButtonColors? = null,
    onClick: () -> Unit,
) {
    val darkMode = isDarkTheme()
    val colors = colors ?: ButtonDefaults.outlinedButtonColors(
        contentColor = if (darkMode) Gray_4 else Gray_6,
        disabledContentColor = if (darkMode) Gray_7 else Gray_5
    )

    OutlinedButton(
        modifier = modifier,
        enabled = enabled,
        shape = Shapes.small,
        border = BorderStroke(1.dp, if (darkMode) Gray_6 else Gray_3),
        colors = colors,
        onClick = onClick
    ) {
        FlatTextOnButton(text)
    }
}