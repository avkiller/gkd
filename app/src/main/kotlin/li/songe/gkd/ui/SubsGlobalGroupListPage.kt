package li.songe.gkd.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.UpsertRuleGroupPageDestination
import kotlinx.coroutines.Dispatchers
import li.songe.gkd.db.DbSet
import li.songe.gkd.ui.component.AnimationFloatingActionButton
import li.songe.gkd.ui.component.BatchActionButtonGroup
import li.songe.gkd.ui.component.EmptyText
import li.songe.gkd.ui.component.RuleGroupCard
import li.songe.gkd.ui.component.TowLineText
import li.songe.gkd.ui.component.animateListItem
import li.songe.gkd.ui.component.toGroupState
import li.songe.gkd.ui.component.useListScrollState
import li.songe.gkd.ui.component.waitResult
import li.songe.gkd.ui.icon.BackCloseIcon
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.share.LocalNavController
import li.songe.gkd.ui.style.EmptyHeight
import li.songe.gkd.ui.style.ProfileTransitions
import li.songe.gkd.ui.style.scaffoldPadding
import li.songe.gkd.util.LIST_PLACEHOLDER_KEY
import li.songe.gkd.util.getUpDownTransform
import li.songe.gkd.util.launchAsFn
import li.songe.gkd.util.switchItem
import li.songe.gkd.util.throttle
import li.songe.gkd.util.toast
import li.songe.gkd.util.updateSubscription

@Suppress("unused")
@Destination<RootGraph>(style = ProfileTransitions::class)
@Composable
fun SubsGlobalGroupListPage(subsItemId: Long, focusGroupKey: Int? = null) {
    val mainVm = LocalMainViewModel.current
    val navController = LocalNavController.current
    val vm = viewModel<SubsGlobalGroupListVm>()
    val subs = vm.subsRawFlow.collectAsState().value
    val subsConfigs by vm.subsConfigsFlow.collectAsState()

    val editable = subsItemId < 0 && subs != null
    val globalGroups = subs?.globalGroups ?: emptyList()

    val isSelectedMode = vm.isSelectedModeFlow.collectAsState().value
    val selectedDataSet = vm.selectedDataSetFlow.collectAsState().value
    LaunchedEffect(key1 = isSelectedMode) {
        if (!isSelectedMode) {
            vm.selectedDataSetFlow.value = emptySet()
        }
    }
    LaunchedEffect(key1 = selectedDataSet.isEmpty()) {
        if (selectedDataSet.isEmpty()) {
            vm.isSelectedModeFlow.value = false
        }
    }
    BackHandler(isSelectedMode) {
        vm.isSelectedModeFlow.value = false
    }

    val (scrollBehavior, listState) = useListScrollState(globalGroups.isEmpty())
    if (focusGroupKey != null) {
        LaunchedEffect(null) {
            if (vm.focusGroupFlow?.value != null) {
                val i = globalGroups.indexOfFirst { it.key == focusGroupKey }
                if (i >= 0) {
                    listState.scrollToItem(i)
                }
            }
        }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior, navigationIcon = {
                IconButton(onClick = throttle {
                    if (isSelectedMode) {
                        vm.isSelectedModeFlow.value = false
                    } else {
                        navController.popBackStack()
                    }
                }) {
                    BackCloseIcon(backOrClose = !isSelectedMode)
                }
            }, title = {
                if (isSelectedMode) {
                    Text(
                        text = selectedDataSet.size.toString(),
                    )
                } else {
                    TowLineText(
                        title = subs?.name ?: subsItemId.toString(),
                        subtitle = "全局规则"
                    )
                }
            }, actions = {
                var expanded by remember { mutableStateOf(false) }
                AnimatedContent(
                    targetState = isSelectedMode,
                    transitionSpec = { getUpDownTransform() },
                    contentAlignment = Alignment.TopEnd,
                ) {
                    if (it) {
                        Row {
                            BatchActionButtonGroup(vm, selectedDataSet)
                            if (editable) {
                                IconButton(
                                    onClick = throttle(
                                        vm.viewModelScope.launchAsFn(
                                            Dispatchers.Default
                                        ) {
                                            subs
                                            mainVm.dialogFlow.waitResult(
                                                title = "删除规则组",
                                                text = "删除当前所选规则组?",
                                                error = true,
                                            )
                                            val keys = selectedDataSet.mapNotNull { it.groupKey }
                                            vm.isSelectedModeFlow.value = false
                                            updateSubscription(
                                                subs.copy(
                                                    globalGroups = globalGroups.filterNot {
                                                        keys.contains(
                                                            it.key
                                                        )
                                                    }
                                                )
                                            )
                                            DbSet.subsConfigDao.batchDeleteGlobalGroupConfig(
                                                subsItemId,
                                                keys
                                            )
                                            toast("删除成功")
                                        })
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                    )
                                }
                            }
                            IconButton(onClick = {
                                expanded = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
                if (isSelectedMode) {
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(text = "全选")
                                },
                                onClick = {
                                    expanded = false
                                    vm.selectedDataSetFlow.value = globalGroups.map {
                                        it.toGroupState(
                                            subsId = subsItemId,
                                        )
                                    }.toSet()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(text = "反选")
                                },
                                onClick = {
                                    expanded = false
                                    val newSelectedIds = globalGroups.map {
                                        it.toGroupState(
                                            subsId = subsItemId,
                                        )
                                    }.toSet() - selectedDataSet
                                    vm.selectedDataSetFlow.value = newSelectedIds
                                }
                            )
                        }
                    }
                }
            })
        },
        floatingActionButton = {
            if (editable) {
                AnimationFloatingActionButton(visible = !isSelectedMode, onClick = throttle {
                    mainVm.navigatePage(
                        UpsertRuleGroupPageDestination(
                            subsId = subsItemId,
                            groupKey = null,
                            appId = null,
                        )
                    )
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "add",
                    )
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.scaffoldPadding(paddingValues),
            state = listState,
        ) {
            items(globalGroups, { g -> g.key }) { group ->
                val subsConfig = subsConfigs.find { it.groupKey == group.key }
                RuleGroupCard(
                    modifier = Modifier.animateListItem(this),
                    subs = subs!!,
                    appId = null,
                    group = group,
                    focusGroupFlow = vm.focusGroupFlow,
                    subsConfig = subsConfig,
                    category = null,
                    categoryConfig = null,
                    showBottom = group !== globalGroups.last(),
                    isSelectedMode = isSelectedMode,
                    isSelected = selectedDataSet.any { it.groupKey == group.key },
                    onLongClick = {
                        if (globalGroups.size > 1) {
                            vm.isSelectedModeFlow.value = true
                            vm.selectedDataSetFlow.value = setOf(
                                group.toGroupState(subsId = subsItemId)
                            )
                        }
                    },
                    onSelectedChange = {
                        vm.selectedDataSetFlow.value = selectedDataSet.switchItem(
                            group.toGroupState(subsId = subsItemId)
                        )
                    }
                )
            }
            item(LIST_PLACEHOLDER_KEY) {
                Spacer(modifier = Modifier.height(EmptyHeight))
                if (globalGroups.isEmpty()) {
                    EmptyText(text = "暂无规则")
                } else if (editable) {
                    Spacer(modifier = Modifier.height(EmptyHeight))
                }
            }
        }
    }
}
