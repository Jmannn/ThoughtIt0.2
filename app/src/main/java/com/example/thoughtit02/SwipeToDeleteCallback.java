package com.example.thoughtit02;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/* A simple callback class to implement swipe-to-remove functionality for
 * RecyclerView.
 * @author Johnny Mann
 */
public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private RecyclerViewAdaptor mAdapter;

    SwipeToDeleteCallback(RecyclerViewAdaptor adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mAdapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }
    /* Once an item has been swiped off the screen. It then calls a function which
     * removes it from the model.
     * @param viewHolder - The viewholder to remove the element from.
     * @param direction - Unused.
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        this.mAdapter.removeItem(position);
    }

}
