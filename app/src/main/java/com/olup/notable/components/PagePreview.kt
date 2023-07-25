package com.olup.notable

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
fun PagePreview(modifier: Modifier, pageId: String){
    val context = LocalContext.current
    val imgFile = remember {
        File(context.filesDir, "pages/previews/thumbs/$pageId")
    }

    var imgBitmap: Bitmap? = null
    if (imgFile.exists()) {
        imgBitmap = remember {
            BitmapFactory.decodeFile(imgFile.absolutePath)
        }
    }
    Image(
        painter = BitmapPainter(imgBitmap?.asImageBitmap()!!),
        contentDescription = "Image",
        contentScale = ContentScale.FillWidth,
        modifier = modifier.then(Modifier.background(Color.LightGray))
    )
}