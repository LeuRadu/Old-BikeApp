package com.leuradu.android.bikeapp.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.StatFs;

import com.google.common.io.ByteStreams;
import com.skobbler.ngx.util.SKLogging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by radu on 25.03.2016.
 */
public class Util {

    private static final String TAG = "Util";
    private static final int K = 1024;
    private static final int M = K*K;

//      Returns most suitable path for storing data.
//      Returns null if context is null
    public static String determineStoragePath(Context context) {
        if (context == null) return null;

        if (getFreeMemorySize(Environment.getDataDirectory().getPath()) >= 50 * M) {
            if (context.getFilesDir() != null) {
                return context.getFilesDir().getPath();
            }
        } else {
            if ((context.getExternalFilesDir(null) != null)) {
                if (getFreeMemorySize(context.getExternalFilesDir(null).toString()) >= 50 * M) {
                    return context.getExternalFilesDir(null).toString();
                }
            }
        }

        SKLogging.writeLog(TAG, "There is not enough memory on any storage, but return internal memory",
                SKLogging.LOG_DEBUG);

        return getInternalMemoryPath(context);
    }

//    Return path of the internal memory
    private static String getInternalMemoryPath(Context context) {
        if (context.getFilesDir() != null) {
            return context.getFilesDir().getPath();
        } else {
            if (context.getExternalFilesDir(null) != null) {
                return context.getExternalFilesDir(null).toString();
            } else {
                return null;
            }
        }
    }

//    Returns number of bytes of free memory at the given path
    public static long getFreeMemorySize(String path) {
        StatFs statFs = null;
        try {
            statFs = new StatFs(path);
        } catch (IllegalArgumentException ex) {
            SKLogging.writeLog(TAG, "Exception when creating StatF; message = " + ex,
                    SKLogging.LOG_DEBUG);
        }
        if (statFs != null) {
            Method getAvailableBytesMethod = null;
            try {
                getAvailableBytesMethod = statFs.getClass().getMethod("getAvailableBytes");
            } catch (NoSuchMethodException e) {
                SKLogging.writeLog(TAG, "Exception at getFreeMemorySize method = " + e.getMessage(),
                        SKLogging.LOG_DEBUG);
            }

            if (getAvailableBytesMethod != null) {
                try {
                    SKLogging.writeLog(TAG, "Using new API for getFreeMemorySize method", SKLogging.LOG_DEBUG);
                    return (Long) getAvailableBytesMethod.invoke(statFs);
                } catch (IllegalAccessException e) {
                    return (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
                } catch (InvocationTargetException e) {
                    return (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
                }
            } else {
                return (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
            }
        } else {
            return 0;
        }
    }

    public static AlertDialog createErrorDialog( Context context, String title, String message )
    {
        return new AlertDialog.Builder( context )
                .setTitle( title )
                .setMessage( message )
                .setIcon( android.R.drawable.ic_dialog_alert )
                .create();
    }

//    TODO: manage asset copying somewhere else?
    public static void copyAssetsToFolder(AssetManager assetManager, String sourceFolder, String destinationFolder)
            throws IOException {
        final String[] assets = assetManager.list(sourceFolder);

        final File destFolderFile = new File(destinationFolder);
        if (!destFolderFile.exists()) {
            destFolderFile.mkdirs();
        }
        copyAsset(assetManager, sourceFolder, destinationFolder, assets);
    }

    public static void copyAsset(AssetManager assetManager, String sourceFolder, String destinationFolder,
                                 String... assetsNames) throws IOException {

        for (String assetName : assetsNames) {
            OutputStream destinationStream = new FileOutputStream(new File(destinationFolder + "/" + assetName));
            String[] files = assetManager.list(sourceFolder + "/" + assetName);
            if (files == null || files.length == 0) {

                InputStream asset = assetManager.open(sourceFolder + "/" + assetName);
                try {
                    ByteStreams.copy(asset, destinationStream);
                } finally {
                    asset.close();
                    destinationStream.close();
                }
            }
        }
    }
}
