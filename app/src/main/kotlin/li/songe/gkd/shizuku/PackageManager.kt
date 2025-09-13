package li.songe.gkd.shizuku

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.IPackageManager
import android.content.pm.PackageInfo
import li.songe.gkd.util.checkExistClass
import kotlin.reflect.typeOf


private var pkgFcType: Int? = null
private fun IPackageManager.compatGetInstalledPackages(
    flags: Int,
    userId: Int
): List<PackageInfo> {
    pkgFcType = pkgFcType ?: findCompatMethod(
        "getInstalledPackages",
        listOf(
            1 to listOf(typeOf<Int>(), typeOf<Int>()),
            2 to listOf(typeOf<Long>(), typeOf<Int>()),
        )
    )
    return when (pkgFcType) {
        1 -> getInstalledPackages(flags, userId).list
        2 -> getInstalledPackages(flags.toLong(), userId).list
        else -> emptyList()
    }
}

class SafePackageManager(private val value: IPackageManager) {
    companion object {
        val isAvailable: Boolean
            get() = checkExistClass("android.content.pm.IPackageManager")

        fun newBinder() = getStubService(
            "package",
            isAvailable
        )?.let {
            SafePackageManager(IPackageManager.Stub.asInterface(it))
        }
    }

    fun getInstalledPackages(flags: Int, userId: Int): List<PackageInfo> {
        return safeInvokeMethod { value.compatGetInstalledPackages(flags, userId) } ?: emptyList()
    }

    fun getAllIntentFilters(packageName: String): List<IntentFilter> {
        return safeInvokeMethod { value.getAllIntentFilters(packageName).list } ?: emptyList()
    }

    fun checkAppHidden(appId: String): Boolean {
        return !getAllIntentFilters(appId).any { f ->
            f.hasAction(Intent.ACTION_MAIN) && f.hasCategory(
                Intent.CATEGORY_LAUNCHER
            )
        }
    }
}
