package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.JournalEntry
import com.example.ui.viewmodel.DisciplineViewModel
import com.example.util.DateUtils

@Composable
fun JournalScreen(
    viewModel: DisciplineViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedJournalDate.collectAsState()
    val journalText by viewModel.activeJournalText.collectAsState()
    val activeJournalImageUri by viewModel.activeJournalImageUri.collectAsState()
    val allEntries by viewModel.journalEntries.collectAsState()

    var draftText by remember(selectedDate, journalText) { mutableStateOf(journalText) }

    val todayStr = DateUtils.getCurrentDateString()
    val displayDate = DateUtils.getFormattedDate(selectedDate)

    // Check if user is viewing today (blocks navigating to future dates)
    val isToday = selectedDate == todayStr

    val context = androidx.compose.ui.platform.LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val fileName = "journal_img_${System.currentTimeMillis()}.jpg"
                        val file = java.io.File(context.filesDir, fileName)
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        viewModel.updateActiveJournalImage(file.absolutePath)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Title Row
        Text(
            text = "DEEP INSIGHT & AWARENESS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "REFLECTION",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date Picker Nav Toolbar
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        viewModel.saveJournalEntry(draftText)
                        val prevDate = DateUtils.addDaysToDateString(selectedDate, -1)
                        viewModel.selectJournalDate(prevDate)
                    }
                ) {
                    Text(
                        text = "<",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = displayDate.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                IconButton(
                    onClick = {
                        if (!isToday) {
                            viewModel.saveJournalEntry(draftText)
                            val nextDate = DateUtils.addDaysToDateString(selectedDate, 1)
                            viewModel.selectJournalDate(nextDate)
                        }
                    },
                    enabled = !isToday
                ) {
                    Text(
                        text = ">",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isToday) {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Immersive text pad
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TextField(
                    value = draftText,
                    onValueChange = { draftText = it },
                    placeholder = {
                        Text(
                            text = "No metrics. No validation.\nWrite honestly about your work, distractions faced, emotional posture, and tomorrow's self-disciplined baseline...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            lineHeight = 22.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("journal_body_input")
                )

                // Render attached image inside the column if present
                activeJournalImageUri?.let { path ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AsyncImage(
                                model = path,
                                contentDescription = "Journal attachment",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }

                        // Remove button on top-right of image
                        IconButton(
                            onClick = { viewModel.updateActiveJournalImage(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Remove image",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image picker button (left aligned)
                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach image button",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (activeJournalImageUri == null) "ADD IMAGE" else "REPLACE IMAGE",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Save / Commit Action button (right aligned)
                    Button(
                        onClick = {
                            viewModel.saveJournalEntry(draftText)
                            android.widget.Toast.makeText(context, "Reflection Saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("save_journal_button")
                    ) {
                        Text("COMMIT REFLECTION", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of history (only if we have alternate historical logs written)
        val historyEntries = allEntries.filter { it.date != selectedDate }
        if (historyEntries.isNotEmpty()) {
            Text(
                text = "HISTORICAL REFLECTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyEntries, key = { it.id }) { entry ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.saveJournalEntry(draftText)
                                viewModel.selectJournalDate(entry.date)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Reflection log",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = DateUtils.getFormattedDate(entry.date).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = entry.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (entry.imageUri != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Card(
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    AsyncImage(
                                        model = entry.imageUri,
                                        contentDescription = "Historical preview image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
