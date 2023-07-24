package com.olup.notable

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.olup.notable.ui.theme.InkaTheme
import com.olup.notable.views.Router
import com.onyx.android.sdk.api.device.epd.EpdController
import kotlinx.coroutines.launch


var SCREEN_WIDTH = EpdController.getEpdHeight().toInt()
var SCREEN_HEIGHT = EpdController.getEpdWidth().toInt()

var TAG = "MainActivity"
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideStatusBar()

        if(SCREEN_WIDTH == 0){
            SCREEN_WIDTH = applicationContext.resources.displayMetrics.widthPixels
            SCREEN_HEIGHT = applicationContext.resources.displayMetrics.heightPixels
        }

        val snackState = SnackState()
        // Refactor - we prob don't need this
        EditorSettingCacheManager.init(applicationContext)


        //EpdDeviceManager.enterAnimationUpdate(true);


        setContent {
            InkaTheme {
                CompositionLocalProvider(SnackContext provides snackState ) {
                    Box(
                        Modifier
                            .background(Color.White)
                    ) {
                        Router()
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.Black)
                    )
                    SnackBar(state = snackState)
                }
            }
        }
    }


    override fun onRestart() {
        super.onRestart()
        // redraw after device sleep
        this.lifecycleScope.launch {
                DrawCanvas.restartAfterConfChange.emit(Unit)
        }
    }

    override fun onPause() {
        super.onPause()
        this.lifecycleScope.launch {
            DrawCanvas.refreshUi.emit(Unit)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        this.lifecycleScope.launch {
            DrawCanvas.refreshUi.emit(Unit)
        }
    }





    override fun onContentChanged() {
        super.onContentChanged()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    }

    private fun hideStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.insetsController?.hide(WindowInsets.Type.systemBars())
//            window.setDecorFitsSystemWindows(false)
//        }
    }
}