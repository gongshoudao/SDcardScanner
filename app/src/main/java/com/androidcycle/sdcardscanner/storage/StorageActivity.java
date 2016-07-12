package com.androidcycle.sdcardscanner.storage;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.androidcycle.sdcardscanner.R;
import com.androidcycle.sdcardscanner.common.utils.StorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class StorageActivity extends AppCompatActivity implements StoragePathRecyclerAdapter.StorageViewHoder.StorageOperatorInterface {

    private static final String TAG = StorageActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        mRecyclerView = (RecyclerView) findViewById(R.id.storage_recycler_view);
        ArrayList<StorageBean> storageData = StorageUtils.getStorageData(this);
        StoragePathRecyclerAdapter storagePathRecyclerAdapter = new StoragePathRecyclerAdapter(this);
        storagePathRecyclerAdapter.refreshData(storageData);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLapeyoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(storagePathRecyclerAdapter);
    }

    @Override
    public void onOpenExplorer(String url) {
        try {
            if (!TextUtils.isEmpty(url)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                final String testFilePath = getTestDataPath(url);
                final File file = new File(testFilePath);
                if (file.exists()) {
                    Uri uri = Uri.parse(testFilePath);
                    intent.setDataAndType(uri, "text/plain");
                    startActivity(Intent.createChooser(intent, "建议使用系统自带文件浏览器"));
                } else {
                    writeStringToFile(testFilePath, file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWriteTest(String url) {
        String filePath = getTestDataPath(url);
        final File testFile = new File(filePath);
        writeStringToFile(filePath, testFile);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void getFilesDirs() {
        try {
            final File[] dirs = getExternalFilesDirs(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeStringToFile(String filePath, File testFile) {
        FileOutputStream fileOutputStream = null;
        try {
            if (!testFile.exists()) {
                final boolean createSuccess = testFile.createNewFile();
                final String notice = createSuccess ? "创建" + filePath + "成功!" : "创建" + filePath + "失败!";
                showSnack(notice);

                if (createSuccess) {
                    fileOutputStream = new FileOutputStream(testFile);
                    fileOutputStream.write("你好！".getBytes());
                }
            } else {
                fileOutputStream = new FileOutputStream(testFile, true);
                fileOutputStream.write("+1".getBytes());
                showSnack("追加写入成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showSnack(e.getMessage());
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @NonNull
    private String getTestDataPath(String url) {
        if (Build.VERSION.SDK_INT >= 19) {
            getFilesDirs();
        }
        final String dir = url + "/Android/data/" + getPackageName() + "/";
        final File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dir + "test.txt";
    }

    private void showSnack(String notice) {
        if (notice != null) {
            Snackbar.make(mRecyclerView, notice, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDeleteTest(String url) {
        final File testFilePath = new File(getTestDataPath(url));
        if (testFilePath.exists()) {
            final boolean delete = testFilePath.delete();
            showSnack(delete ? "删除成功！" : "删除失败");
        } else {
            showSnack(getTestDataPath(url) + " 文件不存在！");
        }
    }
}
