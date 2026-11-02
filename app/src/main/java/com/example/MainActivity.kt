package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.OmniCalcTheme
import com.example.ui.theme.RetroTerminalProfile
import com.example.ui.theme.Vt323Family
import com.example.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            val viewModel: CalculatorViewModel = viewModel()
            val historyList by viewModel.historyItems.collectAsStateWithLifecycle()

            var currentProfile by remember { mutableStateOf(RetroTerminalProfile.GREEN_PHOSPHOR) }
            var selectedNavIndex by remember { mutableIntStateOf(0) }
            var showHistorySheet by remember { mutableStateOf(false) }
            var showProfileMenu by remember { mutableStateOf(false) }
            var restoredExpression by remember { mutableStateOf<String?>(null) }

            OmniCalcTheme(profile = currentProfile) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        text = "OMNI://",
                                        fontFamily = Vt323Family,
                                        fontSize = 28.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Calc_Zer0",
                                        fontFamily = Vt323Family,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "Room_0",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            actions = {
                                // Profile Switcher (Green P1, Amber P3, Cyan VT100)
                                Box {
                                    TextButton(
                                        onClick = { showProfileMenu = true },
                                        shape = RoundedCornerShape(2.dp),
                                        modifier = Modifier.testTag("terminal_profile_btn")
                                    ) {
                                        Text(
                                            text = "[${currentProfile.name.take(5)}]",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showProfileMenu,
                                        onDismissRequest = { showProfileMenu = false }
                                    ) {
                                        RetroTerminalProfile.values().forEach { profile ->
                                            DropdownMenuItem(
                                                text = { Text(profile.title) },
                                                onClick = {
                                                    currentProfile = profile
                                                    showProfileMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // History Button
                                IconButton(
                                    onClick = { showHistorySheet = true },
                                    modifier = Modifier.testTag("top_history_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Room History",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        // Retro Terminal Bracketed Navigation Bar (6 Modes)
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                .testTag("main_bottom_nav")
                        ) {
                            val items = listOf(
                                "[01:CALC]",
                                "[02:PROG]",
                                "[03:UNIT]",
                                "[04:TIME]",
                                "[05:PAY]",
                                "[06:FIN]"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                items.forEachIndexed { index, title ->
                                    val isSelected = selectedNavIndex == index
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 2.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .border(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                RoundedCornerShape(2.dp)
                                            )
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                            )
                                            .clickable { selectedNavIndex = index }
                                            .padding(vertical = 8.dp)
                                            .testTag("nav_tab_$index"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(
                            targetState = selectedNavIndex,
                            label = "navTransition"
                        ) { screenIndex ->
                            when (screenIndex) {
                                0 -> CalculatorScreen(
                                    onSaveCalculation = { expr, res ->
                                        viewModel.saveCalculation(expr, res)
                                    },
                                    onOpenHistory = { showHistorySheet = true },
                                    restoredExpression = restoredExpression,
                                    onClearRestoredExpression = { restoredExpression = null }
                                )
                                1 -> ProgrammerCalculatorScreen()
                                2 -> UnitConverterScreen()
                                3 -> TimeCalculatorScreen()
                                4 -> PayCalculatorScreen()
                                5 -> FinanceScreen()
                            }
                        }

                        if (showHistorySheet) {
                            HistoryBottomSheet(
                                historyList = historyList,
                                onSelectExpression = { expr ->
                                    restoredExpression = expr
                                    selectedNavIndex = 0
                                },
                                onSelectResult = { res ->
                                    restoredExpression = res
                                    selectedNavIndex = 0
                                },
                                onDeleteItem = { id -> viewModel.deleteHistory(id) },
                                onClearHistory = { viewModel.clearAllHistory() },
                                onDismiss = { showHistorySheet = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
