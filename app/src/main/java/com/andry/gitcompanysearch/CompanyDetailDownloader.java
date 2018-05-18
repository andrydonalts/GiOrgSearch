package com.andry.gitcompanysearch;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.andry.gitcompanysearch.model.CompanyItem;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by andry on 15.05.2018.
 */

public class CompanyDetailDownloader<T> extends HandlerThread {

    private static final int MESSAGE_DOWNLOAD_DETAILS = 0;
    private static final String TAG = "CompanyDetailDownloader";
    private ConcurrentHashMap<T, CompanyItem> requestMap = new ConcurrentHashMap<>();
    private DetailsDownloadedListener detailsDownloadedListener;
    private Handler requestHandler;
    private Handler responseHandler;
    private boolean hasQuit;

    public CompanyDetailDownloader(Handler responseHandler) {
        super(TAG);
        this.responseHandler = responseHandler;
    }

    @Override
    public boolean quit() {
        hasQuit = true;
        return super.quit();
    }

    @Override
    protected void onLooperPrepared() {
        requestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MESSAGE_DOWNLOAD_DETAILS) {
                    T target = (T) msg.obj;
                    handleRequest(target, msg.arg1);
                }
            }
        };
    }

    private void handleRequest(final T target, final int position) {
        final CompanyItem item = new GitFetch().fetchCompanyItem(requestMap.get(target));
        if (item == null || item.getName() == null)
            return;
        responseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (hasQuit || requestMap.get(target) == null || !item.getName().equals(requestMap.get(target).getName())) {
                    return;
                }
                requestMap.remove(target);
                detailsDownloadedListener.onDetailsDownloaded(item, target, position);
            }
        });
    }

    public void queryCompanyDetails(CompanyItem item, T target, int position) {
        if (item == null) {
            requestMap.remove(target);
        } else {
            requestMap.put(target, item);
            requestHandler.obtainMessage(MESSAGE_DOWNLOAD_DETAILS, position, 0, target).sendToTarget();
        }
    }

    public void clearQuery() {
        requestHandler.removeMessages(MESSAGE_DOWNLOAD_DETAILS);
        requestMap.clear();
    }

    public void setDetailsDownloadedListener(DetailsDownloadedListener<T> listener) {
        this.detailsDownloadedListener = listener;
    }

    public interface DetailsDownloadedListener<T> {
        void onDetailsDownloaded(CompanyItem item, T target, int position);
    }
 }
