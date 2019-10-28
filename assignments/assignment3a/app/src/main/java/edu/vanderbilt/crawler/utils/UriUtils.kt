package edu.vanderbilt.crawler.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.webkit.URLUtil
import androidx.core.content.FileProvider

import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * A utility class containing methods for creating and manipulating Uri
 * objects.
 */
object UriUtils {
    /**
     * Logging tag.
     */
    private val TAG = "UriUtils"

    /**
     * File provider identifier.
     */
    /**
     * @return Application file provider authority.
     */
    val fileProviderAuthority = "vandy.mooc.assignments.fileprovider"

    /**
     * Converts a local file uri to a local path name. This will throw an
     * IllegalArgumentException if the passed uri is not a valid file uri.
     *
     * @param uri A uri that references a local file.
     * @return The path name suitable for passing to the File class.
     */
    fun getPathNameFromFileUri(uri: Uri): String {
        Preconditions.checkArgument(URLUtil.isFileUrl(uri.toString()),
                                    "Invalid file uri")
        return uri.path!!
    }

    /**
     * Determines if passed string is a valid URL.
     *
     * @param url A URL string.
     * @return `true` if passed URL is valid; `false` if not.
     */
    fun isValidUrl(url: String): Boolean {
        return if (TextUtils.isEmpty(url)) {
            false
        } else {
            ContentResolver.SCHEME_ANDROID_RESOURCE == Uri.parse(url).scheme ||
            URLUtil.isValidUrl(url)
        }

        // Allow urls from testing framework that use images stored in the
        // application resources. Application resource urls will have a scheme
        // of "android.resource" which will not be recognized by
        // UrlUtil.isResourceUrl() which expects resource urls to start with
        // "file:///android_res/".
    }

    /**
     * Constructs a proper "file://" uri for the passed local path name. The
     * most reliable way to create this kind of uri is create a File object with
     * the passed path name and then use the Uri.fromFile() helper to build the
     * uri.
     *
     * @param pathName An absolute path to a local file.
     * @return The file uri.
     */
    fun getFileUriFromPathName(pathName: String): Uri? {
        // Prevent anyone from passing a Uri.toString() value to this method.
        Preconditions.checkArgument(!URLUtil.isValidUrl(pathName),
                                    "pathName must not be a uri string")
        // Safest method is to use fromFile().
        return Uri.fromFile(File(pathName))
    }

    /**
     * Returns a File object for the passed file uri. Throws an exception if
     * the passed uri is not well constructed file uri.
     *
     * @param uri A file uri.
     * @return A File object.
     */
    fun getFileFromUri(uri: Uri): File? {
        Preconditions.checkArgument(URLUtil.isFileUrl(uri.toString()),
                                    "uri must be of file uri")

        return File(uri.path!!)
    }

    /**
     * Returns a file uri for the specified local file. This method is here only
     * to try to centralize all Uri calls for debugging purposes.
     *
     * @param file A local file.
     * @return A well constructed file uri.
     */
    fun getUriFromFile(file: File): Uri {
        return Uri.fromFile(file)
    }

    /**
     * Converts the passed local path name to content uri suitable for passing
     * in an intent to any system activity.
     *
     * @param context  A context.
     * @param pathName A local path name.
     * @return A content uri.
     */
    fun getFileContentUri(context: Context, pathName: String): Uri {
        Preconditions.checkArgument(!URLUtil.isValidUrl(pathName),
                                    "pathName must not be a uri string")
        return FileProvider.getUriForFile(
                context, fileProviderAuthority, File(pathName))
    }

    /**
     * Converts the passed local uri to a content uri suitable for passing in an
     * intent to any system activity.
     *
     * @param context A context.
     * @param uri     A local file uri.
     * @return A content uri.
     */
    fun getFileContentUri(context: Context, uri: Uri): Uri {
        return getFileContentUri(context, getPathNameFromFileUri(uri))
    }

    /**
     * Returns the original uri from the passed cache path name.
     *
     * @param uri A cache uri.
     * @return The original uri that was used to create the cache path name.
     */
    @Deprecated("")
    fun getSourceUriFromCacheUri(uri: Uri): Uri? {
        return try {
            val encodedName = File(uri.toString()).name
            Uri.parse(URLDecoder.decode(encodedName, "UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            Log.w(TAG, "Unable to retrieve URL from cached path name")
            null
        }

    }

    /**
     * Grants the specified uri permissions to all packages that can process the
     * intent. The most secure granting model is used for the current API. This
     * method is designed to work on all versions of Android but has been tested
     * only on API 23, and 24.
     *
     * @param context     A context.
     * @param intent      An intent containing a data uri that was obtained from
     * FileProvider.getUriForFile().
     * @param permissions The permissions to grant.
     */
    fun grantUriPermissions(
            context: Context, intent: Intent, permissions: Int) {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            // Find all packages that support this intent and grant them
            // the specified permissions.
            val resInfoList = context.packageManager.queryIntentActivities(
                    intent, PackageManager.MATCH_DEFAULT_ONLY)
            resInfoList
                    .map { it.activityInfo.packageName }
                    .forEach {
                        context.grantUriPermission(
                                it,
                                intent.data,
                                permissions)
                    }
        } else {
            // Just grant permissions to all apps.
            intent.flags = permissions
        }
    }

    /**
     * Builds an action intent and converts the passed local file uri to a
     * content uri with read permission for all applications that can process
     * the intent. This method is designed to work on all versions of Android
     * but has only been tested on API 23 and 24.
     *
     * @param context  A context.
     * @param pathName A local file path.
     * @param action   The intent action.
     * @param type     The intent type.
     * @return The built intent.
     */
    fun buildReadPrivateUriIntent(
            context: Context, pathName: String, action: String, type: String): Intent {
        // Build a content uri.
        val uri = FileProvider.getUriForFile(
                context, fileProviderAuthority, File(pathName))

        // Create and initialize the intent.
        val intent = Intent()
                .setAction(action)
                .setDataAndType(uri, type)

        // Call helper method that uses the most secure permission granting
        // model for the each API.
        grantUriPermissions(context, intent,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION)

        return intent
    }

    /**
     * Builds an action intent and converts the passed local file uri to a
     * content uri with read permission for all applications that can process
     * the intent. This method is designed to work on all versions of Android
     * but has only been tested on API 23 and 24.
     *
     * @param context A context.
     * @param uri     A local file uri.
     * @param action  The intent action.
     * @param type    The intent type.
     * @return The built intent.
     */
    fun buildReadPrivateUriIntent(
            context: Context, uri: Uri, action: String, type: String): Intent {
        return buildReadPrivateUriIntent(
                context, getPathNameFromFileUri(uri), action, type)
    }

    /**
     * Returns a uri's base data resource name without an extension.
     *
     * @param uri A uri.
     * @return The base data resource name.
     */
    fun getLastPathSegmentBaseName(uri: Uri?): String {
        var name: String? = null

        if (uri != null && !TextUtils.isEmpty(uri.toString())) {
            name = uri.lastPathSegment
            if (name != null && name.contains(".")) {
                name = name.substring(0, name.lastIndexOf("."))
            }
        }

        return if (name != null) name else ""
    }


    /**
     * Parses an array of string urls into an array of Uris. No error checking
     * is performed on the parsing.
     *
     * @return An array of parsed Uris.
     */
    fun parseAll(vararg strings: String): List<Uri> {
        return strings.map { Uri.parse(it) }.toList()
    }
}
