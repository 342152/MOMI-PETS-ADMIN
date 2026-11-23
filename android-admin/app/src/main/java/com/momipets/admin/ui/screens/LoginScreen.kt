
package com.momipets.admin.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.momipets.admin.data.FirebaseAdminRepository
import com.momipets.admin.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(onAuthenticated: () -> Unit) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        // Do not immediately discard cancelled results.
        // Google may return useful error information in the Intent.

        val signInTask =
            GoogleSignIn.getSignedInAccountFromIntent(result.data)

        scope.launch {
            try {

                // 1. Get Google account
                val account = try {
                    signInTask.getResult(ApiException::class.java)
                } catch (e: ApiException) {

                    val statusText =
                        GoogleSignInStatusCodes.getStatusCodeString(
                            e.statusCode
                        )

                    throw Exception(
                        "Google Sign-In Failed\n" +
                        "Status Code: ${e.statusCode}\n" +
                        "Status: $statusText\n" +
                        "Result Code: ${result.resultCode}"
                    )
                }

                // 2. Check ID Token
                val idToken = account.idToken
                    ?: throw Exception(
                        "Google ID Token is NULL.\n" +
                        "Check Firebase Web Client ID and SHA-1."
                    )

                // 3. Create Firebase credential
                val credential =
                    GoogleAuthProvider.getCredential(
                        idToken,
                        null
                    )

                // 4. Firebase authentication
                val authResult =
                    FirebaseAdminRepository.auth
                        .signInWithCredential(credential)
                        .await()

                val user = authResult.user
                    ?: throw Exception(
                        "Firebase authentication returned no user."
                    )

                // 5. Verify owner access
                val authorized =
                    FirebaseAdminRepository.verifyOwnerAccess(user)

                if (authorized) {

                    errorMessage = null
                    onAuthenticated()

                } else {

                    FirebaseAdminRepository.signOut()

                    errorMessage =
                        "ACCESS DENIED\n\n" +
                        "Google Account: ${user.email}\n\n" +
                        "This account is not authorized as MOMI PETS owner."
                }

            } catch (e: Exception) {

                errorMessage =
                    "LOGIN FAILED\n\n" +
                    "${e.javaClass.simpleName}\n\n" +
                    "${e.localizedMessage ?: "Unknown authentication error"}"

            } finally {

                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamCanvas)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CrispWhite
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Surface(
                    color = ForestTint,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(60.dp)
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = "MOMI PETS ADMIN",
                            tint = ForestDark,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Text(
                    text = "MOMI PETS ADMIN",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestDark
                )

                Text(
                    text = "Private owner console for managing products, suppliers, orders, and realized profits in Pakistan.",
                    fontSize = 14.sp,
                    color = InkMuted,
                    textAlign = TextAlign.Center
                )

                // Error message
                if (errorMessage != null) {

                    Surface(
                        color = AlertRed.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = errorMessage!!,
                            color = AlertRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Google Sign-In button
                Button(
                    onClick = {

                        isLoading = true
                        errorMessage = null

                        try {

                            val webClientIdRes =
                                context.resources.getIdentifier(
                                    "default_web_client_id",
                                    "string",
                                    context.packageName
                                )

                            if (webClientIdRes == 0) {

                                isLoading = false

                                errorMessage =
                                    "MISSING WEB CLIENT ID\n\n" +
                                    "Check google-services.json in the app folder."

                                return@Button
                            }

                            val webClientId =
                                context.getString(webClientIdRes)

                            if (webClientId.isBlank()) {

                                isLoading = false

                                errorMessage =
                                    "Firebase Web Client ID is empty."

                                return@Button
                            }

                            val gso =
                                GoogleSignInOptions.Builder(
                                    GoogleSignInOptions.DEFAULT_SIGN_IN
                                )
                                    .requestIdToken(webClientId)
                                    .requestEmail()
                                    .build()

                            val googleClient =
                                GoogleSignIn.getClient(
                                    context,
                                    gso
                                )

                            launcher.launch(
                                googleClient.signInIntent
                            )

                        } catch (e: Exception) {

                            isLoading = false

                            errorMessage =
                                "SIGN-IN START ERROR\n\n" +
                                "${e.localizedMessage ?: e.javaClass.simpleName}"
                        }
                    },

                    enabled = !isLoading,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),

                    shape = RoundedCornerShape(10.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestDark
                    )
                ) {

                    if (isLoading) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CreamCanvas,
                            strokeWidth = 2.dp
                        )

                    } else {

                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(
                            Modifier.width(8.dp)
                        )

                        Text(
                            "Sign In as Authorized Owner"
                        )
                    }
                }
            }
        }
    }
}
