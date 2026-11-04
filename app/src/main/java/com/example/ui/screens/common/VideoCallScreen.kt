package com.example.ui.screens.common

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.repository.LiveClassRepository
import com.example.model.LiveClassMessage
import com.example.model.LiveRoomParticipant
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class RoomInteractionTab(val label: String) {
    VIDEO("Live Video"),
    CHAT("In-Class Q&A"),
    PARTICIPANTS("Participants"),
    WHITEBOARD("Shared Notes")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    title: String,
    roomCode: String,
    isHost: Boolean,
    currentUser: User? = null,
    liveClassRepo: LiveClassRepository? = null,
    onLeaveCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { liveClassRepo ?: LiveClassRepository() }

    // Safe, alphanumeric room identifier for WebRTC conferencing
    val cleanRoom = remember(roomCode) {
        val sanitized = roomCode.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        if (sanitized.startsWith("slaty_") || sanitized.startsWith("slaty-")) sanitized
        else "slaty_$sanitized"
    }

    val participantName = remember(currentUser, isHost) {
        currentUser?.name ?: if (isHost) "Certified Tutor" else "Student Scholar"
    }

    val participantUser = remember(currentUser, isHost, participantName) {
        currentUser ?: User(
            id = "usr_${System.currentTimeMillis() % 100000}",
            name = participantName,
            username = participantName.lowercase().replace(" ", "_"),
            email = "scholar@slaty.edu.ng",
            role = if (isHost) UserRole.TUTOR else UserRole.STUDENT
        )
    }

    // Active interaction tab
    var selectedTab by remember { mutableStateOf(RoomInteractionTab.VIDEO) }

    // Live Room State synced from Firestore
    var participants by remember { mutableStateOf<List<LiveRoomParticipant>>(emptyList()) }
    var inRoomMessages by remember { mutableStateOf<List<LiveClassMessage>>(emptyList()) }
    var sharedNotes by remember { mutableStateOf("") }
    var isHandRaised by remember { mutableStateOf(false) }
    var latestHandRaiseAlert by remember { mutableStateOf<String?>(null) }
    var activeFloatingReaction by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Message Input
    var messageText by remember { mutableStateOf("") }
    var isQuestionFilter by remember { mutableStateOf(false) }
    var markAsQuestion by remember { mutableStateOf(false) }
    var notesDraft by remember { mutableStateOf("") }
    var isNotesSaving by remember { mutableStateOf(false) }

    // Check & request runtime permissions for Camera and Microphone
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasCameraPermission = grants[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = grants[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionsLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    // Join room presence in Firestore and clean up on exit
    DisposableEffect(cleanRoom) {
        scope.launch {
            repo.joinRoom(cleanRoom, participantUser, isHost)
        }
        onDispose {
            scope.launch {
                repo.leaveRoom(cleanRoom, participantUser.id)
            }
        }
    }

    // Real-time synchronization of room participants
    LaunchedEffect(cleanRoom) {
        repo.observeRoomParticipants(cleanRoom).collect { list ->
            participants = list
            // Check if someone raised their hand
            val raised = list.firstOrNull { it.isHandRaised && it.userId != participantUser.id }
            if (raised != null) {
                latestHandRaiseAlert = "${raised.name} raised their hand!"
            }
        }
    }

    // Real-time synchronization of in-class chat & Q&A
    LaunchedEffect(cleanRoom) {
        repo.observeRoomMessages(cleanRoom).collect { msgs ->
            inRoomMessages = msgs
        }
    }

    // Real-time synchronization of shared lecture notes
    LaunchedEffect(cleanRoom) {
        repo.observeSharedNotes(cleanRoom).collect { notes ->
            sharedNotes = notes
            if (!isNotesSaving) {
                notesDraft = notes
            }
        }
    }

    // Real-time reaction broadcasts
    LaunchedEffect(cleanRoom) {
        repo.observeReactions(cleanRoom).collect { reaction ->
            activeFloatingReaction = reaction
            delay(3000)
            activeFloatingReaction = null
        }
    }

    val jitsiUrl = remember(cleanRoom, participantName) {
        val encodedName = Uri.encode(participantName)
        "https://meet.jit.si/$cleanRoom#config.prejoinPageEnabled=false&config.disableDeepLinking=true&userInfo.displayName=$encodedName&config.startWithAudioMuted=false&config.startWithVideoMuted=false&config.enableWelcomePage=false"
    }

    Scaffold(
        containerColor = Color(0xFF090810),
        topBar = {
            Surface(
                color = Color(0xFF14121F),
                border = BorderStroke(1.dp, Color(0xFF28233D))
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF3B30))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE • ${maxOf(participants.size, 1)} IN CLASS",
                                    color = Color(0xFFFF4D4D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Room code copy chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF221D34),
                            border = BorderStroke(1.dp, Color(0xFF383152)),
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Slaty Room Code", cleanRoom)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Room Code Copied: $cleanRoom", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = SlatyMint,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Code: $cleanRoom",
                                    color = SlatyMint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Interactive Tab Navigation Bar
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = SlatyPrimary,
                        edgePadding = 12.dp,
                        divider = {}
                    ) {
                        RoomInteractionTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tab.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp,
                                            color = if (isSelected) Color.White else SlatyTextSecondary
                                        )
                                        if (tab == RoomInteractionTab.PARTICIPANTS && participants.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) SlatyMint else Color(0xFF2A253F))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "${participants.size}",
                                                    color = if (isSelected) Color.Black else Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        if (tab == RoomInteractionTab.CHAT && inRoomMessages.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(SlatyPrimary)
                                                    .size(6.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Live Control Dock
            Surface(
                color = Color(0xFF13111E),
                border = BorderStroke(1.dp, Color(0xFF28233D)),
                modifier = Modifier.navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Floating hand raise alert banner
                    latestHandRaiseAlert?.let { alert ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2E2214),
                            border = BorderStroke(1.dp, Color(0xFFFF9800)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "✋", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = alert,
                                        color = Color(0xFFFFB74D),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                TextButton(
                                    onClick = { latestHandRaiseAlert = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Dismiss", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hand Raise Toggle
                        IconButton(
                            onClick = {
                                isHandRaised = !isHandRaised
                                scope.launch {
                                    repo.setHandRaised(cleanRoom, participantUser.id, isHandRaised)
                                }
                                Toast.makeText(
                                    context,
                                    if (isHandRaised) "Hand Raised ✋ (Tutor notified)" else "Hand Lowered",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isHandRaised) Color(0xFFFF9800) else Color(0xFF252038))
                                .testTag("hand_raise_button")
                        ) {
                            Text(text = "✋", fontSize = 18.sp)
                        }

                        // Quick Reaction Buttons (👍, 💡, 👏, ❤️)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("👍", "💡", "👏", "❤️").forEach { emoji ->
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF221D35),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable {
                                            scope.launch {
                                                repo.sendReaction(cleanRoom, emoji, participantName)
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = emoji, fontSize = 16.sp)
                                    }
                                }
                            }
                        }

                        // Leave Call Button
                        Button(
                            onClick = onLeaveCall,
                            colors = ButtonDefaults.buttonColors(containerColor = SlatyError),
                            shape = RoundedCornerShape(22.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("leave_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "Leave",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Leave", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                RoomInteractionTab.VIDEO -> {
                    // Embedded WebRTC Video Conferencing Stage
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.allowFileAccess = true
                                settings.allowContentAccess = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true

                                webViewClient = WebViewClient()
                                webChromeClient = object : WebChromeClient() {
                                    override fun onPermissionRequest(request: PermissionRequest?) {
                                        // Grant camera and microphone permissions for live video & audio stream
                                        request?.grant(request.resources)
                                    }
                                }
                                loadUrl(jitsiUrl)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("live_video_webview")
                    )
                }

                RoomInteractionTab.CHAT -> {
                    // Live In-Class Q&A and Chat Room
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        // Filter Bar (All vs Questions)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !isQuestionFilter,
                                onClick = { isQuestionFilter = false },
                                label = { Text("All Chat (${inRoomMessages.size})", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SlatyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = isQuestionFilter,
                                onClick = { isQuestionFilter = true },
                                label = { Text("Questions Only ❓ (${inRoomMessages.count { it.isQuestion }})", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF9800),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        val displayedMessages = remember(inRoomMessages, isQuestionFilter) {
                            if (isQuestionFilter) inRoomMessages.filter { it.isQuestion }
                            else inRoomMessages
                        }

                        val listState = rememberLazyListState()
                        LaunchedEffect(displayedMessages.size) {
                            if (displayedMessages.isNotEmpty()) {
                                listState.animateScrollToItem(displayedMessages.size - 1)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (displayedMessages.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.ChatBubbleOutline,
                                                contentDescription = "Chat",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (isQuestionFilter) "No questions asked yet." else "Live classroom chat is ready.",
                                                color = SlatyTextSecondary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(displayedMessages) { msg ->
                                    val isMe = msg.senderId == participantUser.id
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isMe) "You" else msg.senderName,
                                                color = if (msg.senderRole == UserRole.TUTOR) SlatyMint else SlatyLavender,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (msg.senderRole == UserRole.TUTOR) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "TUTOR",
                                                    color = Color.Black,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(SlatyMint)
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                            if (msg.isQuestion) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "QUESTION",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFFF9800))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isMe) SlatyPrimary.copy(alpha = 0.85f) else Color(0xFF1E1B2C),
                                            border = if (msg.isQuestion) BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.6f)) else null,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = msg.text,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Message Input Box
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1E1A2E),
                            border = BorderStroke(1.dp, Color(0xFF383152)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { markAsQuestion = !markAsQuestion },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Text(
                                        text = if (markAsQuestion) "❓" else "💬",
                                        fontSize = 16.sp
                                    )
                                }

                                TextField(
                                    value = messageText,
                                    onValueChange = { messageText = it },
                                    placeholder = {
                                        Text(
                                            text = if (markAsQuestion) "Ask a question to tutor..." else "Type in-class message...",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        val text = messageText.trim()
                                        if (text.isNotBlank()) {
                                            scope.launch {
                                                repo.sendRoomMessage(
                                                    cleanRoom,
                                                    LiveClassMessage(
                                                        senderId = participantUser.id,
                                                        senderName = participantName,
                                                        senderRole = participantUser.role,
                                                        text = text,
                                                        timestamp = System.currentTimeMillis(),
                                                        isQuestion = markAsQuestion
                                                    )
                                                )
                                                messageText = ""
                                                markAsQuestion = false
                                            }
                                        }
                                    },
                                    enabled = messageText.isNotBlank(),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (messageText.isNotBlank()) SlatyPrimary else Color(0xFF2C2740))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (messageText.isNotBlank()) Color.White else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                RoomInteractionTab.PARTICIPANTS -> {
                    // Participants List with Live Indicators
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Active Room Participants (${participants.size})",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (participants.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1B1828),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "You are currently the only one in the room. Share room code to invite others!", color = SlatyTextSecondary, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        items(participants) { p ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF191626),
                                border = if (p.isHandRaised) BorderStroke(1.dp, Color(0xFFFF9800)) else BorderStroke(1.dp, Color(0xFF2B2540)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (p.role == UserRole.TUTOR) Color(0xFF21806A) else SlatyPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = p.name.take(2).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = p.name + if (p.userId == participantUser.id) " (You)" else "",
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (p.isHost) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "HOST",
                                                        color = Color.Black,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(SlatyMint)
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = if (p.role == UserRole.TUTOR) "Certified Tutor" else "Student Participant",
                                                color = SlatyTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (p.isHandRaised) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFFF9800).copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, Color(0xFFFF9800))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "✋ Hand Raised", color = Color(0xFFFFB74D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                RoomInteractionTab.WHITEBOARD -> {
                    // Shared Collaborative Lecture Notes
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Live Shared Lecture Board",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "All notes and equations update live across connected devices",
                                    color = SlatyTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        isNotesSaving = true
                                        repo.updateSharedNotes(cleanRoom, notesDraft)
                                        isNotesSaving = false
                                        Toast.makeText(context, "Board saved & broadcast to all devices!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SlatyMint),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Broadcast", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notesDraft,
                            onValueChange = {
                                notesDraft = it
                                isNotesSaving = true
                            },
                            placeholder = {
                                Text(
                                    text = "Write equations, formulas, derivation steps, or lecture summaries here...",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF14121E),
                                unfocusedContainerColor = Color(0xFF14121E),
                                focusedBorderColor = SlatyMint,
                                unfocusedBorderColor = Color(0xFF2C2742),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            }

            // Floating Animated Reaction Overlay
            activeFloatingReaction?.let { (emoji, sender) ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xCC171424))
                        .border(1.dp, SlatyMint, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = emoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$sender reacted",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
