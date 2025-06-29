package li.songe.gkd.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.destinations.SubsAppGroupListPageDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import li.songe.gkd.data.RawSubscription
import li.songe.gkd.db.DbSet
<<<<<<< HEAD
import li.songe.gkd.ui.component.RuleGroupExtVm
=======
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
import li.songe.gkd.ui.component.ShowGroupState
import li.songe.gkd.util.map
import li.songe.gkd.util.subsIdToRawFlow

<<<<<<< HEAD
class SubsAppGroupListVm(stateHandle: SavedStateHandle) : ViewModel(), RuleGroupExtVm {
=======
class SubsAppGroupListVm(stateHandle: SavedStateHandle) : ViewModel() {
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
    private val args = SubsAppGroupListPageDestination.argsFrom(stateHandle)

    val subsRawFlow = subsIdToRawFlow.map(viewModelScope) { s -> s[args.subsItemId] }

    val subsConfigsFlow = DbSet.subsConfigDao.queryAppGroupTypeConfig(args.subsItemId, args.appId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categoryConfigsFlow = DbSet.categoryConfigDao.queryConfig(args.subsItemId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val subsAppFlow = subsIdToRawFlow.map(viewModelScope) { subsIdToRaw ->
        subsIdToRaw[args.subsItemId]?.apps?.find { it.id == args.appId }
            ?: RawSubscription.RawApp(id = args.appId, name = null)
    }

    val isSelectedModeFlow = MutableStateFlow(false)
    val selectedDataSetFlow = MutableStateFlow(emptySet<ShowGroupState>())

<<<<<<< HEAD
    override val focusGroupKeyFlow = MutableStateFlow<Int?>(args.focusGroupKey)
=======
    val focusGroupFlow = args.focusGroupKey?.let {
        MutableStateFlow<Triple<Long, String?, Int>?>(
            Triple(
                args.subsItemId,
                args.appId,
                args.focusGroupKey
            )
        )
    }
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
}