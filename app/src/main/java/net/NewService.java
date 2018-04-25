package net;

import com.example.zcp.okhttpreview.data.BeautyPicture;

import java.util.HashMap;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.example.zcp.okhttpreview.MainActivity.CACHE_CONTROL_NETWORK;

/**
 * @author Zhao Chenping
 * @creat 2018/4/18.
 * @description
 */

public interface NewService {
    /**
     * 获取美女图片
     * API获取途径http://www.jb51.net/article/61266.htm
     * eg: http://image.baidu.com/data/imgs?sort=0&pn=0&rn=20&col=美女&tag=全部&tag3=&p=channel&from=1
     * 通过分析，推断并验证了其中字段的含义，col表示频道，tag表示的是全部的美女，也可以是其他Tag，pn表示从第几张图片开始，rn表示获取多少张
     * @param
     * @return
     */
    @Headers(CACHE_CONTROL_NETWORK)
    @GET("imgs")
    Observable<BeautyPicture> getWelfarePhoto(@Query("sort") int sort,
                                              @Query("pn")int startImage,
                                              @Query("rn")int size,
                                              @Query("col")String col,
                                              @Query("tag")String tag,
                                              @Query("tag3")String tag3,
                                              @Query("p")String channel,
                                              @Query("from")int from);
    @Headers(CACHE_CONTROL_NETWORK)
    @GET("imgs")
    Call<BeautyPicture> getWelfarePhotoCall(@Query("sort") int sort,
                                            @Query("pn")int startImage,
                                            @Query("rn")int size,
                                            @Query("col")String col,
                                            @Query("tag")String tag,
                                            @Query("tag3")String tag3,
                                            @Query("p")String channel,
                                            @Query("from")int from);

}
