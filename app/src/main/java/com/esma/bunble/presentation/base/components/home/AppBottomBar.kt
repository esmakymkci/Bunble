package com.esma.bunble.presentation.base.components.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandWhite
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.GrayText
import com.esma.bunble.R

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AppBottomBar(navController: NavController) {
    val navItems = listOf(
        BottomNavItem(stringResource(id = R.string.bottom_nav_home), Icons.Default.Home, "home_screen"),
        BottomNavItem(stringResource(id = R.string.bottom_nav_stories), Icons.Default.HistoryEdu, "stories_screen"),
        BottomNavItem(stringResource(id = R.string.bottom_nav_lists), Icons.Default.Checklist, "lists_screen"),
        BottomNavItem(stringResource(id = R.string.bottom_nav_quiz), Icons.Default.Quiz, "quiz_screen"),
        BottomNavItem(stringResource(id = R.string.bottom_nav_chat), Icons.Default.Chat, "chat_screen"),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = Modifier,
        containerColor = BrandWhite,
        contentColor = GrayText
    ) {
        navItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandYellow,
                    selectedTextColor = BrandBlack,
                    unselectedIconColor = GrayText,
                    unselectedTextColor = GrayText,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

/*

currentBackStackEntryAsState():
NavController içindeki mevcut backstack entry’yi bir State olarak döner.
Yani route değiştiğinde Compose bunu fark eder ve AppBottomBar yeniden çizilir.


currentDestination?.hierarchy:

Bazen nested nav graph kullanıyorsun (örneğin bottom bar + inner graph).
hierarchy → şu anki destination’ın ait olduğu tüm graph/destination zincirini verir.
.any { it.route == item.route }:
O hiyerarşi içinde bu item.route ile eşleşen bir route var mı?
== true: Boolean? yerine düz Boolean elde etmek için:
Eğer null ise false olacak.


navController.navigate(item.route) { ... }
Seçilen item’in route’una gider (home_screen, stories_screen vs.)


popUpTo(navController.graph.findStartDestination().id) { saveState = true }

findStartDestination().id: NavGraph’in başlangıç destination’ını bulur (mesela home_screen).

popUpTo(...): Backstack’i bu başlangıç noktasına kadar temizler.

Böylece: Alt bar itemlarına tıklayıp dolaşırken gereksiz stack birikmez.

saveState = true: Daha önce o destination’a gitmişsen, onun state’ini (scroll pozisyonu vs.) saklar.

launchSingleTop = true Eğer zaten item.route en üstte ise, aynı route’u tekrar üste itmez.

Yani: Profile → tekrar Profile’a tıklarsan yeni bir instance eklemez.   Gereksiz backstack şişmesini engeller.

 */
