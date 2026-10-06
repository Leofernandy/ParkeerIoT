package com.example.parkeeriotapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.parkeeriotapp.model.WalletHistory;

import java.util.ArrayList;

public class WalletHistoryAdapter extends BaseAdapter {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Context context;
    private ArrayList<WalletHistory> list;
    private LayoutInflater inflater;

    public WalletHistoryAdapter(Context context, ArrayList<WalletHistory> list) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public boolean isEnabled(int position) {
        // Header tidak bisa di-klik
        return !list.get(position).isHeader();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        WalletHistory history = list.get(i);
        int viewType = getItemViewType(i);

        if (viewType == TYPE_HEADER) {
            HeaderViewHolder headerHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_wallet_header, viewGroup, false);
                headerHolder = new HeaderViewHolder();
                headerHolder.txtHeaderDate = convertView.findViewById(R.id.txtHeaderDate);
                convertView.setTag(headerHolder);
            } else {
                headerHolder = (HeaderViewHolder) convertView.getTag();
            }
            headerHolder.txtHeaderDate.setText(history.getHeaderDate());
            return convertView;
        } else {
            ItemViewHolder itemHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_wallet_history, viewGroup, false);
                itemHolder = new ItemViewHolder();
                itemHolder.txtTitle = convertView.findViewById(R.id.txtTitle);
                itemHolder.txtSubtitle = convertView.findViewById(R.id.txtSubtitle);
                itemHolder.txtAmount = convertView.findViewById(R.id.txtAmount);
                convertView.setTag(itemHolder);
            } else {
                itemHolder = (ItemViewHolder) convertView.getTag();
            }

            itemHolder.txtTitle.setText(history.getTitle());
            itemHolder.txtSubtitle.setText(history.getSubtitle());
            itemHolder.txtAmount.setText(history.getAmount());

            // Warna nominal (Hijau untuk top-up/refund & bayar sesuai screenshot)
            if (history.getType() == 1 || history.getType() == 3) {
                itemHolder.txtAmount.setTextColor(Color.parseColor("#16A34A")); // Hijau
            } else {
                itemHolder.txtAmount.setTextColor(Color.parseColor("#16A34A")); // Hijau sesuai screenshot
            }

            return convertView;
        }
    }

    private static class HeaderViewHolder {
        TextView txtHeaderDate;
    }

    private static class ItemViewHolder {
        TextView txtTitle;
        TextView txtSubtitle;
        TextView txtAmount;
    }
}