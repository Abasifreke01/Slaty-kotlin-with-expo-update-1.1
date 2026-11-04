package com.example.data.repository

import android.util.Log
import com.example.model.LiveClassMessage
import com.example.model.LiveClassSession
import com.example.model.LiveRoomParticipant
import com.example.model.User
import com.example.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class LiveClassRepository(
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
) {
    companion object {
        private const val TAG = "LiveClassRepo"
        private const val COLLECTION_LIVE_CLASSES = "live_classes"
        private const val COLLECTION_LIVE_ROOMS = "live_rooms"
        private const val SUBCOLLECTION_PARTICIPANTS = "participants"
        private const val SUBCOLLECTION_MESSAGES = "messages"
        private const val SUBCOLLECTION_SHARED = "shared"
        private const val SUBCOLLECTION_REACTIONS = "reactions"
    }

    /**
     * Publishes a live class to Firestore so all connected devices can immediately discover it.
     */
    suspend fun publishLiveClass(session: LiveClassSession) {
        val fs = firestore ?: return
        try {
            val data = hashMapOf(
                "id" to session.id,
                "title" to session.title,
                "tutorName" to session.tutorName,
                "tutorId" to session.tutorId,
                "subject" to session.subject,
                "scheduledTime" to session.scheduledTime,
                "durationMinutes" to session.durationMinutes,
                "fee" to session.fee,
                "attendeesCount" to session.attendeesCount,
                "roomCode" to session.roomCode,
                "isLive" to session.isLive,
                "createdAt" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_LIVE_CLASSES).document(session.id).set(data, SetOptions.merge()).await()
            Log.i(TAG, "Published live class: ${session.title} (${session.roomCode})")
        } catch (e: Exception) {
            Log.w(TAG, "Error publishing live class: ${e.message}")
        }
    }

    /**
     * Real-time stream of all active live classes synced across devices.
     */
    fun observeLiveClasses(): Flow<List<LiveClassSession>> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = fs.collection(COLLECTION_LIVE_CLASSES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen to live classes failed", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val classes = snapshot.documents.mapNotNull { doc ->
                            try {
                                LiveClassSession(
                                    id = doc.getString("id") ?: doc.id,
                                    title = doc.getString("title") ?: "Live Class",
                                    tutorName = doc.getString("tutorName") ?: "Certified Tutor",
                                    tutorId = doc.getString("tutorId") ?: "",
                                    subject = doc.getString("subject") ?: "General",
                                    scheduledTime = doc.getString("scheduledTime") ?: "Live Now",
                                    durationMinutes = (doc.getLong("durationMinutes") ?: 60L).toInt(),
                                    fee = doc.getDouble("fee") ?: 2500.0,
                                    attendeesCount = (doc.getLong("attendeesCount") ?: 1L).toInt(),
                                    roomCode = doc.getString("roomCode") ?: "slaty-live",
                                    isLive = doc.getBoolean("isLive") ?: true
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(classes)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach live classes listener: ${e.message}")
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Registers a participant joining the live room.
     */
    suspend fun joinRoom(roomCode: String, user: User, isHost: Boolean) {
        val fs = firestore ?: return
        try {
            val participantData = hashMapOf(
                "userId" to user.id,
                "name" to user.name,
                "role" to user.role.name,
                "isHost" to isHost,
                "isHandRaised" to false,
                "joinedAt" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_LIVE_ROOMS)
                .document(roomCode)
                .collection(SUBCOLLECTION_PARTICIPANTS)
                .document(user.id)
                .set(participantData, SetOptions.merge())
                .await()
            Log.i(TAG, "User ${user.name} joined live room $roomCode")
        } catch (e: Exception) {
            Log.w(TAG, "Error registering room participant: ${e.message}")
        }
    }

    /**
     * Unregisters a participant leaving the live room.
     */
    suspend fun leaveRoom(roomCode: String, userId: String) {
        val fs = firestore ?: return
        try {
            fs.collection(COLLECTION_LIVE_ROOMS)
                .document(roomCode)
                .collection(SUBCOLLECTION_PARTICIPANTS)
                .document(userId)
                .delete()
                .await()
            Log.i(TAG, "User $userId left live room $roomCode")
        } catch (e: Exception) {
            Log.w(TAG, "Error removing room participant: ${e.message}")
        }
    }

    /**
     * Real-time stream of all participants inside this specific live room.
     */
    fun observeRoomParticipants(roomCode: String): Flow<List<LiveRoomParticipant>> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = fs.collection(COLLECTION_LIVE_ROOMS)
                .document(roomCode)
                .collection(SUBCOLLECTION_PARTICIPANTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen to room participants failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val participants = snapshot.documents.mapNotNull { doc ->
                            try {
                                val roleStr = doc.getString("role") ?: "STUDENT"
                                val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT }
                                LiveRoomParticipant(
                                    userId = doc.getString("userId") ?: doc.id,
                                    name = doc.getString("name") ?: "Participant",
                                    role = role,
                                    isHost = doc.getBoolean("isHost") ?: false,
                                    isHandRaised = doc.getBoolean("isHandRaised") ?: false,
                                    joinedAt = doc.getLong("joinedAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(participants)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach participants listener: ${e.message}")
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Toggles a student's hand-raise status so all participants and the tutor see it live.
     */
    suspend fun setHandRaised(roomCode: String, userId: String, isRaised: Boolean) {
        val fs = firestore ?: return
        try {
            fs.collection(COLLECTION_LIVE_ROOMS)
                .document(roomCode)
                .collection(SUBCOLLECTION_PARTICIPANTS)
                .document(userId)
                .update("isHandRaised", isRaised)
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error updating hand raise status: ${e.message}")
        }
    }

    /**
     * Real-time stream of in-class Q&A and chat messages.
     */
    fun observeRoomMessages(roomCode: String): Flow<List<LiveClassMessage>> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = fs.collection(COLLECTION_LIVE_ROOMS)
                .document(roomCode)
                .collection(SUBCOLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen to room messages failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            try {
                                val roleStr = doc.getString("senderRole") ?: "STUDENT"
                                val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT }
                                LiveClassMessage(
                                    id = doc.getString("id") ?: doc.id,
                                    senderId = doc.getString("senderId") ?: "",
                                    senderName = doc.getString("senderName") ?: "Participant",
                                    senderRole = role,
                                    text = doc.getString("text") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    isQuestion = doc.getBoolean("isQuestion") ?: false
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(messages)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach messages listener: ${e.message}")
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Sends an in-class chat message or question live to all devices in the room.
     */
    suspend fun sendRoomMessage(roomCode: String, message: LiveClassMessage) {
        val fs = firestore ?: return
        try {
            val docRef = if (message.id.isBlank()) {
                fs.collection(COLLECTION_LIVE_ROOMS).document(roomCode).collection(SUBCOLLECTION_MESSAGES).document()
            } else {
                fs.collection(COLLECTION_LIVE_ROOMS).document(roomCode).collection(SUBCOLLECTION_MESSAGES).document(message.id)
            }
            val data = hashMapOf(
                "id" to docRef.id,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "senderRole" to message.senderRole.name,
                "text" to message.text,
                "timestamp" to message.timestamp,
                "isQuestion" to message.isQuestion
            )
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error sending room message: ${e.message}")
        }
    }

    /**
     * Real-time stream of the shared lecture notes / whiteboard equations for this class.
     */
    fun observeSharedNotes(roomCode: String): Flow<String> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            trySend("")
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = fs.collection(COLLECTION_LIVE_ROOMS)
                .document(roomCode)
                .collection(SUBCOLLECTION_SHARED)
                .document("notes")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen to shared notes failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    val content = snapshot?.getString("content") ?: ""
                    trySend(content)
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach notes listener: ${e.message}")
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Updates shared lecture notes or equations for all students and tutors live.
     */
    suspend fun updateSharedNotes(roomCode: String, content: String) {
        val fs = firestore ?: return
        try {
            val data = hashMapOf(
                "content" to content,
                "updatedAt" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_LIVE_ROOMS)
                .document(roomCode)
                .collection(SUBCOLLECTION_SHARED)
                .document("notes")
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error updating shared notes: ${e.message}")
        }
    }

    /**
     * Broadcasts an instant emoji reaction (👍, 💡, 👏, ❤️) to all devices in the room.
     */
    suspend fun sendReaction(roomCode: String, emoji: String, senderName: String) {
        val fs = firestore ?: return
        try {
            val data = hashMapOf(
                "emoji" to emoji,
                "senderName" to senderName,
                "timestamp" to System.currentTimeMillis()
            )
            fs.collection(COLLECTION_LIVE_ROOMS)
                .document(roomCode)
                .collection(SUBCOLLECTION_REACTIONS)
                .document()
                .set(data)
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error broadcasting reaction: ${e.message}")
        }
    }

    /**
     * Listens for recent emoji reactions for floating animations.
     */
    fun observeReactions(roomCode: String): Flow<Pair<String, String>> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        val startTime = System.currentTimeMillis() - 5000L
        try {
            listener = fs.collection(COLLECTION_LIVE_ROOMS)
                .document(roomCode)
                .collection(SUBCOLLECTION_REACTIONS)
                .whereGreaterThan("timestamp", startTime)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val doc = snapshot?.documents?.firstOrNull() ?: return@addSnapshotListener
                    val emoji = doc.getString("emoji") ?: ""
                    val sender = doc.getString("senderName") ?: ""
                    if (emoji.isNotBlank()) {
                        trySend(Pair(emoji, sender))
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to listen for reactions: ${e.message}")
        }

        awaitClose {
            listener?.remove()
        }
    }
}
