package com.example.blesearchum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blesearchum.ui.BibleSearchScreen
import com.example.blesearchum.ui.BibleViewModel
import com.example.blesearchum.ui.theme.BibleSearchUMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BibleSearchUMTheme {
                val bibleViewModel: BibleViewModel = viewModel()
                BibleSearchScreen(viewModel = bibleViewModel)
            }
        }
    }
}
