package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DevEmerald

@Composable
fun QuickCommandPalette(
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commandText by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Prompt
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " DEV COMMAND PALETTE (CMD+K / CLI)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Command Input TextField
            OutlinedTextField(
                value = commandText,
                onValueChange = { commandText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        ":add Task title #bug !p0 ^today @core",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Text(
                        ">",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                },
                trailingIcon = {
                    Row {
                        if (commandText.isNotEmpty()) {
                            IconButton(onClick = { commandText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if (commandText.isNotBlank()) {
                                    onExecuteCommand(commandText)
                                    commandText = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Run",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (commandText.isNotBlank()) {
                            onExecuteCommand(commandText)
                            commandText = ""
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Quick Syntax Shortcut Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickTagChip(label = "+ :add", onClick = {
                    if (!commandText.startsWith(":add")) commandText = ":add $commandText"
                })
                QuickTagChip(label = "#bug", onClick = { commandText = "$commandText #bug" })
                QuickTagChip(label = "#feat", onClick = { commandText = "$commandText #feat" })
                QuickTagChip(label = "#refactor", onClick = { commandText = "$commandText #refactor" })
                QuickTagChip(label = "!p0 (Crit)", onClick = { commandText = "$commandText !p0" })
                QuickTagChip(label = "!p1 (High)", onClick = { commandText = "$commandText !p1" })
                QuickTagChip(label = "^today", onClick = { commandText = "$commandText ^today" })
                QuickTagChip(label = "~45m", onClick = { commandText = "$commandText ~45m" })
            }
        }
    }
}

@Composable
private fun QuickTagChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
