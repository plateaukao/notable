package com.olup.notable.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.olup.notable.DrawCanvas
import com.olup.notable.components.QuickNav

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun Router() {
    val navController = rememberNavController()
    var isQuickNavOpen by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = isQuickNavOpen, block = {
        DrawCanvas.isDrawing.emit(!isQuickNavOpen)
    })

    NavHost(
        navController = navController,
        startDestination = "library?folderId={folderId}",
    ) {
        composable(
            route = "library?folderId={folderId}",
            arguments = listOf(navArgument("folderId") { nullable = true })
        ) {
            /* Using composable function */
            Library(navController = navController, folderId = it.arguments?.getString("folderId"))
        }
        composable(
            route = "books/{bookId}/pages/{pageId}",
            arguments = listOf(navArgument("bookId") {
                /* configuring arguments for navigation */
                type = NavType.StringType
            }, navArgument("pageId") {
                type = NavType.StringType
            })
        ) {
            EditorView(
                navController = navController,
                _bookId = it.arguments?.getString("bookId")!!,
                _pageId = it.arguments?.getString("pageId")!!
            )
        }
        composable(
            route = "pages/{pageId}",
            arguments = listOf(navArgument("pageId") {
                type = NavType.StringType
            })
        ) {
            EditorView(
                navController = navController,
                _bookId = null,
                _pageId = it.arguments?.getString("pageId")!!
            )
        }
        composable(
            route = "books/{bookId}/pages",
            arguments = listOf(navArgument("bookId") {
                /* configuring arguments for navigation */
                type = NavType.StringType
            })
        ) {
            PagesView(
                navController = navController,
                bookId = it.arguments?.getString("bookId")!!
            )
        }
    }

    if (isQuickNavOpen) QuickNav(navController = navController, { isQuickNavOpen = false })
    else Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .pointerInteropFilter {
                    if (it.size == 0f) return@pointerInteropFilter true
                    false
                }
                .pointerInput(Unit) {

                    detectTapGestures(
                        onDoubleTap = {
                            isQuickNavOpen = true
                        }
                    )
                })
    }

}
