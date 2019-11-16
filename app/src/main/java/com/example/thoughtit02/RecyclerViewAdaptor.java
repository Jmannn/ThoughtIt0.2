package com.example.thoughtit02;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecyclerViewAdaptor extends RecyclerView.Adapter<RecyclerViewAdaptor.ViewHolder>{

    private static final String TAG = "RecyclerViewAdaptor";
    private List<String> mThoughts = new ArrayList<>();
    private List<Date> mDates = new ArrayList<>();

    public Context getContext() {
        return mContext;
    }

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
    public void removeItem(int position){
        ((MainActivity)mContext).removeThought(position);
    }

    public List<String> getData() {
        return mThoughts;
    }
    public List<Date> getDates() {
        return mDates;
    }



    @Override
    public int getItemCount() {
        return mThoughts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        EditText thought;
        TextView date;
        ImageView imageView;
        RelativeLayout parentLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            boolean first = true;
            this.thought = itemView.findViewById(R.id.thought);
            this.date = itemView.findViewById(R.id.date);
            this.parentLayout = itemView.findViewById(R.id.parent_layout);
            if(first){
                this.thought.setFocusable(false);
                //this.thought.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
            imageView = itemView.findViewById(R.id.image_recycler);
            //Log.d("DEBUG", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+ getAdapterPosition());
            if(getAdapterPosition()%2==0) {
                imageView.setImageResource(R.drawable.ic_save_icon);
            }
            //imageView.
            this.thought.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO Auto-generated method
                    Log.d("DEBUG", "pos "+ getAdapterPosition() + " getItemId ");
                    Log.d("DEBUG", "enableEdit: ");
                    EditText editText = v.findViewById(R.id.thought);
                    if(editText.isFocusable()){
                        editText.setFocusableInTouchMode(false);
                        editText.setFocusable(false);
                        //editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                        ((MainActivity)mContext).updateThought(getAdapterPosition(), editText);
                        //call and update method
                        Log.d("DEBUG", "UNFOCUS");
                        TextView textView = new TextView(mContext);

                    } else {
                        editText.setFocusableInTouchMode(true);
                        editText.setFocusable(true);
                        //editText.setInputType(InputType.TYPE_CLASS_TEXT);
                        Log.d("DEBUG", "FOCUS");
                        ((MainActivity)mContext).toastMessage("Edit enabled. Long press to save/exit.");

                    }
                    return true;
                }

            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO Auto-generated method
                    Log.d("DEBUG", "pos "+ getAdapterPosition() + " getItemId ");


                    return true;
                }

            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method
                    Log.d("DEBUG", "pos "+ getAdapterPosition() + " getItemId ");
                    Toast.makeText(mContext, "To remove item swipe horizontally. Long press the " +
                            "thought to bring up options", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
}
