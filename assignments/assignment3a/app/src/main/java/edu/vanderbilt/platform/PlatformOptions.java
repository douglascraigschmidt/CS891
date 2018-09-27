package edu.vanderbilt.platform;

/**
 * Options are obtained in different ways on different platforms.
 * For example, form a Java console, the options are specified on
 * the command line, and on Android, they are specified through a
 * settings Activity.
 */
public interface PlatformOptions {
	int getMaxDepth();
	String getRootUri();

	String getDownloadDirName();

	boolean getDiagnosticsEnabled();
}
