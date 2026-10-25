package com.example.myfirstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myfirstapp.data.repository.NoteRepositoryImpl
import com.example.myfirstapp.ui.navigation.AppNavigation
import com.example.myfirstapp.ui.theme.VoxMindTheme

class MainActivity : ComponentActivity() {

    private val repository = NoteRepositoryImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoxMindTheme {
                AppNavigation(repository = repository)
            }
        }
    }
}
