package de.visualfan.picchat.ui;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.visualfan.picchat.R;
import de.visualfan.picchat.databinding.MessageItemBinding;
import de.visualfan.picchat.model.Message;

/**
 * Created by Berenice on 23.01.18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final static String TAG = "Adapter";
    private List<? extends Message> mMessageList ;
    private final MessageAdapterOnClickHandler mMessageClickHandler;


    public interface MessageAdapterOnClickHandler{
        void onClick(View view, int position, Message message);
    }



    MessageAdapter(@Nullable MessageAdapterOnClickHandler clickCallback) {
        this.mMessageClickHandler = clickCallback;
    }


    void setMessageList(final List<? extends Message> messageList){
        mMessageList = messageList;
        notifyDataSetChanged();
    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MessageItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.message_item,
                        parent,
                        false);

        return new MessageViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = mMessageList.get(position);
        holder.binding.setMessage(mMessageList.get(position));
        //holder.binding.name.setText(message.getUserName());
        //holder.binding.tvDescription.setText(message.getDescription());
        if(message.getPhotoUrl() != null && !message.getPhotoUrl().isEmpty())
            Glide.with(holder.binding.photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .thumbnail(0.01f)
                    .into(holder.binding.photoImageView);
        holder.binding.executePendingBindings();
    }



    @Override
    public int getItemCount() {
        return mMessageList == null ? 0 : mMessageList.size();
    }


    // inner class ViewHolder
    class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final MessageItemBinding binding;

        public MessageViewHolder(MessageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.cardview.setOnClickListener(this);
            //this.binding.tvDescription.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Log.d(TAG, "onClick at adapter position = " + adapterPosition);
            mMessageClickHandler.onClick(v, adapterPosition, mMessageList.get(adapterPosition));
        }

    }
}
