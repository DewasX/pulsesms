package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoogleBlue
import com.example.viewmodel.MessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MessagesViewModel,
    onNavigateToChat: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()

    // Mock categorized filters
    var selectedFilterCategory by remember { mutableStateOf<String?>(null) }

    val mockCategorizedStaticFiles = remember(selectedFilterCategory) {
        when (selectedFilterCategory) {
            "Photos" -> listOf(
                StaticFileItem("Sarah Jenkins", "Shared photo: Beach_Sunset.jpg", Icons.Outlined.Image, 4),
                StaticFileItem("David Chen", "Shared photo: Invoice_Receipt.png", Icons.Outlined.Image, 4)
            )
            "Links" -> listOf(
                StaticFileItem("Netflix Security & Alerts", "Link: https://netflix.com/your-verify-pass", Icons.Outlined.Link, 2),
                StaticFileItem("Sarah Jenkins", "Link: https://google.com/maps/cafe-bakery", Icons.Outlined.Link, 4)
            )
            "OTPs" -> listOf(
                StaticFileItem("Google Accounts", "OTP Security Code: 482910 delivered", Icons.Outlined.AutoAwesome, 3)
            )
            "Transactions" -> listOf(
                StaticFileItem("Netflix", "Confirmation details for billing of $15.49", Icons.Outlined.Payments, 2)
            )
            else -> emptyList()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_screen_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant search bar header matching Google Messages
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                TextField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.setSearchQuery(it)
                        if (it.isNotEmpty()) selectedFilterCategory = null
                    },
                    placeholder = { Text("Search messages, contacts, links...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GoogleBlue) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                query = ""
                                viewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input_field")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            if (query.isEmpty() && selectedFilterCategory == null) {
                // Render category choices
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Search by category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SearchCategoryPill(
                            label = "Photos",
                            icon = Icons.Outlined.Image,
                            color = Color(0xFF1F85DE),
                            onClick = { selectedFilterCategory = "Photos" }
                        )

                        SearchCategoryPill(
                            label = "Links",
                            icon = Icons.Outlined.Link,
                            color = Color(0xFFE28A00),
                            onClick = { selectedFilterCategory = "Links" }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SearchCategoryPill(
                            label = "OTPs",
                            icon = Icons.Outlined.AutoAwesome,
                            color = Color(0xFF289053),
                            onClick = { selectedFilterCategory = "OTPs" }
                        )

                        SearchCategoryPill(
                            label = "Transactions",
                            icon = Icons.Outlined.Payments,
                            color = Color(0xFF9012CE),
                            onClick = { selectedFilterCategory = "Transactions" }
                        )
                    }
                }
            } else if (selectedFilterCategory != null) {
                // Render files found under categories
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Matching $selectedFilterCategory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GoogleBlue
                        )
                        
                        TextButton(onClick = { selectedFilterCategory = null }) {
                            Text("Clear filter")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(mockCategorizedStaticFiles) { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToChat(file.conversationId) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(GoogleBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(file.icon, contentDescription = file.label, tint = GoogleBlue)
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        text = file.contact,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = file.label,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Render real-time active message database matching results
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if (searchResults.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Outlined.Search, contentDescription = "Empty result", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No text messages match your term", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(searchResults, key = { it.id }) { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToChat(msg.conversationId) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Chat, contentDescription = "msg", tint = MaterialTheme.colorScheme.secondary)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = msg.body,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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

@Composable
fun SearchCategoryPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(72.dp)
            .clickable { onClick() }
            .width(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color
            )
        }
    }
}

data class StaticFileItem(
    val contact: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val conversationId: Int
)
