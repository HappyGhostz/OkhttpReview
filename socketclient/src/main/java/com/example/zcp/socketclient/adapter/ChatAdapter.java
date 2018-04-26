package com.example.zcp.socketclient.adapter;

import android.graphics.BitmapFactory;

import com.chad.library.adapter.base.BaseViewHolder;
import com.example.zcp.socketclient.Constants;
import com.example.zcp.socketclient.R;
import com.example.zcp.socketclient.bean.Transmission;

import java.util.List;

/**
 * @author Zhao Chenping
 * @creat 2018/4/26.
 * @description
 */
public class ChatAdapter extends BaseMultiItemQuickAdapter<Transmission, BaseViewHolder> {

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public ChatAdapter(List<Transmission> data) {
        super(data);

        addItemType(Constants.CHAT_FROM, R.layout.chat_from_msg);
        addItemType(Constants.CHAT_SEND, R.layout.chat_send_msg);
    }

    @Override
    protected void convert(BaseViewHolder helper, Transmission item) {

        switch (item.itemType) {
            case Constants.CHAT_FROM:
                helper.setText(R.id.chat_from_content, item.content)
                        .setText(R.id.chat_from_time,item.time);
                break;
            case Constants.CHAT_SEND:
                if (item.showType == 1) {
                    helper.setVisible(R.id.chat_send_content, false);
                    helper.setText(R.id.chat_send_time,item.time);
                } else {
                    helper.setVisible(R.id.chat_send_content, true);
                    helper.setText(R.id.chat_send_content, item.content)
                            .setText(R.id.chat_send_time,item.time);
                }
                break;
            default:
        }

    }
}

