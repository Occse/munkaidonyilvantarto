package com.niev.munkaidonyilvantartoalkalmazas;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class HourAdapter extends RecyclerView.Adapter<HourAdapter.viewHolderWorker> {
    private final ArrayList<HourData> mHourDataData;
    private final Context nContext;
    private int lastPosition = -1;

    HourAdapter(Context context, ArrayList<HourData> hourData) {
        this.mHourDataData = hourData;
        nContext = context;
    }

    @NonNull
    @Override
    public viewHolderWorker onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        boolean isMiamiTheme = BaseActivity.checkTheme();
        if (isMiamiTheme) {
            return new viewHolderWorker(LayoutInflater.from(nContext).inflate(R.layout.list_hoursmiami, parent, false));
        } else {
            return new viewHolderWorker(LayoutInflater.from(nContext).inflate(R.layout.list_hours, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolderWorker holder, int position) {
        HourData currentItem = mHourDataData.get(position);

        holder.bindToManagment(currentItem);

        if (holder.getBindingAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(nContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getBindingAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mHourDataData.size();
    }

    class viewHolderWorker extends RecyclerView.ViewHolder {
        private final TextView mLunchStart;
        private final TextView mLunchEnd;
        private final TextView mWorkDay;
        private final TextView mWorkStart;
        private final TextView mWorkEnd;
        private final TextView mWorkTime;

        viewHolderWorker(@NonNull View hourView) {
            super(hourView);
            mWorkDay = hourView.findViewById(R.id.workDay);
            mWorkStart = hourView.findViewById(R.id.workStart);
            mLunchStart = hourView.findViewById(R.id.lunchStart);
            mLunchEnd = hourView.findViewById(R.id.lunchEnd);
            mWorkEnd = hourView.findViewById(R.id.workEnd);
            mWorkTime = hourView.findViewById(R.id.workHours);
        }

        public void bindToManagment(@NonNull HourData currentWorkerHour) {
            mWorkDay.setText(currentWorkerHour.getWorkDay("full"));
            mWorkStart.setText(currentWorkerHour.getWorkStart());
            mLunchStart.setText(currentWorkerHour.getLunchStart());
            mLunchEnd.setText(currentWorkerHour.getLunchEnd());
            mWorkEnd.setText(currentWorkerHour.getWorkEnd());
            Object hourObject = currentWorkerHour.getWorkedHours();
            long hourLong = -1;
            double hourDouble = -1;
            if (hourObject instanceof Long) {
                hourLong = (long) hourObject;
            } else {
                hourDouble = (double) hourObject;
            }
            BigDecimal hourFormat = BigDecimal.valueOf(hourLong == -1 ? hourDouble : hourLong);
            BigDecimal formattedHour = hourFormat.setScale(1, RoundingMode.HALF_UP);
            mWorkTime.setText(formattedHour + " óra");
        }
    }
}
