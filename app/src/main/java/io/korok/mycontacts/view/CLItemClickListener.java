package io.korok.mycontacts.view;

import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * CLItemClickListener works like OnItemClickListener in ListView.
 */
public abstract class CLItemClickListener implements RecyclerView.OnItemTouchListener {
    private GestureDetector g;

    public CLItemClickListener(final RecyclerView recyclerView) {
        this.g = new GestureDetector(recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                final View v = recyclerView.findChildViewUnder(e.getX(), e.getY());
                onItemClick(v);
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return g.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public abstract void onItemClick(View v);
}
