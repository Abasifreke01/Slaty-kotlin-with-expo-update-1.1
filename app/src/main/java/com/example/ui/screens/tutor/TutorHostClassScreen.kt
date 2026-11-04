package com.example.ui.screens.tutor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.student.defaultTextFieldColors
import com.example.ui.theme.*

@Composable
fun TutorHostClassScreen(
    onStartClass: (title: String, fee: Double, subject: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var className by remember { mutableStateOf("Calculus & Analytical Geometry") }
    var amountText by remember { mutableStateOf("3000") }
    var subject by remember { mutableStateOf("Mathematics") }
    var generatedLink by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Host a Live Class",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Create an instant HD interactive video classroom. Share your link with students to teach and earn.",
            color = SlatyTextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF1F1C2F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class / Topic Name") },
                    modifier = Modifier.fillMaxWidth().testTag("host_class_name_input"),
                    colors = defaultTextFieldColors()
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g. Mathematics, Physics)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Ticket Fee per Student (₦)") },
                    modifier = Modifier.fillMaxWidth().testTag("host_class_fee_input"),
                    colors = defaultTextFieldColors()
                )

                // Generate Link Button
                Button(
                    onClick = {
                        val slug = className.trim().lowercase().replace("\\s+".toRegex(), "-")
                        generatedLink = "https://meet.jit.si/slaty-$slug"
                        Toast.makeText(context, "Class link generated!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2945)),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_link_button")
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = SlatyMint)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Shareable Link", color = SlatyMint, fontWeight = FontWeight.Bold)
                }

                // Start Class (Tutor)
                Button(
                    onClick = {
                        val fee = amountText.toDoubleOrNull() ?: 2500.0
                        onStartClass(className.trim(), fee, subject.trim())
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("start_class_tutor_button")
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Live Class (Tutor)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // Generated link view
        if (generatedLink != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF151320),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlatyMint),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Your Shareable Class Link:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = generatedLink ?: "",
                        color = SlatyMint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Class Link", generatedLink)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Link", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Ready to share with students!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
