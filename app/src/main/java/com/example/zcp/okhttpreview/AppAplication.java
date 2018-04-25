package com.example.zcp.okhttpreview;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import net.RetrofitClient;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Zhao Chenping
 * @creat 2018/4/18.
 * @description
 */

public class AppAplication extends Application {
    private Cache cache;
    private Interceptor cacheInterceptor;
    private Interceptor logInterceptor;
    //设缓存有效期为1天
    long CACHE_STALE_SEC = 60 * 60 * 24 * 1;
    //查询缓存的Cache-Control设置，为if-only-cache时只查询缓存而不会请求服务器，max-stale可以配合设置缓存失效时间
    public String CACHE_CONTROL_CACHE = "only-if-cached, max-stale=" + CACHE_STALE_SEC;
    //(假如请求了服务器并在a时刻返回响应结果，则在max-age规定的秒数内，浏览器将不会发送对应的请求到服务器，数据由缓存直接返回)
    public static final String CACHE_CONTROL_NETWORK = "Cache-Control: public, max-age=3600";

    @Override
    public void onCreate() {
        super.onCreate();
        initNet();
    }

    private void initNet() {
        //缓存目录
        cache = new Cache(new File(this.getCacheDir(),"test_cache"),1024*1024*1024);
        //配置缓存策略
        cacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                request = request.newBuilder().header("Cache-Control","public, max-age=3600").build();
                if(!isNetworkAvailable(AppAplication.this)){
                    request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                }
                Response response = chain.proceed(request);
                String cacheControl = request.cacheControl().toString();
                if(isNetworkAvailable(AppAplication.this)){
                    return response.newBuilder()
                            .header("Cache-Control",cacheControl)
                            .header("User-Agent","oktest")
                            .removeHeader("Pragma")
                            .build();
                }else {
                    return response.newBuilder()
                            .header("Cache-Control","public, " + CACHE_CONTROL_CACHE)
                            .header("User-Agent","oktest")
                            .removeHeader("Pragma")
                            .build();
                }
            }
        };
        //打印请求头 url；打印响应头 响应时间 响应体
        logInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                long t1 = System.nanoTime();
                Log.e("Sending request",request.url().toString());
                Log.e("request connection",chain.connection()+"");
                Log.e("request headers",request.headers().toString());
                Response response = chain.proceed(request);

                long t2 = System.nanoTime();
                Log.e("Received response for",response.request().url().toString());
                Log.e("response time",(t2 - t1) / 1e6d+"毫秒");
                Log.e("response headers",response.headers().toString());
                Log.e("response body",response.body().toString());
//                logger.info(String.format("Received response for %s in %.1fms%n%s%s",
//                        response.request().url(), (t2 - t1) / 1e6d, response.headers(),response.body().toString()));
                return response;
            }
        };
        RetrofitClient.initNet(this,cache,logInterceptor,cacheInterceptor);
    }
    /**
     * 判断网络是否可用
     */
    private boolean isNetworkAvailable(Context context){
        ConnectivityManager manager =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if(info!=null&&info.isConnected()){
            return  true;
        }
        return false;
    }
}
