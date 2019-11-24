package com.example.thoughtit02;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecyclerViewAdaptor extends RecyclerView.Adapter<RecyclerViewAdaptor.ViewHolder>{

    private static final String TAG = "RecyclerViewAdaptor";
    private List<String> mThoughts = new ArrayList<>();
    private List<String> mType = new ArrayList<>();
    private List<String> mUrl = new ArrayList<>();
    private List<Date> mDates = new ArrayList<>();

    public Context getContext() {
        return mContext;
    }

    private Context mContext;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    private CustomBitmapTransform customBitmapTransform =  new CustomBitmapTransform(screenHeight/2);


    public RecyclerViewAdaptor(Context mContext, List<String> mThoughts, List<Date> mDates,List<String> mType,List<String> mUrl) {
        this.mThoughts = mThoughts;
        this.mDates = mDates;
        this.mType = mType;
        this.mUrl = mUrl;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        holder.thought.setText(mThoughts.get(position));
        holder.date.setText("Date of Thought: "+mDates.get(position).toString());
        if (mType.get(position).equals("Photo")) {
            holder.imageView.setVisibility(View.VISIBLE);
            Log.d("DEBUG", "ATACHING IMAGE");
            RequestOptions options = new RequestOptions();
            Glide.with(mContext).
                    asBitmap().
                    load(mUrl.get(position)).
                    diskCacheStrategy(DiskCacheStrategy.NONE).//slower load, but otherwise images dont load correct size
                    transform(this.customBitmapTransform).
                    apply(new RequestOptions()).into(holder.imageView);
            holder.mediaBar.setVisibility(View.GONE);
        } else if (mType.get(position).equals("Audio")){
            holder.imageView.setVisibility(View.GONE);
            holder.mediaBar.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.mediaBar.setVisibility(View.GONE);
        }


    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, List<Object> payloads) {

        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position , payloads);
        }else{

            for (Object payload : payloads) {
                if (payload instanceof Integer) {
                    holder.seekBar.setProgress(((Integer) payload).intValue());
                }
            }
        }

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
        RelativeLayout mediaBar;
        ImageButton playButton;
        ImageButton pauseButton;
        ImageButton stopButton;
        SeekBar seekBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            boolean first = true;
            this.thought = itemView.findViewById(R.id.thought);
            this.date = itemView.findViewById(R.id.date);
            this.parentLayout = itemView.findViewById(R.id.parent_layout);
            this.mediaBar = itemView.findViewById(R.id.audio_widget);
            this.playButton = itemView.findViewById(R.id.play);
            this.pauseButton = itemView.findViewById(R.id.pause);
            this.stopButton = itemView.findViewById(R.id.stop);
            this.seekBar = itemView.findViewById(R.id.seek_bar);
            if(first){
                this.thought.setFocusable(false);
            }
            imageView = itemView.findViewById(R.id.image_recycler);
            this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        ((MainActivity) mContext).timePostionOfTrack(getAdapterPosition(), progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            this.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method
                    Log.d("DEBUG", "pos "+ getAdapterPosition() + " play ");
                    ((MainActivity)mContext).playRecording(getAdapterPosition());

                }
            });
            this.pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method
                    Log.d("DEBUG", "pos "+ getAdapterPosition() + " play ");
                    ((MainActivity)mContext).pauseRecording();

                }
            });
            this.stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method
                    Log.d("DEBUG", "pos "+ getAdapterPosition() + " play ");
                    ((MainActivity)mContext).stopRecording();

                }
            });
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
