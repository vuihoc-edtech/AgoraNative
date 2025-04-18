package io.agora.flat.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import io.agora.vuihoc.agora_native.R

@OptIn(ExperimentalCoilApi::class)
@Composable
fun FlatAvatar(avatar: Any?, size: Dp) {
    Image(
        painter = rememberImagePainter(avatar) {
            placeholder(R.drawable.ic_register_avatar)
        },
        contentDescription = null,
        modifier = Modifier
            .size(size, size)
            .clip(shape = RoundedCornerShape(size / 2)),
        contentScale = ContentScale.Crop
    )
}
