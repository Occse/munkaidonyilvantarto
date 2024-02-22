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
        boolean isMiamiTheme = BaseActivity.checkTheme();
        if (isMiamiTheme) {
            return new viewHolderWorker(LayoutInflater.from(nContext).inflate(R.layout.list_workersmiami, parent, false));
        } else {
            return new viewHolderWorker(LayoutInflater.from(nContext).inflate(R.layout.list_workers, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolderWorker holder, int position) {
        WorkerData currentItem = mWorkerDataData.get(position);

        holder.bindToManagment(currentItem);

        if (holder.getBindingAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(nContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getBindingAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mWorkerDataData.size();
    }

    class viewHolderWorker extends RecyclerView.ViewHolder {
        private final TextView mUserName;
        private final TextView mEmail;
        private final TextView mUserAdo;
        private final TextView mUserDegree;
        private final TextView mUserBirthDate;
        private final TextView mUserId;
        private final TextView mUserLakcim;
        private final TextView mUserTAJ;
        private final TextView mUserMunkakor;

        viewHolderWorker(@NonNull View workerView) {
            super(workerView);
            mUserName = workerView.findViewById(R.id.workerName);
            mEmail = workerView.findViewById(R.id.workerEmail);
            mUserId = workerView.findViewById(R.id.userId);
            mUserLakcim = workerView.findViewById(R.id.userLakcimNumber);
            mUserAdo = workerView.findViewById(R.id.userAdoKartya);
            mUserTAJ = workerView.findViewById(R.id.userTAJNumber);
            mUserDegree = workerView.findViewById(R.id.userDegree);
            mUserBirthDate = workerView.findViewById(R.id.userBirthDate);
            mUserMunkakor = workerView.findViewById(R.id.userMunkakor);
        }

        public void bindToManagment(WorkerData currentWorker) {
            mUserName.setText(currentWorker.getUserName());
            mEmail.setText(currentWorker.getEmail());
            mUserId.setText(currentWorker.getUserId());
            mUserLakcim.setText(currentWorker.getUserLakcim());
            mUserAdo.setText(currentWorker.getUserAdo());
            mUserTAJ.setText(currentWorker.getUserTAJ());
            mUserDegree.setText(currentWorker.getUserDegree());
            mUserBirthDate.setText(currentWorker.getUserBirthDate());
            mUserMunkakor.setText(currentWorker.getUserMunkakor());
            itemView.findViewById(R.id.checkWorker)
                    .setOnClickListener(view -> ((ManageWorkersActivity) nContext).checkWorkerHours(currentWorker));
            itemView.findViewById(R.id.kickWorker)
                    .setOnClickListener(view -> ((ManageWorkersActivity) nContext).kickWorkerDialog(currentWorker));
        }
    }
}