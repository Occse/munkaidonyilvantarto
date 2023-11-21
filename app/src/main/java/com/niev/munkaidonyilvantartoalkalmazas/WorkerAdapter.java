package com.niev.munkaidonyilvantartoalkalmazas;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.viewHolderWorker> {
    private final ArrayList<WorkerData> mWorkerDataData;
    private final Context nContext;
    private int lastPosition = -1;

    WorkerAdapter(Context context, ArrayList<WorkerData> workerData) {
        this.mWorkerDataData = workerData;
        nContext = context;
    }

    @NonNull
    @Override
    public viewHolderWorker onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new viewHolderWorker(LayoutInflater.from(nContext).inflate(R.layout.workers, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolderWorker holder, int position) {
        WorkerData currentItem = mWorkerDataData.get(position);

        holder.bindToManagment(currentItem);

        if (holder.getAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(nContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mWorkerDataData.size();
    }

    class viewHolderWorker extends RecyclerView.ViewHolder {
        private TextView mUserName;
        private TextView mWorkerImage;
        private TextView mEmail;
        private TextView mUserAdo;
        private TextView mUserDegree;
        private TextView mUserBirthDate;
        private TextView mUserId;
        private TextView mUserLakcim;
        private TextView mUserTAJ;
        private TextView mUserMunkakor;

        viewHolderWorker(@NonNull View itemView) {
            super(itemView);
            mUserName = itemView.findViewById(R.id.workerName);
            mEmail = itemView.findViewById(R.id.workerEmail);
            mUserId = itemView.findViewById(R.id.userId);
            mUserLakcim = itemView.findViewById(R.id.userLakcimNumber);
            mUserAdo = itemView.findViewById(R.id.userAdoKartya);
            mUserTAJ = itemView.findViewById(R.id.userTAJNumber);
            mUserDegree = itemView.findViewById(R.id.userDegree);
            mUserBirthDate = itemView.findViewById(R.id.userBirthDate);
            mUserMunkakor = itemView.findViewById(R.id.userMunkakor);
        }

        public void bindToManagment(WorkerData currentItem) {
            mUserName.setText(currentItem.getUserName());
            mEmail.setText(currentItem.getEmail());
            mUserId.setText(currentItem.getUserId());
            mUserLakcim.setText(currentItem.getUserLakcim());
            mUserAdo.setText(currentItem.getUserAdo());
            mUserTAJ.setText(currentItem.getUserTAJ());
            mUserDegree.setText(currentItem.getUserDegree());
            mUserBirthDate.setText(currentItem.getUserBirthDate());
            mUserMunkakor.setText(currentItem.getUserMunkakor());
        }
    }
}