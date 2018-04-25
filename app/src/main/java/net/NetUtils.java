package net;

import com.example.zcp.okhttpreview.data.BeautyPicture;
import com.trello.rxlifecycle2.components.RxActivity;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Zhao Chenping
 * @creat 2018/4/18.
 * @description
 */

public class NetUtils {


    private static ObservableTransformer<BeautyPicture,BeautyPicture> transformer = getSchedulers();

    public static Observable<BeautyPicture> getService(RxActivity activity){
        NewService newService = RetrofitClient.getOkhttpClientServiceStringUrl("http://image.baidu.com/data/", NewService.class);
        return newService.getWelfarePhoto(0,0,50,"美女","全部", "", "channel", 1)
                .compose(transformer)
                .compose(activity.<BeautyPicture>bindToLifecycle());
    }
    private static <T>ObservableTransformer<T,T> getSchedulers(){
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }


}
