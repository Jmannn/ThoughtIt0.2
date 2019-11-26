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
    /* Log Tag. */
    private static final String TAG = "RecyclerViewAdaptor";
    /* A reference to the model. */
    private ThoughtCollection thoughtCollection;
    /* A reference to the main activity context. */
    private Context mContext;
    /* The height of the screen of the device. */
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    /* Custom bitmap transformation object for limiting image size. */
    private CustomBitmapTransform customBitmapTransform =  new CustomBitmapTransform(screenHeight/2);

    /* Sets a copy of the references to context and model to this instance.
     * @param reference of the model
     * @param main activity context
     */
    RecyclerViewAdaptor(Context mContext,ThoughtCollection thoughtCollection) {
        this.thoughtCollection = thoughtCollection;
        this.mContext = mContext;
    }
    /* This method inflates the list item for RecyclerView.
     * @param the parent recycler view
     * @param viewtype ID
     * @return a viewholder containing these views
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        return new ViewHolder(view);
    }
    /* This method prepares the view for display by adding the data from the model,
     * using Glide to display an image, and hiding no applicable view elements for the data type.
     * @param the view holder
     * @param ViewHolder position to bind to
     */
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
    /* Used primarily to update a seek bar, this method allows main activity to
     * to make changes to view items.
     * @param the ViewHolder
     * @param the position within the ViewHolder
     * @param the data to add to the view
     */
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
    /* This method instructs main activity to remove an item from
     * both recycler view and model.
     * @param pos - the position to remove the data from
     */
    void removeItem(int pos){
        ((MainActivity)mContext).removeThought(pos);
    }

    /* Returns the amount of items to be displayed. This tells RecyclerView how many
     * items to display.
     * @param display count
     */
    @Override
    public int getItemCount() {
        return this.thoughtCollection.getDisplaySize();
    }
    /* This inner class defines operations that happen to every list item.
     * It creates references to the view elements in the ViewHolder and attaches
     * listeners to various components.
     */
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
                /* If the user touching the bar causes this event, then it will update the
                 * track in main activity.
                 * @param seekBar - the bar touched by the user
                 * @param progress - the current change in progress
                 * @param fromUser - whether the user touched the bar
                 */
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) ((MainActivity) mContext).changeTimePositionOfTrack(progress);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            this.playButton.setOnClickListener(new View.OnClickListener() {
                /* Instructs MediaPlayer in main activity to begin playing the file
                 * corresponding to the list item.
                 * @param v - The play button that caused the event.
                 */
                @Override
                public void onClick(View v) {
                    ((MainActivity)mContext).playRecording(getAdapterPosition());
                }
            });
            this.pauseButton.setOnClickListener(new View.OnClickListener() {
                /* Instructs MediaPlayer in main activity to pause the audio file
                 * corresponding to the list item.
                 * @param v - The pause button that caused the event.
                 */
                @Override
                public void onClick(View v) {
                    ((MainActivity)mContext).pauseRecording();
                }
            });
            this.stopButton.setOnClickListener(new View.OnClickListener() {
                /* Instructs MediaPlayer in main activity to stop playing file
                 * corresponding to the list item.
                 * @param v - The stop button that caused the event.
                 */
                @Override
                public void onClick(View v) {
                    ((MainActivity)mContext).stopRecording();
                }
            });
            this.thought.setOnLongClickListener(new View.OnLongClickListener() {
                /* This method makes the text being displayed in the list item
                 * editable on long click. A repeat long click saves the new entry
                 * and makes it uneditable again.
                 * @param v - The EditText view that caused the event.
                 */
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
            itemView.setOnClickListener(new View.OnClickListener() {
                /* When a list item is clicked, a small help message should appear.
                 * @param v - The list item view that caused the event.
                 */
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "To remove item swipe horizontally. Long press the " +
                            "thought to bring up options", Toast.LENGTH_SHORT).show();

                }
            });
        }

    }


}
