package com.example.yujhaochen.ims;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

//import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by yujhaochen on 2016/10/24.
 */
public class GetServiceData {

    //正式機
    public static String ServicePath = "http://172.16.98.4/IMSApp/IMS_App_Service.asmx";

    //測試機
    //public static String ServicePath = "http://172.16.111.111:200/IMS_App_Service.asmx";

    public static void getString(String Url, RequestQueue mQueue, final VolleyCallback callback) {

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, Url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("TestJsonObj");
                        System.out.println(error);
                    }
                }
        );

        int socketTimeout = 30000;

        getRequest.setRetryPolicy(new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mQueue.add(getRequest);
    }


    public static void SendPostRequest(String Url, RequestQueue mQueue,final VolleyStringCallback callback,final Map<String, String> map)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Url,   new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSendRequestSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put("params1", "value1");
//                map.put("params2", "value2");
                return map;
            }
        };

        mQueue.add(stringRequest);
    }


    public static void SendRequest(String Url, RequestQueue mQueue,final VolleyStringCallback callback)
    {
        StringRequest stringRequest = new StringRequest(Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSendRequestSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        mQueue.add(stringRequest);
    }

    public static void getImage(String Url, RequestQueue mQueue, final ImageView Img) {
        ImageRequest request = new ImageRequest(Url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        Img.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        // mImageViㄌew.setImageResource(R.drawable.image_load_error);
                    }
                });
    }

    public static String encodeURIComponent(String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public static void GetImageByImageLoad(String Url, final ImageView Img) {

        if(!(Url.contains("http://") || Url.contains("https://")))
        {
            Url = "http:" + Url;

        }

        try
        {

            String ReplaceURL = Url.substring(Url.lastIndexOf('/') + 1,Url.length());



            String EncodeURL = "";

            if (ReplaceURL != "")
            {
                EncodeURL = URLEncoder.encode(ReplaceURL,"utf-8");
            }

            Url = Url.replace(ReplaceURL,EncodeURL);

        }
        catch (IOException e)
        {
            System.out.print(e);
        }


        RequestQueue mQueue = AppController.getInstance().getRequestQueue();

        ImageLoader imageLoader = new ImageLoader(mQueue, new BitmapCache() {
            @Override
            public void putBitmap(String url, Bitmap bitmap) {

                Img.setImageBitmap(bitmap);

            }

            @Override
            public Bitmap getBitmap(String url) {
                return null;
            }
        });


        ImageLoader.ImageListener listener = ImageLoader.getImageListener(Img,
                R.mipmap.ic_launcher, R.mipmap.ic_launcher);

        imageLoader.get(Url, listener);
    }



    public static void GetImageByImageLoad(String Url, final ImageView Img,int DefaultImage,int ErrorImage) {

        if(!(Url.contains("http://") || Url.contains("https://")))
        {
            Url = "http:" + Url;

        }

        try
        {

            String ReplaceURL = Url.substring(Url.lastIndexOf('/') + 1,Url.length());

            String EncodeURL = "";

            if (ReplaceURL != "")
            {
                EncodeURL = URLEncoder.encode(ReplaceURL,"utf-8");
            }

            Url = Url.replace(ReplaceURL,EncodeURL);


        }
        catch (IOException e)
        {
            System.out.print(e);
        }



        RequestQueue mQueue = AppController.getInstance().getRequestQueue();

        ImageLoader imageLoader = new ImageLoader(mQueue, new BitmapCache() {
            @Override
            public void putBitmap(String url, Bitmap bitmap) {

                Img.setImageBitmap(bitmap);

            }

            @Override
            public Bitmap getBitmap(String url) {
                return null;
            }
        });


        ImageLoader.ImageListener listener = ImageLoader.getImageListener(Img,
                DefaultImage, ErrorImage);

        imageLoader.get(Url, listener);
    }

    public static void GetUserPhoto(String WorkID,final ImageView Img) {

        String Url = "http://172.16.111.114/File/SDQA/Code/Admin/" + WorkID + ".jpg";

        RequestQueue mQueue = AppController.getInstance().getRequestQueue();


        ImageLoader imageLoader = new ImageLoader(mQueue, new BitmapCache() {
            @Override
            public void putBitmap(String url, Bitmap bitmap) {

                Img.setImageBitmap(bitmap);

            }

            @Override
            public Bitmap getBitmap(String url) {
                return null;
            }
        });


        ImageLoader.ImageListener listener = ImageLoader.getImageListener(Img,
                R.mipmap.avatar_man, R.mipmap.avatar_man);

        imageLoader.get(Url, listener);

    }

    public interface VolleyCallback {

        void onSuccess(JSONObject result);

    }

    public interface VolleyImageCallback {

        void onSuccess(Bitmap ImgBitMap);
    }

    public interface VolleyStringCallback {

        void onSendRequestSuccess(String response);
    }

    public static void uploadImage(String UPLOAD_URL, RequestQueue mQueue, File file, String stringPart){

        MultipartRequest MultiPart = new MultipartRequest(UPLOAD_URL,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("TestJsonObj");
                        System.out.println(error);
                    }
                },
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Success?"+response);
                    }
                },file,"");


        mQueue.add(MultiPart);
    }

    public static void UploadImageVolley(String UPLOAD_URL,File file,RequestQueue mQueue,Context mContext)
    {
        //Auth header
        Map<String, String> mHeaderPart= new HashMap<>();
        mHeaderPart.put("Content-Type", "multipart/form-data;");
        //mHeaderPart.put("access_token", accessToken);

//File part
        Map<String, File> mFilePartData= new HashMap<>();

        mFilePartData.put("Files", file);
        //mFilePartData.put("file", new File(mFilePath));

//String part
        Map<String, String> mStringPart= new HashMap<>();
//        mStringPart.put("profile_id","1");
//        mStringPart.put("imageType", "ProfileImage");

        CustomMultipartRequest mCustomRequest = new CustomMultipartRequest(Request.Method.POST, mContext, UPLOAD_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                //listener.onResponse(jsonObject);
                //System.out.println(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //listener.onErrorResponse(volleyError);
                System.out.println(volleyError);
            }
        }, mFilePartData, mStringPart, mHeaderPart);

        mQueue.add(mCustomRequest);
    }



    public static String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public static void UploadFile(String FilePath,String Uploadurl){
        try {
            // Set your file path here
            FileInputStream fstrm = new FileInputStream(FilePath);
            //System.out.println(FilePath);
            // Set your server page url (and the file title/description)
            HttpFileUpload hfu = new HttpFileUpload(Uploadurl, "Test","Test");

            hfu.Send_Now(fstrm);

        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }

    public static void uploadImageByte(String UPLOAD_URL, RequestQueue mQueue,final Bitmap bitmap,final String FileName){
        //Showing the progress dialog
        //final ProgressDialog loading = ProgressDialog.show(,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                                //System.out.println(s);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {

                        } else if (error instanceof AuthFailureError) {
                            System.out.println("1");
                        } else if (error instanceof ServerError) {
                            System.out.println("2");
                        } else if (error instanceof NetworkError) {
                            System.out.println("3");
                        } else if (error instanceof ParseError) {
                            System.out.println("4");
                        }
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);
                //System.out.println(image);
                //Getting Image Name
                String name = FileName;

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("Data", image);
                params.put("FileName", name);

                //returning parameters
                return params;
            }
        };

        mQueue.add(stringRequest);
    }


    /**
     * 獲取軟件版本號
     *
     * @param context
     * @return
     */
    private static int getVersionCode(Context context)
    {
        int versionCode = 0;
        try
        {
            // 獲取軟件版本號，對應AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo("com.example.yujhaochen.ims", 0).versionCode;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static void isUpdate(final Context context)
    {

        RequestQueue mQueue = Volley.newRequestQueue(context);

        String Path = GetServiceData.ServicePath + "/Get_Android_Version";

        GetServiceData.getString(Path, mQueue, new GetServiceData.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {

                try {

                    JSONArray UserArray = new JSONArray(result.getString("Key"));

                    if (UserArray.length() > 0) {

                        UpdateManager mUpdateManager;

                        int versionCode = getVersionCode(context);

                        JSONObject IssueData = UserArray.getJSONObject(0);

                        String Version = String.valueOf(IssueData.getInt("Version"));


                        if (Version != String.valueOf(versionCode))
                        {
                            mUpdateManager = new UpdateManager(context);

                            mUpdateManager.checkUpdateInfo();
                        }

                    }
                } catch (JSONException ex) {

                }

            }
        });

        //return VersionCheck;
    }
}








