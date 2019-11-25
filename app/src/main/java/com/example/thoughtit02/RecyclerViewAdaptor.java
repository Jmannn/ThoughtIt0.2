package com.example.thoughtit02;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;

public class RecyclerViewAdaptor extends RecyclerView.Adapter<RecyclerViewAdaptor.ViewHolder>{

    private static final String TAG = "RecyclerViewAdaptor";
    private ThoughtCollection thoughtCollection;


    private Context mContext;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    private CustomBitmapTransform customBitmapTransform =  new CustomBitmapTransform(screenHeight/2);


    RecyclerViewAdaptor(Context mContext,ThoughtCollection thoughtCollection) {
        this.thoughtCollection = thoughtCollection;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String prefix ="Date of Thought: ";
        Thought thought = this.thoughtCollection.getThought(position);
        String date = prefix+thought.getDate();
        holder.thought.setText(thought.getThoughtText());
        holder.date.setText(date);
        if (thought.getType() == Type.PICTURE) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(mContext).
                    asBitmap().
                    load(thought.getUri()).
                    transform(this.customBitmapTransform).
                    diskCacheStrategy(DiskCacheStrategy.NONE).
                    apply(new RequestOptions()).into(holder.imageView);
            holder.mediaBar.setVisibility(View.GONE);
        } else if (thought.getType() == Type.AUDIO){
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
                    holder.seekBar.setProgress((Integer) payload);
                }
            }
        }

    }
    void removeItem(int position){
        ((MainActivity)mContext).removeThought(position);
    }

    @Override
    public int getItemCount() {
        return this.thoughtCollection.getDisplaySize();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        EditText thought;
        TextView date;
        ImageView imageView;
        RelativeLayout parentLayout;
        RelativeLayout mediaBar;
        ImageButton playButton;
        ImageButton pauseButton;
        ImageButton stopButton;
        SeekBar seekBar;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.thought = itemView.findViewById(R.id.thought);
            this.date = itemView.findViewById(R.id.date);
            this.parentLayout = itemView.findViewById(R.id.parent_layout);
            this.mediaBar = itemView.findViewById(R.id.audio_widget);
            this.playButton = itemView.findViewById(R.id.play);
            this.pauseButton = itemView.findViewById(R.id.pause);
            this.stopButton = itemView.findViewById(R.id.stop);
            this.seekBar = itemView.findViewById(R.id.seek_bar);
            this.thought.setFocusable(false);

            imageView = itemView.findViewById(R.id.image_recycler);
            this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) ((MainActivity) mContext).timePositionOfTrack(progress);
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
                    ((MainActivity)mContext).playRecording(getAdapterPosition());
                }
            });
            this.pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)mContext).pauseRecording();
                }
            });
            this.stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)mContext).stopRecording();
                }
            });
            this.thought.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    EditText editText = v.findViewById(R.id.thought);
                    if(editText.isFocusable()){
                        editText.setFocusableInTouchMode(false);
                        editText.setFocusable(false);
                        ((MainActivity)mContext).updateThought(getAdapterPosition(), editText);

                    } else {
                        editText.setFocusableInTouchMode(true);
                        editText.setFocusable(true);
                        ((MainActivity)mContext).toastMessage("Edit enabled. Long press to save/exit.");

                    }
                    return true;
                }

            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }

            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "To remove item swipe horizontally. Long press the " +
                            "thought to bring up options", Toast.LENGTH_SHORT).show();

                }
            });
        }

    }


}
