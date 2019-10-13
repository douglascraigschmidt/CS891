package edu.vanderbilt.crawler.utils;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * A FileObserver that observes all the files/folders within given directory
 * recursively. It automatically starts/stops monitoring new folders/files
 * created after starting the watch.
 * <p>
 * From: https://gist.github.com/gitanuj/888ef7592be1d3f617f6
 */
public class RecursiveFileObserver extends FileObserver {
    /**
     * Logging tag.
     */
    private static final String TAG = "RecursiveFileObserver";

    private final Map<String, FileObserver> mObservers = new HashMap<>();

    private String mPath;

    private int mMask;

    private EventListener mListener;

    public RecursiveFileObserver(String path, EventListener listener) {
        this(path, ALL_EVENTS, listener);
    }

    public RecursiveFileObserver(String path, int mask, EventListener listener) {
        super(path, mask);
        mPath = path;
        mMask = mask | FileObserver.CREATE | FileObserver.DELETE_SELF;
        mListener = listener;
    }

    private void startWatching(String path) {
        synchronized (mObservers) {
            FileObserver observer = mObservers.remove(path);
            if (observer != null) {
                observer.stopWatching();
            }
            observer = new SingleFileObserver(path, mMask);
            observer.startWatching();
            mObservers.put(path, observer);
        }
    }

    @Override
    public void startWatching() {
        Stack<String> stack = new Stack<>();
        stack.push(mPath);

        // Recursively watch all child directories
        while (!stack.empty()) {
            String parent = stack.pop();
            startWatching(parent);

            File path = new File(parent);
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (watch(file)) {
                        stack.push(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private boolean watch(File file) {
        return file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..");
    }

    private void stopWatching(String path) {
        synchronized (mObservers) {
            FileObserver observer = mObservers.remove(path);
            if (observer != null) {
                observer.stopWatching();
            }
        }
    }

    @Override
    public void stopWatching() {
        synchronized (mObservers) {
            for (FileObserver observer : mObservers.values()) {
                observer.stopWatching();
            }
            mObservers.clear();
        }
    }

    @Override
    public void onEvent(int event, String path) {
        File file;
        if (path == null) {
            file = new File(mPath);
        } else {
            file = new File(mPath, path);
        }
        notify(event, file);
    }

    private void notify(int event, File file) {
        if (mListener != null) {
            mListener.onEvent(event & FileObserver.ALL_EVENTS, file);
        }
    }

    public interface EventListener {
        void onEvent(int event, File file);
    }

    private class SingleFileObserver extends FileObserver {
        private String filePath;

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            filePath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            File file;
            if (path == null) {
                file = new File(filePath);
            } else {
                file = new File(filePath, path);
            }

            switch (event & FileObserver.ALL_EVENTS) {
                case DELETE_SELF:
                    RecursiveFileObserver.this.stopWatching(filePath);
                    break;
                case CREATE:
                    if (watch(file)) {
                        Log.d(TAG, "CREATE - starting to watch " + file.getAbsolutePath());
                        RecursiveFileObserver.this.startWatching(file.getAbsolutePath());
                    }
                    break;
            }

            switch (event & FileObserver.ALL_EVENTS) {
                case FileObserver.ACCESS:
                    Log.d(TAG, "ACCESS: " + file.getAbsolutePath());
                    break;
                case FileObserver.ATTRIB:
                    Log.d(TAG, "ATTRIB: " + file.getAbsolutePath());
                    break;
                case FileObserver.CLOSE_NOWRITE:
                    Log.d(TAG, "CLOSE_NOWRITE: " + file.getAbsolutePath());
                    break;
                case FileObserver.CLOSE_WRITE:
                    Log.d(TAG, "CLOSE_WRITE: " + file.getAbsolutePath());
                    break;
                case FileObserver.CREATE:
                    Log.d(TAG, "CREATE: " + file.getAbsolutePath());
                    break;
                case FileObserver.DELETE:
                    Log.d(TAG, "DELETE: " + file.getAbsolutePath());
                    break;
                case FileObserver.DELETE_SELF:
                    Log.d(TAG, "DELETE_SELF: " + file.getAbsolutePath());
                    break;
                case FileObserver.MODIFY:
                    Log.d(TAG, "MODIFY: " + file.getAbsolutePath());
                    break;
                case FileObserver.MOVED_FROM:
                    Log.d(TAG, "MOVED_FROM: " + file.getAbsolutePath());
                    break;
                case FileObserver.MOVED_TO:
                    Log.d(TAG, "MOVED_TO: " + file.getAbsolutePath());
                    break;
                case FileObserver.MOVE_SELF:
                    Log.d(TAG, "MOVE_SELF: " + file.getAbsolutePath());
                    break;
                case FileObserver.OPEN:
                    Log.d(TAG, "OPEN: " + file.getAbsolutePath());
                    break;
                default:
                    Log.d(TAG, "UNKNOWN!!!: " + file.getAbsolutePath());
                    break;
            }

            RecursiveFileObserver.this.notify(event, file);
        }
    }
}

