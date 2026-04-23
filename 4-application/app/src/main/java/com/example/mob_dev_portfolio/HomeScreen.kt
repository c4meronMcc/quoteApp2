package com.example.mob_dev_portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    viewModel: QuoteViewModel,
    onProfileSelected: (Int) -> Unit,
    onImportClicked: () -> Unit
) {
    val savedProfiles = viewModel.savedProfiles

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var renameTargetId by remember { mutableIntStateOf(-1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableIntStateOf(-1) }

    // Rename dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = Paper,
            title = {
                Text(
                    "Rename Profile",
                    fontFamily = DisplayFont,
                    fontSize = 20.sp,
                    color = Ink
                )
            },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Profile Name", fontFamily = BodyFont) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber,
                        focusedLabelColor  = Amber,
                        cursorColor        = Amber
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) {
                        viewModel.renameProfileFromHome(renameTargetId, renameText)
                        showRenameDialog = false
                    }
                }) {
                    Text("Save", color = Amber, fontFamily = BodyFont, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Muted, fontFamily = BodyFont)
                }
            }
        )
    }



    // Root layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Paper)
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 20.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Quote")
                        withStyle(
                            SpanStyle(
                                color      = Amber,
                                fontStyle  = FontStyle.Italic,
                                fontFamily = DisplayFont
                            )
                        ) { append("scout") }
                    },
                    fontFamily = DisplayFont,
                    fontSize   = 30.sp,
                    color      = Ink,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text          = "Compare · Save · Decide",
                    fontFamily    = BodyFont,
                    fontSize      = 12.sp,
                    color         = Muted,
                    letterSpacing = 1.5.sp
                )
            }
            HorizontalDivider(color = Border, thickness = 1.dp)

            // Content list
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(
                    start  = 24.dp,
                    end    = 24.dp,
                    top    = 24.dp,
                    bottom = 120.dp   // room for FAB
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Pinned section
                val pinnedProfiles = savedProfiles.filter { it.isPinned }
                if (pinnedProfiles.isNotEmpty()) {
                    item { SectionLabel("Pinned") }
                    item { Spacer(Modifier.height(12.dp)) }
                    items(pinnedProfiles) { profile ->
                        PinnedProfileCard(
                            profile          = profile,
                            onProfileSelected = onProfileSelected,
                            onPin            = { viewModel.togglePin(profile.id, profile.isPinned) },
                            onDelete = {
                                deleteTargetId = profile.id
                                showDeleteDialog = true
                            },
                            onRename         = {
                                renameTargetId = profile.id
                                renameText     = profile.profileName
                                showRenameDialog = true
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }

                // Recent section
                item { SectionLabel("Recent Quotes") }
                item { Spacer(Modifier.height(4.dp)) }

                if (savedProfiles.isEmpty()) {
                    item {
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "No saved profiles yet — tap + to import",
                            fontFamily = BodyFont,
                            fontSize   = 14.sp,
                            color      = Muted
                        )
                    }
                } else {
                    val recentProfiles = savedProfiles.filter { !it.isPinned }
                    items(recentProfiles) { profile ->
                        RecentProfileRow(
                            profile           = profile,
                            onProfileSelected = onProfileSelected,
                            onPin             = { viewModel.togglePin(profile.id, profile.isPinned) },
                            onDelete          = { viewModel.deleteProfile(profile.id) },
                            onRename          = {
                                renameTargetId = profile.id
                                renameText     = profile.profileName
                                showRenameDialog = true
                            }
                        )
                    }
                }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 32.dp)
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Amber)
                .clickable { onImportClicked() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Import",
                tint   = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// Section label
@Composable
private fun SectionLabel(text: String) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        verticalAlignment   = Alignment.CenterVertically
    ) {
        Text(
            text          = text.uppercase(),
            fontFamily    = BodyFont,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = Muted,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.width(10.dp))
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = Border,
            thickness = 1.dp
        )
    }
}

// Pinned card (dark, shows profile name + date)
@Composable
private fun PinnedProfileCard(
    profile: QuoteProfileSummary,
    onProfileSelected: (Int) -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Ink)
            .clickable { onProfileSelected(profile.id) }
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            // Pinned badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AmberLight.copy(alpha = 0.15f))
                    .border(1.dp, AmberLight.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "PINNED",
                    fontFamily    = BodyFont,
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = AmberLight,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text       = profile.profileName,
                fontFamily = DisplayFont,
                fontSize   = 20.sp,
                color      = Color.White,
                letterSpacing = (-0.3).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = "Imported ${profile.createdAt}",
                fontFamily = BodyFont,
                fontSize   = 11.sp,
                color      = Color.White.copy(alpha = 0.4f)
            )
        }

        // Three-dot menu
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { expanded = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint   = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
            ProfileDropdownMenu(
                expanded   = expanded,
                isPinned   = profile.isPinned,
                onDismiss  = { expanded = false },
                onPin      = { expanded = false; onPin() },
                onRename   = { expanded = false; onRename() },
                onDelete   = { expanded = false; onDelete() }
            )
        }
    }
}

// Recent row
@Composable
private fun RecentProfileRow(
    profile: QuoteProfileSummary,
    onProfileSelected: (Int) -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onProfileSelected(profile.id) }
                .padding(vertical = 12.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Cream)
                    .border(1.dp, Border, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("📋", fontSize = 18.sp)
            }

            // Name + date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = profile.profileName,
                    fontFamily = BodyFont,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp,
                    color      = Ink
                )
                Text(
                    text       = "Imported ${profile.createdAt}",
                    fontFamily = BodyFont,
                    fontSize   = 11.sp,
                    color      = Muted
                )
            }

            // Three-dot menu
            Box {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint   = Muted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                ProfileDropdownMenu(
                    expanded  = expanded,
                    isPinned  = profile.isPinned,
                    onDismiss = { expanded = false },
                    onPin     = { expanded = false; onPin() },
                    onRename  = { expanded = false; onRename() },
                    onDelete  = { expanded = false; onDelete() }
                )
            }
        }
        HorizontalDivider(color = Border, thickness = 0.5.dp)
    }
}

// shared drop down menu for profile options
@Composable
private fun ProfileDropdownMenu(
    expanded: Boolean,
    isPinned: Boolean,
    onDismiss: () -> Unit,
    onPin: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded          = expanded,
        onDismissRequest  = onDismiss,
    ) {
        DropdownMenuItem(
            text         = { Text(if (isPinned) "Unpin" else "Pin", fontFamily = BodyFont, fontSize = 13.sp) },
            leadingIcon  = { Icon(Icons.Default.Star, contentDescription = null, tint = Amber, modifier = Modifier.size(17.dp)) },
            onClick      = onPin
        )
        DropdownMenuItem(
            text         = { Text("Rename", fontFamily = BodyFont, fontSize = 13.sp) },
            leadingIcon  = { Icon(Icons.Default.Edit, contentDescription = null, tint = Muted, modifier = Modifier.size(17.dp)) },
            onClick      = onRename
        )
        DropdownMenuItem(
            text         = { Text("Delete", fontFamily = BodyFont, fontSize = 13.sp, color = QuoteRed) },
            leadingIcon  = { Icon(Icons.Default.Delete, contentDescription = null, tint = QuoteRed, modifier = Modifier.size(17.dp)) },
            onClick      = onDelete
        )
    }
}