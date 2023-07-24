package com.olup.notable.views

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.olup.notable.AppRepository
import com.olup.notable.EditorControlTower
import com.olup.notable.EditorGestureReceiver
import com.olup.notable.EditorSettingCacheManager
import com.olup.notable.EditorState
import com.olup.notable.EditorSurface
import com.olup.notable.History
import com.olup.notable.PageView
import com.olup.notable.ScrollIndicator
import com.olup.notable.SelectedBitmap
import com.olup.notable.Toolbar
import com.olup.notable.convertDpToPixel
import com.olup.notable.ui.theme.InkaTheme


@OptIn(ExperimentalComposeUiApi::class)
@Composable
@ExperimentalFoundationApi
fun EditorView(
    navController: NavController, _bookId: String?, _pageId: String
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // control if we do have a page
    if (AppRepository(context).pageRepository.getById(_pageId) == null) {
        if (_bookId != null) {
            // clean the book
            Log.i(com.olup.notable.TAG, "Cleaning book")
            AppRepository(context).bookRepository.removePage(_bookId, _pageId)
        }
        navController.navigate("library")
        return
    }

    BoxWithConstraints() {
        var height = convertDpToPixel(this.maxHeight, context).toInt()
        var width = convertDpToPixel(this.maxWidth, context).toInt()


        val page = remember {
            PageView(
                context = context,
                coroutineScope = scope,
                id = _pageId,
                width = width,
                viewWidth = width,
                viewHeight = height
            )
        }

        val editorState =
            remember { EditorState(bookId = _bookId, pageId = _pageId, pageView = page) }

        val history = remember {
            History(scope, page)
        }
        val editorControlTower = remember {
            EditorControlTower(scope, page, history, editorState)
        }

        val appRepository = AppRepository(context)

        // update opened page
        LaunchedEffect(Unit) {
            if (_bookId != null) {
                appRepository.bookRepository.setOpenPageId(_bookId, _pageId)
            }
        }

        // TODO put in editorSetting class
        LaunchedEffect(
            editorState.isToolbarOpen,
            editorState.pen,
            editorState.penSettings,
            editorState.mode
        ) {
            Log.i(com.olup.notable.TAG, "saving")
            EditorSettingCacheManager.setEditorSettings(
                context,
                EditorSettingCacheManager.EditorSettings(
                    isToolbarOpen = editorState.isToolbarOpen,
                    mode = editorState.mode,
                    pen = editorState.pen,
                    eraser = editorState.eraser,
                    penSettings = editorState.penSettings
                )
            )
        }

        val lastRoute = navController.previousBackStackEntry

        fun goToNextPage() {
            if (_bookId != null) {
                val newPageId = appRepository.getNextPageIdFromBookAndPage(
                    pageId = _pageId, notebookId = _bookId!!
                )
                navController.navigate("books/${_bookId}/pages/${newPageId}") {
                    popUpTo(lastRoute!!.destination.id) {
                        inclusive = false
                    }
                }
            }
        }

        fun goToPreviousPage() {
            if (_bookId != null) {
                val newPageId = appRepository.getPreviousPageIdFromBookAndPage(
                    pageId = _pageId, notebookId = _bookId!!
                )
                if (newPageId != null) navController.navigate("books/${_bookId}/pages/${newPageId}")
            }
        }



        InkaTheme {
            EditorSurface(
                state = editorState, page = page, history = history
            )
            EditorGestureReceiver(
                goToNextPage = ::goToNextPage,
                goToPreviousPage = ::goToPreviousPage,
                controlTower = editorControlTower,
                state = editorState
            )
            SelectedBitmap(editorState = editorState, controlTower = editorControlTower)
            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()){
                Spacer(modifier = Modifier.weight(1f))
                ScrollIndicator(context = context, state = editorState)
            }
            Toolbar(
                navController = navController, state = editorState
            )

        }
    }
}


