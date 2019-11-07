package com.example.thoughtit02;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecyclerViewAdaptor extends RecyclerView.Adapter<RecyclerViewAdaptor.ViewHolder>{

    private static final String TAG = "RecyclerViewAdaptor";
    private List<String> mThoughts = new ArrayList<>();
    private List<Date> mDates = new ArrayList<>();
    private Context mContext;


    public RecyclerViewAdaptor(Context mContext, List<String> mThoughts, List<Date> mDates) {
        this.mThoughts = mThoughts;
        this.mDates = mDates;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        holder.thought.setText(mThoughts.get(position));
        holder.date.setText("Date of Thought: "+mDates.get(position).toString());


    }

    @Override
    public int getItemCount() {
        return mThoughts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView thought;
        TextView date;
        RelativeLayout parentLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.thought = itemView.findViewById(R.id.thought);
            this.date = itemView.findViewById(R.id.date);
            this.parentLayout = itemView.findViewById(R.id.parent_layout);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO Auto-generated method
                    Log.d("DEBUG", "pos "+ getAdapterPosition() + " getItemId ");
                    Toast.makeText(mContext, "Removed "+mThoughts.get(getAdapterPosition())+" p:"+ getAdapterPosition()
                            , Toast.LENGTH_SHORT).show();
                    // Here we can use to show dialog.
                    ((MainActivity)mContext).removeThought(getAdapterPosition());

                    return true;
                }

            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method
                    Log.d("DEBUG", "pos "+ getAdapterPosition() + " getItemId ");
                    Toast.makeText(mContext, "To remove item. Long press the " +
                            "thought to remove.", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
}
