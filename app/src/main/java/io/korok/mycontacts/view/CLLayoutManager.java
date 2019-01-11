package io.korok.mycontacts.view;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;


/**
 * CLLayoutManager is a Center-Locking, horizontal LayoutManager. It uses
 * SnapHelper to lock child in center.
 */
public class CLLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    /**
     * snapHelper used to lock the child view in center of parent.
     */
    private LinearSnapHelper snapHelper = new LinearSnapHelper();

    private RecyclerView recyclerView;

    /**
     * the fist visible child view, it's always the first child in parent.
     */
    private int firstPosition;

    public CLLayoutManager() {
    }

    public void lockCenter(final RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        snapHelper.attachToRecyclerView(recyclerView);
        clickToCenter();
    }

    private void clickToCenter() {
        recyclerView.addOnItemTouchListener(new CLItemClickListener(recyclerView) {
            @Override
            public void onItemClick(View v) {
                if (v != null) {
                    smoothScrollToPosition(v);
                }
            }
        });
    }


    /**
     * Move the view to center.
     * @param v
     */
    private void smoothScrollToPosition(View v) {
        View snap = snapHelper.findSnapView(this);
        if (snap != v) {
            recyclerView.smoothScrollToPosition(getPosition(v));
        } else {
            final int mid = getWidth()/2;
            final int pos = snap.getLeft() + snap.getWidth()/2;
            if (Math.abs(mid-pos) < 4) {
                recyclerView.smoothScrollToPosition(getPosition(v));
            }
        }
    }

    /**
     * Returns layout params.
     */
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        if (getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        // No data changes, just return.
        if (state.isPreLayout() || (state.getItemCount() != 0 && !state.didStructureChange())) {
            return;
        }

        // For the first time, we should layout the child from center, then we
        // just layout from left to right by the edge of the first visible item.
        final int parentRight = getWidth();
        final int count = getItemCount();

        int left = 0;
        if (getChildCount() > 0) {
            left = getChildAt(0).getLeft();
        } else {
            View scrap = recycler.getViewForPosition(0);
            measureChildWithMargins(scrap, 0, 0);
            int w = getDecoratedMeasuredWidth(scrap);
            left = (parentRight-w)/2;
            recycler.recycleView(scrap);
        }

        int right  = 0;
        int top    = 0;
        int bottom = getHeight();


        detachAndScrapAttachedViews(recycler);

        // start layout horizontally.
        for(int i = firstPosition; i < count && left < parentRight ; i++, left=right) {
            View child = recycler.getViewForPosition(i);
            addView(child);
            {
                // size of the child
                measureChildWithMargins(child, 0, 0);
                int width = getDecoratedMeasuredWidth(child);
                right = left + width;
                layoutDecoratedWithMargins(child, left, top, right, bottom);
            }
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }

        // Figure out how much space can we scroll. When it reaches the start or
        // the end of the list, we should pay limit it to the middle of parent.
        int delta = -dx;
        int mid = getWidth()/2;
        View child;
        if (dx > 0) {
            if (getPosition(getChildAt(getChildCount() - 1)) == getItemCount() - 1) {
                child = getChildAt(getChildCount() - 1);
                delta = -Math.max(0, Math.min(dx, (child.getRight() - child.getLeft()) / 2 + child.getLeft() - mid));
            }
        } else {
            if (firstPosition == 0) {
                child = getChildAt(0);
                delta = -Math.min(0, Math.max(dx, ((child.getRight() - child.getLeft()) / 2 + child.getLeft()) - mid));
            }
        }

        // Fill the hole left by scroll.
        if (dx > 0) {
             fillWhenScrollLeft(dx, recycler);
        } else {
             fillWhenScrollRight(dx, recycler);
        }

        // Remove invisible views.
        recycleViewsOutOfBounds(recycler);

        // Scroll children views by delta.
        offsetChildrenHorizontal(delta);
        return -delta;
    }

    // If there is free space in left, insert a child view at 0.
    private void fillWhenScrollRight(int dx, RecyclerView.Recycler recycler) {
        if (getChildCount() <= 0 || dx >=0) {
            return;
        }
        View first = getChildAt(0);
        int position = getPosition(first);
        int offsetX = getDecoratedLeft(first);
        int hangingLeft = dx; // CAUTION: value < 0
        int height = getHeight();
        for(int i = position-1; i >=0 && offsetX > hangingLeft; i--) {
            View v = recycler.getViewForPosition(i);
            addView(v, 0);
            measureChildWithMargins(v, 0, 0);
            int width = getDecoratedMeasuredWidth(v);
            layoutDecorated(v,  offsetX-width, 0, offsetX, height);
            offsetX -= width;
            firstPosition = i;
        }
    }

    // If there is free space in right, append a child.
    private void fillWhenScrollLeft(int dx, RecyclerView.Recycler recycler) {
        if (getChildCount() <= 0 || dx <= 0) {
            return;
        }
        View last = getChildAt(getChildCount()-1);
        int position = getPosition(last);
        int offsetX = getDecoratedRight(last);
        int count = getItemCount();
        int hangingRight = getWidth() + dx;
        int height = getHeight();

        for (int i = position+1; i < count && offsetX < hangingRight; i++) {
            View v = recycler.getViewForPosition(i);
            addView(v);
            measureChildWithMargins(v, 0, 0);
            int width = getDecoratedMeasuredWidth(v);
            layoutDecorated(v,  offsetX, 0, offsetX+width, height);
            offsetX += width;
        }
        return;
    }


    // Remove all the views that are invisible, updates the first visible position
    // at the same time.
    private void recycleViewsOutOfBounds(RecyclerView.Recycler recycler) {
        final int childCount = getChildCount();
        final int parentWidth = getWidth();
        final int parentHeight = getHeight();
        boolean foundFirst = false;
        int first = 0; // 第一个可见元素
        int last = 0;  // 最后一个可见元素
        for (int i = 0; i < childCount; i++) {
            final View v = getChildAt(i);
            if (v.hasFocus() || inParentBound(v, parentWidth, parentHeight)) {
                if (!foundFirst) {
                    first = i;
                    foundFirst = true;
                }
                last = i;
            }
        }
        for (int i = childCount-1; i > last; i--) {
            removeAndRecycleViewAt(i, recycler);
        }
        for (int i = first-1; i >= 0; i--) {
            removeAndRecycleViewAt(i, recycler);
        }
        if (getChildCount() == 0) {
            firstPosition = 0;
        } else {
            firstPosition += first;
        }
    }

    // If a view is in parent bounding box.
    private boolean inParentBound(View v, int width, int height) {
        return getDecoratedRight(v) >= 0 &&
                getDecoratedLeft(v) <= width &&
                getDecoratedTop(v) <= height &&
                getDecoratedBottom(v) >= 0;
    }


    // Scroll logic //
    private int calculateScrollDirectionForPosition(int position) {
        if (getChildCount() == 0) {
            return -1;
        }
        final int firstChildPos = firstPosition;
        return position < firstChildPos ? -1 : 1;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        final int direction = calculateScrollDirectionForPosition(targetPosition);
        PointF outVector = new PointF();
        if (direction == 0) {
            return null;
        }
        outVector.x = direction;
        outVector.y = 0;
        return outVector;
    }


    /**
     * RecyclerView will not scroll if we don't implement this method.
     * @param recyclerView
     * @param state
     * @param position
     */
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        if (position >= getItemCount() || position < 0) {
            return;
        }
        final Context context = recyclerView.getContext();
        LinearSmoothScroller scroller = new SmoothScroller(context);
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    /**
     * SmoothScroller is required to make RecyclerView.smoothScrollToPosition() works.
     */
    private class SmoothScroller extends LinearSmoothScroller {

        public SmoothScroller(Context context) {
            super(context);
        }
        //This controls the direction in which smoothScroll looks
        //for your view
        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return CLLayoutManager.this.computeScrollVectorForPosition(targetPosition);
        }

        private static final float MILLISECONDS_PER_INCH = 80f;

        //This returns the milliseconds it takes to
        //scroll one pixel.
        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH/displayMetrics.densityDpi;
        }

        /**
         * Calculates the horizontal scroll amount necessary to make the given view in center of the RecycleView.
         */
        public int calculateDxToMakeCentral(View view) {
            final RecyclerView.LayoutManager layoutManager = getLayoutManager();
            if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
                return 0;
            }
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            final int left = layoutManager.getDecoratedLeft(view) - params.leftMargin;
            final int right = layoutManager.getDecoratedRight(view) + params.rightMargin;
            final int start = layoutManager.getPaddingLeft();
            final int end = layoutManager.getWidth() - layoutManager.getPaddingRight();
            final int childCenter = left + (int) ((right - left) / 2.0f);
            final int containerCenter = (int) ((end - start) / 2.f);
            return containerCenter - childCenter;
        }


        @Override
        protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
            final int dx = calculateDxToMakeCentral(targetView);
            final int distance = dx;
            final int time = calculateTimeForDeceleration(distance);
            if (time > 0) {
                action.update(-dx, 0, time, mDecelerateInterpolator);
            }
        }
    }
}

