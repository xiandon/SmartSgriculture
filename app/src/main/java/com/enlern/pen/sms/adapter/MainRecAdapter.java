package com.enlern.pen.sms.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.enlern.pen.sms.R;
import com.xiandon.wsn.node.NodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pen on 2017/9/14.
 */

public class MainRecAdapter extends RecyclerView.Adapter {
    private static final String TAG = "MainRecAdapter";
    private Context mContext;
    private List<NodeInfo> nodeInfos = new ArrayList<>();
    private LayoutInflater layoutInflater;


    public MainRecAdapter(Context context) {
        this.mContext = context;
        layoutInflater = LayoutInflater.from(context);
    }


    public void addData(int count, NodeInfo nodeInfo) {
        nodeInfos.add(count, nodeInfo);
        notifyItemInserted(count);
        notifyDataSetChanged();

        if (nodeInfos.size() > 100) {
            clearData();
        }
    }

    public void clearData() {
        nodeInfos.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.layout_show_item, parent, false);
        RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view) {
        };
        return holder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        TextView tv_show_num = holder.itemView.findViewById(R.id.tv_show_num);
        TextView tv_show_name = holder.itemView.findViewById(R.id.tv_show_name);
        TextView tv_show_status = holder.itemView.findViewById(R.id.tv_show_status);
        TextView tv_show_data = holder.itemView.findViewById(R.id.tv_show_data);
        TextView tv_show_wsn = holder.itemView.findViewById(R.id.tv_show_wsn);

        tv_show_num.setText(nodeInfos.get(position).getNode_num() + "");
        tv_show_name.setText(nodeInfos.get(position).getNode_name() + "");
        tv_show_status.setText(nodeInfos.get(position).getNode_data() + "");
        tv_show_data.setText(nodeInfos.get(position).getData_analysis() + "");
        tv_show_wsn.setText(nodeInfos.get(position).getWsn() + "");

    }

    @Override
    public int getItemCount() {
        return nodeInfos.size();
    }


}
