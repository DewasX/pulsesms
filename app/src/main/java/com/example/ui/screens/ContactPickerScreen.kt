package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoogleBlue
import com.example.viewmodel.MessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerScreen(
    viewModel: MessagesViewModel,
    onNavigateToChat: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var queryName by remember { mutableStateOf("") }
    var queryNumber by remember { mutableStateOf("") }

    val mockContacts = listOf(
        ContactItem("Alice Thompson", "+1 (555) 019-2831", "#1A73E8"),
        ContactItem("Bob Martinez", "+1 (555) 482-9102", "#34A853"),
        ContactItem("Charlie Davidson", "+1 (555) 283-4920", "#EA4335"),
        ContactItem("David Chen", "+1 (555) 902-8314", "#FBBC05"),
        ContactItem("Emma Watson", "+1 (555) 589-2910", "#AB47BC"),
        ContactItem("Frank Castle", "+1 (555) 219-9024", "#26C6DA")
    )

    val filteredContacts = remember(queryName) {
        if (queryName.isBlank()) {
            mockContacts
        } else {
            mockContacts.filter { it.name.contains(queryName, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("contact_picker_root"),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("New conversation", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Manual Custom Number Input
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Or start by manual credentials",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = queryName,
                        onValueChange = { queryName = it },
                        label = { Text("Search or type contact name") },
                        modifier = Modifier.fillMaxWidth().testTag("contact_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = queryNumber,
                        onValueChange = { queryNumber = it },
                        label = { Text("Type phone number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "phone") },
                        modifier = Modifier.fillMaxWidth().testTag("contact_phone_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (queryName.isNotBlank() && queryNumber.isNotBlank()) {
                                val newId = viewModel.createNewConversation(queryName, queryNumber)
                                // Navigate immediately on the next frame so database writes complete
                                onNavigateToChat(newId)
                            } else {
                                Toast.makeText(context, "Please fill in Name and Phone details first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Create")
                            Text("Start Thread Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "My local SIM Contacts",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Suggestions List
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredContacts) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val convId = viewModel.createNewConversation(contact.name, contact.phone)
                                onNavigateToChat(convId)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(contact.colorHex))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = contact.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = contact.phone,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ContactItem(
    val name: String,
    val phone: String,
    val colorHex: String
)
