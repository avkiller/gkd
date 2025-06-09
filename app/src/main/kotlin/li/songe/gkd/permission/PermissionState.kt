package li.songe.gkd.permission

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.ramcosta.composedestinations.generated.destinations.AppOpsAllowPageDestination
import com.ramcosta.composedestinations.utils.toDestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import li.songe.gkd.MainActivity
import li.songe.gkd.app
import li.songe.gkd.appOpsManager
import li.songe.gkd.appScope
import li.songe.gkd.shizuku.shizukuCheckGranted
import li.songe.gkd.ui.local.LocalNavController
import li.songe.gkd.util.forceUpdateAppList
import li.songe.gkd.util.initOrResetAppInfoCache
import li.songe.gkd.util.launchTry
import li.songe.gkd.util.mayQueryPkgNoAccessFlow
import li.songe.gkd.util.toast
import li.songe.gkd.util.updateAppMutex

class PermissionState(
    val check: () -> Boolean,
    val request: (suspend (context: MainActivity) -> PermissionResult)? = null,
    /**
     * show it when user doNotAskAgain
     */
    val reason: AuthReason? = null,
) {
    val stateFlow = MutableStateFlow(false)
    fun updateAndGet(): Boolean {
        return stateFlow.updateAndGet { check() }
    }

    fun checkOrToast(): Boolean {
        updateAndGet()
        if (!stateFlow.value) {
            reason?.text?.let { toast(it) }
        }
        return stateFlow.value
    }
}

private fun checkSelfPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        app,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

private sealed class XXPermissionResult {
    data class Granted(
        val permissions: MutableList<String>,
        val allGranted: Boolean,
    ) : XXPermissionResult()

    data class Denied(
        val permissions: MutableList<String>,
        val doNotAskAgain: Boolean,
    ) : XXPermissionResult()

    data class Both(
        val granted: Granted,
        val denied: Denied,
    ) : XXPermissionResult()
}

private suspend fun asyncRequestPermission(
    context: Activity,
    vararg permissions: String,
): PermissionResult {
    if (XXPermissions.isGrantedPermissions(context, permissions)) {
        return PermissionResult.Granted
    }

    val permissionResultFlow = MutableStateFlow<XXPermissionResult?>(null)
    XXPermissions.with(context)
        .unchecked()
        .permission(permissions)
        .request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                LogUtils.d("allGranted: $allGranted", permissions)
                permissionResultFlow.update {
                    val granted = XXPermissionResult.Granted(permissions, allGranted)
                    if (it == null) {
                        granted
                    } else {
                        XXPermissionResult.Both(
                            granted = granted,
                            denied = it as XXPermissionResult.Denied
                        )
                    }
                }
            }

            override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                LogUtils.d("doNotAskAgain: $doNotAskAgain", permissions)
                permissionResultFlow.update {
                    val denied = XXPermissionResult.Denied(permissions, doNotAskAgain)
                    if (it == null) {
                        denied
                    } else {
                        XXPermissionResult.Both(
                            granted = it as XXPermissionResult.Granted,
                            denied = denied
                        )
                    }
                }
            }
        })
    val result = permissionResultFlow.debounce(100L).filterNotNull().first()
    return when (result) {
        is XXPermissionResult.Granted -> {
            if (result.allGranted) {
                PermissionResult.Granted
            } else {
                PermissionResult.Denied(false)
            }
        }

        is XXPermissionResult.Denied -> {
            PermissionResult.Denied(result.doNotAskAgain)
        }

        is XXPermissionResult.Both -> {
            PermissionResult.Denied(result.denied.doNotAskAgain)
        }
    }
}

@Suppress("SameParameterValue")
private fun checkOpNoThrow(op: String): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            return appOpsManager.checkOpNoThrow(
                op,
                android.os.Process.myUid(),
                app.packageName
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    return AppOpsManager.MODE_ALLOWED
}

// https://github.com/gkd-kit/gkd/issues/954
// https://github.com/gkd-kit/gkd/issues/887
val foregroundServiceSpecialUseState by lazy {
    PermissionState(
        check = {
            checkOpNoThrow("android:foreground_service_special_use") != AppOpsManager.MODE_IGNORED
        },
        reason = AuthReason(
            text = "当前操作权限「特殊用途的前台服务」已被限制, 请先解除限制",
            renderConfirm = {
                val navController = LocalNavController.current
                {
                    navController.toDestinationsNavigator().navigate(AppOpsAllowPageDestination)
                }
            }
        ),
    )
}

val notificationState by lazy {
    val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        arrayOf(
            Permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE
        )
    } else {
        arrayOf(Permission.POST_NOTIFICATIONS)
    }
    PermissionState(
        check = {
            XXPermissions.isGrantedPermissions(app, list)
        },
        request = { asyncRequestPermission(it, *list) },
        reason = AuthReason(
            text = "当前操作需要「通知权限」\n您需要前往应用权限设置打开此权限",
            confirm = {
                XXPermissions.startPermissionActivity(app, Permission.POST_NOTIFICATIONS)
            }
        ),
    )
}

val canQueryPkgState by lazy {
    PermissionState(
        check = {
            XXPermissions.isGrantedPermissions(app, Permission.GET_INSTALLED_APPS)
        },
        request = {
            asyncRequestPermission(it, Permission.GET_INSTALLED_APPS)
        },
        reason = AuthReason(
            text = "当前操作需要「读取应用列表权限」\n您需要前往应用权限设置打开此权限",
            confirm = {
                XXPermissions.startPermissionActivity(app, Permission.GET_INSTALLED_APPS)
            }
        ),
    )
}

val canDrawOverlaysState by lazy {
    PermissionState(
        check = {
            // 需要注意, 即使有悬浮权限, 在某些特殊的页面如 微信支付完毕 页面, 下面的方法会返回 false
            Settings.canDrawOverlays(app)
        },
        reason = AuthReason(
            text = "当前操作需要「悬浮窗权限」\n您需要前往应用权限设置打开此权限",
            confirm = {
                XXPermissions.startPermissionActivity(app, Manifest.permission.SYSTEM_ALERT_WINDOW)
            }
        ),
    )
}

val canWriteExternalStorage by lazy {
    PermissionState(
        check = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                true
            }
        },
        request = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                asyncRequestPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                PermissionResult.Granted
            }
        },
        reason = AuthReason(
            text = "当前操作需要「写入外部存储权限」\n您需要前往应用权限设置打开此权限",
            confirm = {
                XXPermissions.startPermissionActivity(
                    app,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        ),
    )
}

val writeSecureSettingsState by lazy {
    PermissionState(
        check = { checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) },
    )
}

val shizukuOkState by lazy {
    PermissionState(
        check = { shizukuCheckGranted() },
    )
}

fun startQueryPkgSettingActivity(context: Activity) {
    XXPermissions.startPermissionActivity(context, Permission.GET_INSTALLED_APPS)
}

fun updatePermissionState() {
    val stateChanged = canQueryPkgState.stateFlow.value != canQueryPkgState.updateAndGet()
    if (!updateAppMutex.mutex.isLocked && (stateChanged || mayQueryPkgNoAccessFlow.value)) {
        appScope.launchTry(Dispatchers.IO) {
            initOrResetAppInfoCache()
        }
    } else {
        forceUpdateAppList()
    }
    arrayOf(
        notificationState,
        foregroundServiceSpecialUseState,
        canDrawOverlaysState,
        canWriteExternalStorage,
        writeSecureSettingsState,
        shizukuOkState,
    ).forEach { it.updateAndGet() }
}