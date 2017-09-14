package edu.vanderbilt.webcrawler.extensions

import android.net.Uri
import java.io.File

/**
 * Created by monte on 2017-09-13.
 */

fun File(uri: Uri): File = File(uri.path)

fun File.fromUri(uri: String) = File(Uri.parse(uri).path)

