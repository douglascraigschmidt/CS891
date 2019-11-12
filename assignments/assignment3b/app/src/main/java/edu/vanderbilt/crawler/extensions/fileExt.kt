package edu.vanderbilt.crawler.extensions

import android.net.Uri
import java.io.File

fun File(uri: Uri): File = File(uri.path!!)

fun File.fromUri(uri: String) = File(Uri.parse(uri).path!!)

