package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.auth.attemptAutoSignIn
import com.example.data.auth.onGoogleSignInClicked
import com.example.ui.components.CelestialBackdrop
import com.example.ui.components.ConstellationDivider
import com.example.ui.components.LunarCard
import com.example.ui.components.LunarChip
import com.example.ui.components.LunarMoonCrest
import com.example.ui.components.LunarPrimaryButton
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LunarTextStyles

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(
    onAuthSuccess: () -> Unit
) {
    val lunar = LocalLunarColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isCheckingSession by remember { mutableStateOf(true) }
    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isCheckingSession = false
                onAuthSuccess()
            },
            onUnauthenticated = {
                isCheckingSession = false
            },
            scope = scope
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CelestialBackdrop(showCastleSilhouette = true)

        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // Hero royal-beige crescent moon with champagne-gold moonlight glow
            LunarMoonCrest(
                size = 132.dp,
                animateBounce = true,
                contentDescription = "Lunar Castle hero crescent moon"
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "LUNAR CASTLE",
                style = LunarTextStyles.StatusMono,
                color = if (lunar.isDark) lunar.gold else lunar.deepLavender
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Lunar Castle",
                style = MaterialTheme.typography.displayLarge,
                color = lunar.primaryText,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Where quiet study blooms under moonlight and steeped tea.",
                style = LunarTextStyles.TaglineItalic,
                color = lunar.secondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LunarChip(label = "Moon Room Focus", selected = true, onClick = {})
                LunarChip(label = "Study Tables", selected = true, accentGold = true, onClick = {})
                LunarChip(label = "The Secret Garden", selected = true, onClick = {})
            }

            Spacer(Modifier.height(24.dp))
            ConstellationDivider()
            Spacer(Modifier.height(20.dp))

            LunarCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(22.dp)
            ) {
                Text(
                    text = "Cross-Device Cloud Archive",
                    style = MaterialTheme.typography.titleLarge,
                    color = lunar.primaryText
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Sign in with Google to keep your Moon Room sessions, study table milestones, reading log, and Secret Garden notes synced privately across devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = lunar.secondaryText
                )

                AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                if (isCheckingSession || isSigningIn) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = lunar.gold,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = if (isCheckingSession) "Opening castle gate..." else "Signing in with Google...",
                            style = MaterialTheme.typography.labelLarge,
                            color = lunar.secondaryText
                        )
                    }
                } else {
                    LunarPrimaryButton(
                        text = "Sign in with Google",
                        onClick = {
                            errorMessage = null
                            isSigningIn = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isSigningIn = false
                                    onAuthSuccess()
                                },
                                onAuthError = { msg ->
                                    isSigningIn = false
                                    errorMessage = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isSigningIn = false
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_sign_in_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LockOpen,
                                contentDescription = "Sign in with Google",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = if (lunar.isDark) lunar.gold else lunar.deepLavender,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "PRIVATE PER-USER CLOUD SYNC",
                        style = LunarTextStyles.StatusMono,
                        color = lunar.mutedMetaText
                    )
                }
            }
        }
    }
}
