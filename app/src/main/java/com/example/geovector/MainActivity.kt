package com.example.geovector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.geovector.presentation.navigation.AppNavGraph
import com.example.geovector.ui.theme.GeoVectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoVectorTheme {
                val nav = rememberNavController()
                AppNavGraph(nav)
            }
        }
    }
}
