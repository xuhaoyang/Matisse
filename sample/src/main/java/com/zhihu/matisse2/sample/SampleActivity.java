/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse2.sample;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.RomUtils;
import com.bumptech.glide.Glide;
import com.echat.matisse.Matisse;
import com.echat.matisse.MimeType;
import com.echat.matisse.engine.impl.PicassoEngine;
import com.echat.matisse.filter.Filter;
import com.echat.matisse.internal.entity.CaptureStrategy;
import com.echat.matisse.listener.OnCheckedListener;
import com.echat.matisse.listener.OnMaxFileSizeListener;
import com.echat.matisse.listener.OnSelectedListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SampleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE = 23;

    private UriAdapter mAdapter;
    private Toast sToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.zhihu).setOnClickListener(this);
        findViewById(R.id.dracula).setOnClickListener(this);

        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //content://media/external/video/media/1417095
                //content://media/external/video/media/1417110
                //content://media/external/video/media/1417094
                //ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), 1417094)
                //Uri.parse("content://media/external/video/media/1417110")

//Uri.parse("content://media/external/images/media/1417113")

//                String s = "content://media/external/images/media/1417113";
//                int beginIndex = s.lastIndexOf("/");
//                Log.e("TEST", "onClick: " + beginIndex);
//                Log.e("TEST", "onClick: " + s.substring(beginIndex + 1));
//                query(Uri.parse("content://media/external/images/media/1417113"));
//                query(ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), 1417113));
//                Uri external = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), 1417094);
//                Log.e("TEST", "onClick: " + external.toString());
//                query(external);
//                delete();
//                queryDelete();
//                scanFileOld1();
//                queryHUAWEIGallery();
//                openSystemPicker();
                checkSystemGalary();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new UriAdapter());

        findViewById(R.id.btn_open_lib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSystemPicker();
            }
        });
    }


    private void query(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            Log.e("Sample", "onClick: " + "空或者没有数据");
        }

        Log.e("Sample", "onClick: " + DatabaseUtils.dumpCursorToString(cursor));
        Log.e("DATA", "getString: DATA -> " + cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
        Log.e("DATA", "getString: DISPLAY_NAME -> " + cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)));

    }

    private void scanFileOld1() {
        Uri uri = Uri.fromFile(new File("/mnt/sdcard/Pictures/TestImages/"));
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(intent);
    }


    private void scanFile() {
        MediaScannerConnection.scanFile(this,
                new String[]{"/sdcard/Pictures/TestImages/"},
                new String[]{"*/*"},
                new MediaScannerConnection.MediaScannerConnectionClient() {
                    @Override
                    public void onMediaScannerConnected() {
                        Log.i("Scan", "onMediaScannerConnected ");

                    }

                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("Scan", "onScanCompleted: " + path);
                    }
                });

        new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.i("Scan", "onScanCompleted: " + path);
            }
        };
    }

    private static final int REQUEST_SYSTEM_PICKER = 10011;

    private void openSystemImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("video/*;image/*");
        startActivityForResult(intent, REQUEST_SYSTEM_PICKER);
    }

    private void openSystemPicker() {
        File file = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        Uri cameraOutputUri = Uri.fromFile(file);
        Intent intent = getPickIntent(cameraOutputUri);
        startActivityForResult(intent, REQUEST_SYSTEM_PICKER);
    }

    private Intent getPickIntent(Uri cameraOutputUri) {
        final List<Intent> intents = new ArrayList<Intent>();

        if (true) {
            intents.add(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }

        if (true) {
            setCameraIntents(intents, cameraOutputUri);
        }

        LogUtils.i(intents);
        if (intents.isEmpty()) return null;
        Intent result = Intent.createChooser(intents.remove(0), null);
        if (!intents.isEmpty()) {
            result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
        }
        return result;
    }


    //HW
    final static String HUAWEIGalleryPackageName = "com.android.gallery3d";
    final static String XIAOMIGalleryPackageName = "com.miui.gallery";

    //Miui com.miui.gallery/.picker.PickGalleryActivity
    private void checkSystemGalary() {
        //com.android.gallery3d
        final PackageManager packageManager = getPackageManager();
        boolean hasHUAWEIGalary = false;
        if (RomUtils.isHuawei()) {
            String version = RomUtils.getRomInfo().getVersion();
            String[] split = version.split("\\.");
            if (split.length > 0 && "10".equals(split[0])) {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(HUAWEIGalleryPackageName, 0);
                    if (packageInfo != null) {
                        hasHUAWEIGalary = true;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    hasHUAWEIGalary = false;
                }
            }
        }

        hasHUAWEIGalary = true;
        if (hasHUAWEIGalary) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            final List<ResolveInfo> listGalary = packageManager.queryIntentActivities(intent, 0);
            LogUtils.i(listGalary);
            String packageName = null, name = null;
            for (ResolveInfo resolveInfo : listGalary) {
                if (HUAWEIGalleryPackageName.equals(resolveInfo.activityInfo.packageName) ||
                        XIAOMIGalleryPackageName.equals(resolveInfo.activityInfo.packageName)) {
                    packageName = resolveInfo.activityInfo.packageName;
                    name = resolveInfo.activityInfo.name;
                    break;
                }
            }
            if (!TextUtils.isEmpty(name)) {
                intent.setComponent(new ComponentName(packageName, name));
                intent.setPackage(packageName);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("video/*;image/*");
                startActivityForResult(intent, REQUEST_SYSTEM_PICKER);
            }
        }
    }

    private void queryHUAWEIGallery() {
        final List<Intent> intents = new ArrayList<Intent>();

        Intent query = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(query, 0);
        for (ResolveInfo res : listCam) {
            LogUtils.i(res);
            final Intent intent = new Intent(query);
            final String packageName = res.activityInfo.packageName;
            final String name = res.activityInfo.name;
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.setType("video/*;image/*");
            intents.add(intent);
        }
        Intent result = Intent.createChooser(intents.remove(0), null);
        result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
        startActivityForResult(result, REQUEST_SYSTEM_PICKER);
    }

    private void setCameraIntents(List<Intent> cameraIntents, Uri output) {
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
            cameraIntents.add(intent);
        }
    }

    private void queryDelete() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"),
                null,
                MediaStore.MediaColumns.DATA + " like ?",
                new String[]{"%com.echat.echatjsdemo.single/files/DCIM/Echat%"},
                null);
        //Android/data/com.echat.echatjsdemo.single/files/DCIM/Echat
        Log.w("queryDelete", "onClick: " + DatabaseUtils.dumpCursorToString(cursor));
    }

    private void delete() {
        ContentResolver contentResolver = getContentResolver();
        int external = contentResolver.delete(MediaStore.Files.getContentUri("external"),
                MediaStore.MediaColumns.DATA + " like ?",
                new String[]{"%com.echat.echatjsdemo.single/files/DCIM/Echat%"});
        Log.w("delete", "delete: " + external);

    }

    @Override
    public void onClick(final View v) {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            switch (v.getId()) {
                                case R.id.zhihu:
                                    Matisse.from(SampleActivity.this)
                                            .choose(MimeType.ofAll(), false)
                                            .countable(true)
                                            .capture(true)
                                            .captureStrategy(
                                                    new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
                                            .maxSelectable(9)
                                            .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                                            .gridExpectedSize(
                                                    getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                            .thumbnailScale(0.85f)
//                                            .imageEngine(new GlideEngine())  // for glide-V3
                                            .imageEngine(new Glide4Engine())    // for glide-V4
                                            .setOnSelectedListener(new OnSelectedListener() {
                                                @Override
                                                public void onSelected(
                                                        @NonNull List<Uri> uriList, @NonNull List<String> pathList) {
                                                    // DO SOMETHING IMMEDIATELY HERE
                                                    Log.e("onSelected", "onSelected: pathList=" + pathList);

                                                }
                                            })
                                            .originalEnable(true)
                                            .maxOriginalSize(8)
                                            .autoHideToolbarOnSingleTap(true)
                                            .setOnCheckedListener(new OnCheckedListener() {
                                                @Override
                                                public void onCheck(boolean isChecked) {
                                                    // DO SOMETHING IMMEDIATELY HERE
                                                    Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                                                }
                                            })
//                                            .maxFileSize(10 * 1024 * 1024)
//                                            .setOnMaxFileSizeListener(new OnMaxFileSizeListener() {
//                                                @Override
//                                                public void triggerLimit() {
//                                                    showToast("超出10M大小，无法上传", Toast.LENGTH_SHORT);
//                                                }
//                                            })
                                            .forResult(REQUEST_CODE_CHOOSE);
                                    break;
                                case R.id.dracula:
                                    Matisse.from(SampleActivity.this)
                                            .choose(MimeType.ofAll())
                                            .theme(R.style.Matisse_Dracula)
                                            .countable(false)
                                            .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                                            .maxSelectablePerMediaType(9, 1)
                                            .originalEnable(false)
                                            .imageEngine(new PicassoEngine())
                                            .maxFileSize(20 * 1024 * 1024)
                                            .setOnMaxFileSizeListener(new OnMaxFileSizeListener() {
                                                @Override
                                                public void triggerLimit() {
                                                    showToast("超出20M大小，无法上传", Toast.LENGTH_SHORT);
                                                }
                                            })
                                            .forResult(REQUEST_CODE_CHOOSE);
                                    break;
                                default:
                                    break;
                            }
                            mAdapter.setData(null, null);
                        } else {
                            Toast.makeText(SampleActivity.this, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * @param text
     * @param duration
     */
    private void showToast(final CharSequence text, final int duration) {
        cancelToast();
        sToast = Toast.makeText(this, text, duration);
        sToast.show();
    }

    private void cancelToast() {
        if (sToast != null) {
            sToast.cancel();
            sToast = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mAdapter.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data));
            Log.e("OnActivityResult ", String.valueOf(Matisse.obtainOriginalState(data)));
            query(Matisse.obtainResult(data).get(0));
        }

        if (requestCode == REQUEST_SYSTEM_PICKER) {
            LogUtils.i(data);
            if (data != null) {
                Uri data1 = data.getData();
                LogUtils.i(data1);
                View root = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null, false);
                ImageView imageView = root.findViewById(R.id.imageview);
                Glide.with(this)
                        .load(data1)
                        .into(imageView);
                new AlertDialog.Builder(this).setView(root).setCancelable(true)
                        .show();

            }
        }
    }

    private static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private List<Uri> mUris;
        private List<String> mPaths;

        void setData(List<Uri> uris, List<String> paths) {
            mUris = uris;
            mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {
            holder.mUri.setText(mUris.get(position).toString());
            holder.mPath.setText(mPaths.get(position));

            holder.mUri.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
            holder.mPath.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        static class UriViewHolder extends RecyclerView.ViewHolder {

            private TextView mUri;
            private TextView mPath;

            UriViewHolder(View contentView) {
                super(contentView);
                mUri = (TextView) contentView.findViewById(R.id.uri);
                mPath = (TextView) contentView.findViewById(R.id.path);
            }
        }
    }


}
