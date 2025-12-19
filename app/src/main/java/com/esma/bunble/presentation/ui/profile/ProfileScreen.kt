package com.esma.bunble.presentation.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.R
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.GrayText
import com.esma.bunble.presentation.viewmodel.profile.ProfileNavigationEvent
import com.esma.bunble.presentation.viewmodel.profile.ProfileState
import com.esma.bunble.presentation.viewmodel.profile.ProfileViewModel
import com.esma.bunble.presentation.viewmodel.profile.Statistic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // NAVİGASYON SİNYALİNİ DİNLE
    LaunchedEffect(key1 = true) { // key1=true efekti bir kere çalıştırır.
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ProfileNavigationEvent.NavigateToSignIn -> {
                    // Tüm geri yığınını temizle ve signin_screen'e git
                    navController.navigate("signin_screen") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9F9F9), // Tasarımdaki hafif gri arka plan
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = BrandBlack) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandBlack)
                    }
                },
                actions = {
                    TextButton(onClick = { /* TODO: Edit profiline git */ }) {
                        Text("Edit", color = BrandYellow, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // Sayfa içeriği sığmazsa kaydırılabilir yapar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                // Veri yüklenirken bir progress bar gösterilebilir
                CircularProgressIndicator()
            }else {
                Spacer(modifier = Modifier.height(24.dp))
                ProfileHeader(state)
                Spacer(modifier = Modifier.height(24.dp))
                LevelProgressCard(state)
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("Statistics")
                Spacer(modifier = Modifier.height(16.dp))
                StatisticsGrid(state)
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("General Settings")
                Spacer(modifier = Modifier.height(16.dp))
                GeneralSettingsCard(
                    state = state,
                    onDarkModeChanged = viewModel::onDarkModeChanged,
                    onNotificationsChanged = viewModel::onNotificationsChanged
                )
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("Account")
                Spacer(modifier = Modifier.height(16.dp))
                AccountSettingsCard(
                    onChangePassword = { navController.navigate("change_password_screen") },
                    onSignOut = viewModel::onSignOutClicked
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(state: ProfileState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.user_avatar),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified",
                tint = Color(0xFF64B5F6),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.White, CircleShape)
                    .size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(state.userName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = state.currentLanguage,
            color = GrayText,
            fontSize = 16.sp
        )
    }
}

@Composable
fun LevelProgressCard(state: ProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("LEVEL  ${state.level}", color = BrandYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Intermediate Scholar", fontWeight = FontWeight.Bold, color = BrandBlack)
                Text("⭐ ${state.currentXp}/${state.totalXp} XP", fontWeight = FontWeight.Bold, color = BrandBlack)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.currentXp.toFloat() / state.totalXp.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = BrandYellow,
                trackColor = BrandYellow.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("250 XP to next level reward! 🎁", fontSize = 12.sp, color = GrayText)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = BrandBlack,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun StatisticsGrid(state: ProfileState) {
    val statistics = state.statistics
    if (statistics.size >= 2) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCardFromData(statistic = statistics[0], modifier = Modifier.weight(1f))
            StatCardFromData(statistic = statistics[1], modifier = Modifier.weight(1f))
        }
    }
    if (statistics.size >= 4) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCardFromData(statistic = statistics[2], modifier = Modifier.weight(1f))
            StatCardFromData(statistic = statistics[3], modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(icon: ImageVector, title: String, value: String, unit: String, color: Color, iconColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = title, tint = iconColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = iconColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
            Text(unit, fontSize = 12.sp, color = GrayText)
        }
    }
}

@Composable
fun StatCardFromData(statistic: Statistic, modifier: Modifier = Modifier) {
    // Renk ve ikonları başlığa göre belirleyebiliriz
    val (icon, color, iconColor) = when (statistic.title) {
        "STREAK" -> Triple(Icons.Default.LocalFireDepartment, Color(0xFFFFF4E0), Color(0xFFFFA726))
        "LEARNED" -> Triple(Icons.AutoMirrored.Filled.MenuBook, Color(0xFFE3F2FD), Color(0xFF42A5F5))
        "TIME" -> Triple(Icons.Default.Timer, Color(0xFFF3E5F5), Color(0xFFAB47BC))
        "DAILY" -> Triple(Icons.Default.CalendarMonth, Color(0xFFE0F7F4), Color(0xFF26A69A))
        else -> Triple(Icons.Default.Help, Color.LightGray, Color.DarkGray)
    }
    StatCard(icon, statistic.title, statistic.value, statistic.unit, color, iconColor, modifier)
}


@Composable
fun GeneralSettingsCard(
    state: ProfileState,
    onDarkModeChanged: (Boolean) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            SettingsRow(
                icon = Icons.Default.NightsStay,
                title = "Dark Mode",
                onClick = { onDarkModeChanged(!state.isDarkMode) },
                trailingContent = {
                    Switch(
                        checked = state.isDarkMode,
                        onCheckedChange = onDarkModeChanged,
                        colors = SwitchDefaults.colors(checkedThumbColor = BrandYellow, uncheckedThumbColor = Color.LightGray)
                    )
                }
            )
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                onClick = { onNotificationsChanged(!state.areNotificationsEnabled) },
                trailingContent = {
                    Switch(
                        checked = state.areNotificationsEnabled,
                        onCheckedChange = onNotificationsChanged,
                        colors = SwitchDefaults.colors(checkedThumbColor = BrandYellow, uncheckedThumbColor = Color.LightGray)
                    )
                }
            )
        }
    }
}

@Composable
fun AccountSettingsCard(
    onChangePassword: () -> Unit,
    onSignOut: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            SettingsRow(icon = Icons.Default.Lock, title = "Change Password", onClick = onChangePassword )
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Sign Out",
                onClick = onSignOut,
                titleColor = Color.Red,
                iconColor = Color.Red
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    titleColor: Color = BrandBlack,
    iconColor: Color = BrandBlack,
    trailingContent: (@Composable () -> Unit)? = { Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = GrayText) }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = iconColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.SemiBold, color = titleColor, modifier = Modifier.weight(1f))
        if (trailingContent != null) {
            trailingContent()
        }
    }
}
