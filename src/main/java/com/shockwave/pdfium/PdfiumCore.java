package com.shockwave.pdfium;

import android.util.Log;
import android.view.Surface;

import java.io.FileDescriptor;
import java.lang.reflect.Field;

public class PdfiumCore {
    private static final String TAG = PdfiumCore.class.getName();

    static{
        System.loadLibrary("jniPdfium");
    }

    private native long nativeOpenDocument(int fd);
    private native void nativeCloseDocument(long docPtr);
    private native long nativeLoadPage(long docPtr, int pageIndex);
    private native long[] nativeLoadPages(long docPtr, int fromIndex, int toIndex);
    private native void nativeClosePage(long pagePtr);
    private native void nativeClosePages(long[] pagesPtr);
    //private native long nativeGetNativeWindow(Surface surface);
    //private native void nativeRenderPage(long pagePtr, long nativeWindowPtr);
    private native void nativeRenderPage(long pagePtr, Surface surface, int dpi);

    private static final Class FD_CLASS = FileDescriptor.class;
    private static final String FD_FIELD_NAME = "descriptor";
    private static Field mFdField = null;

    public static int getNumFd(FileDescriptor fdObj){
        try{
            if(mFdField == null){
                mFdField = FD_CLASS.getDeclaredField(FD_FIELD_NAME);
                mFdField.setAccessible(true);
            }

            return mFdField.getInt(fdObj);
        }catch(NoSuchFieldException e){
            e.printStackTrace();
            return -1;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public PdfDocument newDocument(FileDescriptor fd){
        PdfDocument document = new PdfDocument();

        document.mNativeDocPtr = nativeOpenDocument(getNumFd(fd));
        if(document.mNativeDocPtr <= 0) Log.e(TAG, "Open document failed");

        return document;
    }

    public long openPage(PdfDocument doc, int pageIndex){
        long pagePtr = nativeLoadPage(doc.mNativeDocPtr, pageIndex);
        doc.mNativePagesPtr.put(pageIndex, pagePtr);
        return pagePtr;
    }

    public void renderPage(PdfDocument doc, Surface surface, int pageIndex, int densityDpi){
        nativeRenderPage(doc.mNativePagesPtr.get(pageIndex), surface, densityDpi);
    }

    public void closeDocument(PdfDocument doc){

        for(Integer index : doc.mNativePagesPtr.keySet()){
            nativeClosePage(doc.mNativePagesPtr.get(index));
        }
        doc.mNativePagesPtr.clear();

        nativeCloseDocument(doc.mNativeDocPtr);
    }
}