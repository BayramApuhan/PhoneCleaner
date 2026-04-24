package com.bayramapuhan.phonecleaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bayramapuhan.phonecleaner.ui.navigation.AppNavGraph
import com.bayramapuhan.phonecleaner.ui.theme.PhoneCleanerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoneCleanerTheme {
                AppNavGraph()
            }
        }
    }
}
