package net;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Zhao Chenping
 * @creat 2018/4/18.
 * @description
 */

public class RetrofitClient {

    private static OkHttpClient okHttpClient;

    public static void initNet(Context context ,Cache cache, Interceptor interceptor, Interceptor netInterceptor){
        if(cache==null){
            //缓存目录
            cache = new Cache(new File(context.getCacheDir(),"cache"),1024*1024*1024);
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        if(interceptor!=null){
            builder.addInterceptor(interceptor);
        }
        if(netInterceptor!=null){
            builder.addNetworkInterceptor(netInterceptor);
        }
        okHttpClient = builder.build();
    }
    public static void initNet(OkHttpClient client){
        okHttpClient = client;
    }

    /**
     * @param
     * @describe Retrofit框架原始网络请求服务
     * @author happyGhost
     * @time 2018/4/18  15:44
     */
    public static <T> T getInstanceStringUrl(String url,Class<T> clazz){
        Retrofit  retrofit = new Retrofit.Builder()
//                .client()//配置OKhttp客户端
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
//                .validateEagerly()  在Call时，是否立即验证接口中的方法配置
//                .callbackExecutor() //回调
                .build();
        T service = retrofit.create(clazz);
        return service;
    }
    public static <T> T getInstanceHttpUrl(HttpUrl url, Class<T> clazz){
        Retrofit  retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        T service = retrofit.create(clazz);
        return service;
    }
    public static <T> T getOkhttpClientServiceStringUrl(String url,Class<T> tClass){
        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        T service = retrofit.create(tClass);
        return service;
    }
    public static<T> T getOkhttpClientServiceHttpurl(HttpUrl url,Class<T> tClass){
        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        T service = retrofit.create(tClass);
        return service;
    }
}
//    /**
//     * 获取美女图片
//     * http://image.baidu.com/data/imgs?sort=0&pn=0&rn=20&col=美女&tag3=&p=channel&from=1
//     */
//Call<BeautyPicture> photoCall = service.getWelfarePhotoCall(0, startImage, size, col, tag, "", "channel", 1);
//        photoCall.enqueue(callback);
//    fun getBeautyPicture(startImage:Int,size: Int,col:String,tag:String):Observable<BeautyPicture>{
//        return iBeautyApi!!.getWelfarePhoto(0,startImage,size,col,tag,"","channel",1)
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        }