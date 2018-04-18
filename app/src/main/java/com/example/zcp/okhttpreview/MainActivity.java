package com.example.zcp.okhttpreview;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //设缓存有效期为1天
    long CACHE_STALE_SEC = 60 * 60 * 24 * 1;
    //查询缓存的Cache-Control设置，为if-only-cache时只查询缓存而不会请求服务器，max-stale可以配合设置缓存失效时间
    String CACHE_CONTROL_CACHE = "only-if-cached, max-stale=" + CACHE_STALE_SEC;
    //(假如请求了服务器并在a时刻返回响应结果，则在max-age规定的秒数内，浏览器将不会发送对应的请求到服务器，数据由缓存直接返回)
    String CACHE_CONTROL_NETWORK = "Cache-Control: public, max-age=3600";
    private Cache cache;
    private Interceptor cacheInterceptor;
    private Interceptor logInterceptor;
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBuilder();
        initView();
    }

    private void initBuilder() {
        //缓存目录
        cache = new Cache(new File(this.getCacheDir(),"test_cache"),1024*1024*1024);
        //配置缓存策略
        cacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                request = request.newBuilder().header("Cache-Control","public, max-age=3600").build();
                if(!isNetworkAvailable(MainActivity.this)){
                    request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                }
                Response response = chain.proceed(request);
                String cacheControl = request.cacheControl().toString();
                if(isNetworkAvailable(MainActivity.this)){
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
        //缓存配置
//设置拦截器 可以对请求和响应做处理，比如缓存配置，打印响应流
//同上
//开启失败重连
//                        .authenticator()响应服务器的身份验证，可以为NULL
//                        .certificatePinner()  配置证书
//                        .connectionPool() 配置连接复用池 ， 默认5个 5分钟
//                        .connectionSpecs() 链接策略。默认包含一个TLS链接和一个未加密的链接
//                        .cookieJar() cookie 管理 默认为null
//                        .dispatcher()异步请求时的策略 默认已经初始化
//                        .dns()查找主机名IP地址的DNS服务 默认使用框架自带的DNS，也可以自己实现
//                        .followRedirects() 设置是否可以重定向 默认开启
//                        .followSslRedirects()从HTTPS到HTTP和从HTTP到HTTPS的重定向，默认遵循协议重定向
//                        .hostnameVerifier()HTTPS链接请求主机名与响应证书的验证器
//                        .pingInterval()设置此客户机发起的HTTP/2和web套接字ping之间的间隔。用它来自动发送ping帧，
//                                        直到连接失败或关闭。这使连接存在，并可检测连接性故障。
//                        .protocols()客户端配置的协议集合 默认为http/1.1和h2的集合
//                        .proxy()  HTTP代理
//                        .readTimeout()设置新连接的默认读取超时。值0表示没有超时，否则。当转换为毫秒时，值必须在1到Integer.MAX_VALUE之间。
//                        .socketFactory() 默认使用系统的套接字工厂
//                        .sslSocketFactory() 同上
//                        .writeTimeout(10, TimeUnit.SECONDS)设置新连接的默认写入超时。值0表示没有超时，否则。当转换为毫秒时，值必须在1到Integer.MAX_VALUE之间。
        okHttpClient = new OkHttpClient.Builder()
                //缓存配置
                .cache(cache)
                .addInterceptor(cacheInterceptor)//设置拦截器 可以对请求和响应做处理，比如缓存配置，打印响应流
                .addNetworkInterceptor(cacheInterceptor)//同上
                .addInterceptor(logInterceptor)
                .retryOnConnectionFailure(true)//开启失败重连
//                        .authenticator()响应服务器的身份验证，可以为NULL
//                        .certificatePinner()  配置证书
//                        .connectionPool() 配置连接复用池 ， 默认5个 5分钟
//                        .connectionSpecs() 链接策略。默认包含一个TLS链接和一个未加密的链接
//                        .cookieJar() cookie 管理 默认为null
//                        .dispatcher()异步请求时的策略 默认已经初始化
//                        .dns()查找主机名IP地址的DNS服务 默认使用框架自带的DNS，也可以自己实现
//                        .followRedirects() 设置是否可以重定向 默认开启
//                        .followSslRedirects()从HTTPS到HTTP和从HTTP到HTTPS的重定向，默认遵循协议重定向
//                        .hostnameVerifier()HTTPS链接请求主机名与响应证书的验证器
//                        .pingInterval()设置此客户机发起的HTTP/2和web套接字ping之间的间隔。用它来自动发送ping帧，
//                                        直到连接失败或关闭。这使连接存在，并可检测连接性故障。
//                        .protocols()客户端配置的协议集合 默认为http/1.1和h2的集合
//                        .proxy()  HTTP代理
//                        .readTimeout()设置新连接的默认读取超时。值0表示没有超时，否则。当转换为毫秒时，值必须在1到Integer.MAX_VALUE之间。
//                        .socketFactory() 默认使用系统的套接字工厂
//                        .sslSocketFactory() 同上
//                        .writeTimeout(10, TimeUnit.SECONDS)设置新连接的默认写入超时。值0表示没有超时，否则。当转换为毫秒时，值必须在1到Integer.MAX_VALUE之间。
                .build();
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

    private void initView() {

        Button btRequestHttp = (Button) findViewById(R.id.bt_request);
        Button btClear = (Button) findViewById(R.id.bt_clear);
        final TextView tvShow = (TextView) findViewById(R.id.tv_show);
        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvShow.setText("");
            }
        });
        btRequestHttp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request request = new Request.Builder()
                        .url("https://blog.csdn.net/zcpHappy/article/details/79719141")//可以是string字符串，也可以是URL 或是一个HttpUrl
//                        .addHeader()添加请求头
                        .header("User-Agent", "oktest") //同上 方式不一样
//                        .header("Cache-Control","public, max-age=3600")
//                        .headers()同上 方式不一样
//                        .cacheControl()配置请求时的缓存指令
//                        .delete() 请求时的方法
//                        .delete(new RequestBody() {
//                            @Nullable
//                            @Override
//                            public MediaType contentType() {
//                                return null;
//                            }
//
//                            @Override
//                            public void writeTo(BufferedSink sink) throws IOException {
//
//                            }
//                        })  同上
                        .get() //同上
//                        .head() //同上
//                        .method() //同上 自己配置请求方法 和 请求体
//                        .patch()同上
//                        .post()同上
//                        .put()同上
//                        .removeHeader() 移除请求头
//                        .tag() 给此请求配置标记，通过此标记可以取消此请求
                        .build();
                okHttpClient.newCall(request)
//                        .execute()//同步请求
                        .enqueue(new Callback() { //异步请求  有回调
                            @Override
                            public void onFailure(Call call, final IOException e) {
                                //请求失败回调方法
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvShow.setText(e.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                //请求成功回掉方法
//                                String responseString = response.body().string();
                                InputStream inputStream = response.body().byteStream();
                                final StringBuilder stringBuilder = new StringBuilder();
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                                String  line;
                                try{
                                    while ((line = bufferedReader.readLine())!=null){
                                        stringBuilder.append(line+"\n");
                                    }

                                }catch (Exception e){
                                    e.printStackTrace();
                                }finally {
                                    try {
                                        inputStream.close();
                                    }catch (Exception eInput){
                                        eInput.printStackTrace();
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvShow.setText(stringBuilder.toString());
                                    }
                                });
                            }
                        });
            }
        });
    }
}
