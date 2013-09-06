package net.snoone.SaveFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private TextView textView;
    private String tempFilePath;
    private String zxingDownloadUrl = "https://zxing.googlecode.com/files/BarcodeScanner4.3.2.apk";
    private String fileName = "BarcodeScanner4.3.2";
    private String fileExName = "apk";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textView = (TextView)findViewById(R.id.txtMsg);
    }

    /*  下載功能區--開始 */
    public void onDownloadClick(View view){
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size()==0){
            new ConnectionTask().execute(zxingDownloadUrl);
        }else{
            textView.setText("元件已裝");
        }

    }
    private class ConnectionTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.connect();
                InputStream inputStream = conn.getInputStream();
                if(inputStream==null){
                    throw new RuntimeException("Stream is null");
                }
                tempFilePath = getApplicationContext().getCacheDir().getAbsolutePath();
                File tempFile = File.createTempFile(fileName, "."+fileExName);
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                try {
                    byte[] buf = new byte[256];
                    int len = 0;
                    while ((len=inputStream.read(buf))!=-1){
                        fileOutputStream.write(buf, 0, len);
                    }
                }catch (Exception e){
                    inputStream.close();
                    fileOutputStream.close();
                    return "串流寫入錯誤!中止下載";
                }


                //檔案列表
                String[] filesList = getApplicationContext().getCacheDir().list();
                String filesListStr = "";
                for(int i=0; i<filesList.length; i++){
                    File nowFile = new File( getApplicationContext().getCacheDir() + "/" + filesList[i]);
                    filesListStr += "("+ i +")" + filesList[i] + " Size:" + (nowFile.length()/1024)  + "KB\n";
                }
                startInstall(tempFile);
                try {
                    inputStream.close();
                    fileOutputStream.close();
                }catch (Exception e){
                    return "Stream close fail!!"+e.getMessage();
                }

                return tempFile.getName() + " 下載完成\n" + " Cache:" + tempFilePath + "\n" + filesListStr;
            }catch (Exception e){
                return e.getMessage();
            }
        }
        @Override
        protected void onPostExecute(String result){
            textView.setText(result);
        }
    }
    public void startInstall(File apkFile){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile),"application/vnd.android.package-archive");
        startActivity(intent);
    }

    /*  下載功能區--結束 */

    /*存檔--開始*/
    //Save File Button Click
    public void onSaveFileClick(View view){
        //TextView txtMsg = (TextView)findViewById(R.id.txtMsg);
        String filename = "myfile";
        String str = "Hello 文仁!!";
        FileOutputStream outputStream;
        FileInputStream inputStream;
        try{
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(str.getBytes());
            outputStream.close();
            textView.setText("存檔完畢\n");
            inputStream = openFileInput(filename);
            String[] savedFiles = getApplicationContext().fileList();
            for (int i=0; i<savedFiles.length; i++){
                textView.setText(savedFiles[i]+"\n");
                textView.append(getApplicationContext().getFilesDir().getPath()+"\n");
            }
        }catch (Exception e){
            textView.setText(e.getLocalizedMessage());
        }
    }
    /*存檔--結束*/
}
