package li.songe.gkd.data

<<<<<<< HEAD
<<<<<<< HEAD
=======
import android.content.pm.PackageManager
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
=======
import android.content.pm.PackageManager
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
import kotlinx.serialization.Serializable
import li.songe.gkd.app

@Serializable
data class ComplexSnapshot(
    override val id: Long,

    override val appId: String?,
    override val activityId: String?,

    override val screenHeight: Int,
    override val screenWidth: Int,
    override val isLandscape: Boolean,

<<<<<<< HEAD
<<<<<<< HEAD
    val appInfo: AppInfo? = appId?.let { app.packageManager.getPackageInfo(appId, 0)?.toAppInfo() },
=======
=======
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
    val appInfo: AppInfo? = appId?.let {
        app.packageManager.getPackageInfo(
            appId,
            PackageManager.MATCH_UNINSTALLED_PACKAGES
        )?.toAppInfo()
    },
<<<<<<< HEAD
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
=======
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
    val gkdAppInfo: AppInfo? = selfAppInfo,
    val device: DeviceInfo = DeviceInfo.instance,

    @Deprecated("use appInfo")
    override val appName: String? = appInfo?.name,
    @Deprecated("use appInfo")
    override val appVersionCode: Long? = appInfo?.versionCode,
    @Deprecated("use appInfo")
    override val appVersionName: String? = appInfo?.versionName,

    val nodes: List<NodeInfo>,
) : BaseSnapshot

fun ComplexSnapshot.toSnapshot(): Snapshot {
    return Snapshot(
        id = id,

        appId = appId,
        activityId = activityId,

        screenHeight = screenHeight,
        screenWidth = screenWidth,
        isLandscape = isLandscape,

        appName = appInfo?.name,
        appVersionCode = appInfo?.versionCode,
        appVersionName = appInfo?.versionName,
    )
}


