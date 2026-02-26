package com.duoschedule.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duoschedule.data.model.PersonType
import com.duoschedule.ui.settings.components.TextInputDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val personAName by viewModel.personAName.collectAsState()
    val personBName by viewModel.personBName.collectAsState()

    var showPersonADialog by remember { mutableStateOf(false) }
    var showPersonBDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 小米风格卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "人员名称设置",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 我的名称
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPersonBDialog = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "我的名称",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "设置自己的显示名称",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = personBName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // Ta的名称
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPersonADialog = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ta的名称",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "设置对方的显示名称",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = personAName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showPersonADialog) {
        TextInputDialog(
            title = "Ta的名称",
            label = "请输入Ta的名称",
            initialValue = personAName,
            onDismiss = { showPersonADialog = false },
            onConfirm = { 
                viewModel.setPersonName(PersonType.PERSON_A, it)
                showPersonADialog = false
            },
            placeholder = "例如：小明"
        )
    }

    if (showPersonBDialog) {
        TextInputDialog(
            title = "我的名称",
            label = "请输入我的名称",
            initialValue = personBName,
            onDismiss = { showPersonBDialog = false },
            onConfirm = { 
                viewModel.setPersonName(PersonType.PERSON_B, it)
                showPersonBDialog = false
            },
            placeholder = "例如：小红"
        )
    }
}
