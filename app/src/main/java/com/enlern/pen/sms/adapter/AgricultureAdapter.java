package com.enlern.pen.sms.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.enlern.pen.sms.R;
import com.xiandon.wsn.node.NodeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pen on 2017/9/14.
 */

public class AgricultureAdapter extends RecyclerView.Adapter {
    private static final String TAG = "AgricultureAdapter";
    private Context mContext;
    private List<NodeInfo> nodeInfos = new ArrayList<>();
    private LayoutInflater layoutInflater;

    private TextView tv_show_name;
    private TextView tv_show_num;
    private TextView tv_show_data;
    private TextView tv_agriculture_chip;
    private TextView tv_agriculture_frame_num;
    private TextView tv_agriculture_sys_num;


    public AgricultureAdapter(Context context) {
        this.mContext = context;
        layoutInflater = LayoutInflater.from(context);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.layout_node, parent, false);
        RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view) {
        };
        return holder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        tv_show_name = holder.itemView.findViewById(R.id.tv_agriculture_name);
        tv_show_num = holder.itemView.findViewById(R.id.tv_agriculture_num);
        tv_show_data = holder.itemView.findViewById(R.id.tv_agriculture_data);
        tv_agriculture_chip = holder.itemView.findViewById(R.id.tv_agriculture_chip);
        tv_agriculture_frame_num = holder.itemView.findViewById(R.id.tv_agriculture_frame_num);
        tv_agriculture_sys_num = holder.itemView.findViewById(R.id.tv_agriculture_sys_num);


        Drawable bottom = null;
        switch (nodeInfos.get(position).getNode_num()) {
            case "0080":
                bottom = mContext.getResources().getDrawable(R.drawable.if_water_level);
                setData(nodeInfos.get(position), bottom);
                break;
            case "0082":
                bottom = mContext.getResources().getDrawable(R.drawable.if_wind_wheel);
                setData(nodeInfos.get(position), bottom);
                break;
            case "000c":
                bottom = mContext.getResources().getDrawable(R.drawable.if_smoking);
                setData(nodeInfos.get(position), bottom);
                break;
            case "0087":
                bottom = mContext.getResources().getDrawable(R.drawable.if_water_z);
                setData(nodeInfos.get(position), bottom);
                break;
            case "0007":
                bottom = mContext.getResources().getDrawable(R.drawable.if_rainning);
                setData(nodeInfos.get(position), bottom);
                break;
            case "0081":
                bottom = mContext.getResources().getDrawable(R.drawable.if_ph);
                setData(nodeInfos.get(position), bottom);
                break;
            case "0088":
                bottom = mContext.getResources().getDrawable(R.drawable.if_bucket_water);
                setData(nodeInfos.get(position), bottom);
                break;
            case "0083":
                bottom = mContext.getResources().getDrawable(R.drawable.if_soil_small);
                setData(nodeInfos.get(position), bottom);
                break;
            case "0089":
                bottom = mContext.getResources().getDrawable(R.drawable.if_co_small);
                setData(nodeInfos.get(position), bottom);
                break;
            case "0086":
                bottom = mContext.getResources().getDrawable(R.drawable.if_air_small);
                setData(nodeInfos.get(position), bottom);
                break;
            case "0001":
                bottom = mContext.getResources().getDrawable(R.drawable.if_sun_small);
                setData(nodeInfos.get(position), bottom);
                break;

        }


    }

    private void setData(NodeInfo info, Drawable bottom) {
        tv_show_name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, bottom);
        tv_show_name.setText(info.getNode_name());
        tv_show_num.setText(info.getNode_num());
        tv_show_data.setText(info.getData_analysis());
        tv_agriculture_chip.setText(info.getChip_type());
        tv_agriculture_frame_num.setText(info.getFrame_num());
        tv_agriculture_sys_num.setText(info.getSys_board());
    }


    @Override
    public int getItemCount() {
        return nodeInfos.size();
    }

    public void update(NodeInfo info) {

        boolean bAgri = checkAgri(info.getNode_num());


        if (!bAgri) {
            return;
        }

        int in = nodeInfos.size();

        boolean b = false;
        for (int i = 0; i < in; i++) {
            if (nodeInfos.get(i).getNode_num().equals(info.getNode_num())) {
                b = true;
                in = i;
            } else {
                b = false;
            }
        }
        if (b) {
            nodeInfos.set(in, info);
            notifyItemChanged(in);
        } else {
            addData(info);
        }
    }

    private boolean checkAgri(String node_num) {
        String strings[] = {"0082", "000c", "0080", "0081", "0087", "0088", "0007", "0083", "0089", "0086", "0001"};
        List<String> list = Arrays.asList(strings);
        if (list.contains(node_num)) {
            return true;
        } else {
            return false;
        }
    }


    private void addData(NodeInfo info) {

        int in = nodeInfos.size();
        for (int i = 0; i < in; i++) {
            if (nodeInfos.get(i).getNode_num().equals(info.getNode_num())) {
                in = i;
                if (i > 1) {
                    removeData(i);
                    notifyItemChanged(i);
                }
            }
        }
        nodeInfos.add(in, info);
        notifyItemInserted(in);
        notifyDataSetChanged();
    }


    private void removeData(int position) {
        //保证列表有数据，并且最少有一条
        if (nodeInfos.size() < 2 && nodeInfos.size() != 0) {
            nodeInfos.remove(0);
            notifyDataSetChanged();
        } else if (nodeInfos.size() == 0) {//当列表没有数据提示用户，免得造成系统崩溃
            Toast.makeText(mContext, "没数据了", Toast.LENGTH_SHORT).show();
        } else {//更新列表
            nodeInfos.remove(position);
            notifyDataSetChanged();
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, nodeInfos.size());
        }

    }

    public void clearData() {
        nodeInfos.clear();
        notifyDataSetChanged();
    }
}
