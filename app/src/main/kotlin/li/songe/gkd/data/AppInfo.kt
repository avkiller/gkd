package li.songe.gkd.data

<<<<<<< HEAD
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
=======
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
import android.graphics.drawable.Drawable
import android.os.Build
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import li.songe.gkd.app
<<<<<<< HEAD
import java.util.Objects
=======
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52

@Serializable
data class AppInfo(
    val id: String,
    val name: String,
    @Transient
    val icon: Drawable? = null,
    val versionCode: Long,
    val versionName: String?,
    val isSystem: Boolean,
    val mtime: Long,
    val hidden: Boolean,
    val userId: Int? = null, // null -> current user
<<<<<<< HEAD
) {
    // 重写 equals 和 hashCode 便于 compose 重组比较
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is AppInfo) return false
        return (id == other.id && icon == other.icon && mtime == other.mtime && userId == other.userId)
    }

    override fun hashCode(): Int {
        return Objects.hash(id, icon, mtime, userId)
    }
}

val selfAppInfo by lazy {
    app.packageManager.getPackageInfo(app.packageName, 0).toAppInfo()
=======
)

val selfAppInfo by lazy {
    app.packageManager.getPackageInfo(
        app.packageName,
        PackageManager.MATCH_UNINSTALLED_PACKAGES
    ).toAppInfo()
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
}

fun Drawable.safeGet(): Drawable? {
    return if (intrinsicHeight <= 0 || intrinsicWidth <= 0) {
        // https://github.com/gkd-kit/gkd/issues/924
        null
    } else {
        this
    }
}

/**
 * 平均单次调用时间 11ms
 */
fun PackageInfo.toAppInfo(
    userId: Int? = null,
    hidden: Boolean? = null,
): AppInfo {
    return AppInfo(
        id = packageName,
        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode
        } else {
            @Suppress("DEPRECATION")
            versionCode.toLong()
        },
        versionName = versionName,
        mtime = lastUpdateTime,
        isSystem = applicationInfo?.let { it.flags and ApplicationInfo.FLAG_SYSTEM != 0 } ?: false,
        name = applicationInfo?.run { loadLabel(app.packageManager).toString() } ?: packageName,
        icon = applicationInfo?.loadIcon(app.packageManager)?.safeGet(),
        userId = userId,
<<<<<<< HEAD
        hidden = hidden ?: (app.packageManager.getLaunchIntentForPackage(packageName) == null),
=======
        hidden = hidden ?: app.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).setPackage(packageName).addCategory(Intent.CATEGORY_LAUNCHER),
            PackageManager.MATCH_DISABLED_COMPONENTS
        ).isEmpty(),
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
    )
}
