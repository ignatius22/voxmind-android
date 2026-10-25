package com.example.myfirstapp.ui.screens.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstapp.domain.model.NoteCategory
import com.example.myfirstapp.ui.components.AudioWaveformVisualizer
import com.example.myfirstapp.ui.theme.AccentCyan
import com.example.myfirstapp.ui.theme.DarkBackground
import com.example.myfirstapp.ui.theme.DarkCardBg
import com.example.myfirstapp.ui.theme.DarkSurface
import com.example.myfirstapp.ui.theme.PrimaryGradientEnd
import com.example.myfirstapp.ui.theme.PrimaryViolet
import com.example.myfirstapp.ui.theme.StatusAmber
import com.example.myfirstapp.ui.theme.TextPrimary
import com.example.myfirstapp.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun RecordScreen(
    viewModel: RecordViewModel,
    onBackClick: () -> Unit,
    onRecordingCompleted: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val minutes = state.elapsedSeconds / 60
    val seconds = state.elapsedSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New AI Voice Note",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.onTitleChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note Title (Optional)") },
                    placeholder = { Text("e.g. Weekly Sync with Marketing") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryViolet,
                        unfocusedBorderColor = DarkSurface,
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Selection
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(NoteCategory.entries.toTypedArray()) { category ->
                        if (category != NoteCategory.ALL) {
                            val isSelected = state.category == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onCategorySelected(category) },
                                label = { Text(category.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryViolet,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = DarkCardBg,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Central Recording Dashboard
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                // Timer
                Text(
                    text = formattedTime,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (state.isRecording && !state.isPaused) PrimaryViolet else TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        state.isProcessing -> "AI is analyzing audio & generating notes..."
                        state.isRecording && !state.isPaused -> "Recording live audio..."
                        state.isPaused -> "Recording paused"
                        else -> "Tap microphone to start recording"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.isRecording && !state.isPaused) AccentCyan else TextSecondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Dynamic Waveform Visualizer
                AudioWaveformVisualizer(
                    isRecording = state.isRecording && !state.isPaused,
                    barCount = 28
                )
            }

            // Controls Bottom Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isProcessing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryViolet)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Transcribing & Extracting Key Takeaways...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentCyan
                        )
                    }
                } else if (!state.isRecording && state.elapsedSeconds == 0) {
                    // Record Button
                    IconButton(
                        onClick = { viewModel.startRecording() },
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(PrimaryViolet, PrimaryGradientEnd)
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Start Recording",
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    // Controls (Pause/Resume + Finish/Summarize)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pause / Resume
                        IconButton(
                            onClick = {
                                if (state.isPaused) viewModel.resumeRecording()
                                else viewModel.pauseRecording()
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(DarkCardBg)
                        ) {
                            Icon(
                                imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (state.isPaused) "Resume" else "Pause",
                                tint = StatusAmber,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Generate AI Summary Button
                        Button(
                            onClick = {
                                viewModel.finishAndGenerateSummary { id ->
                                    onRecordingCompleted(id)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generate AI Notes",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
