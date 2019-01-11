package io.korok.mycontacts.view;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

/**
 * Synchronize two RecyclerView, it supposes that the first scroll horizontally,
 * the second one scroll vertically.
 */
public class CLSyncMediator {
    /**
     * RecyclerView that scrolls horizontally.
     */
    private RecyclerView hRecyclerView;

    /**
     * RecyclerView that scroll vertically.
     */
    private RecyclerView vRecyclerView;

    /**
     * EventSource is used to distinguish which RecyclerView was scrolling.
     */
    private EventSource source;

    private ScrollWatcher hScrollWatcher;
    private ScrollWatcher vScrollWatcher;


    /**
     * @param h horizontal RecyclerView
     * @param v vertical RecyclerView
     */
    public CLSyncMediator(RecyclerView h, RecyclerView v) {
        this.hRecyclerView = h;
        this.vRecyclerView = v;
        hScrollWatcher = new ScrollWatcher(h);
        vScrollWatcher = new ScrollWatcher(v);
        source = new EventSource(h, v);
    }

    /**
     * sync is a helper method which is used to synchronize two RecyclerView.
     * The first params must be a horizontal RecyclerView, and the other should
     * be a vertical RecyclerView.
     * @param h horizontal RecyclerView
     * @param v vertical RecyclerView
     */
    public static void sync(RecyclerView h, RecyclerView v) {
        new CLSyncMediator(h, v).sync();
    }

    public void sync() {
        hRecyclerView.addOnScrollListener(hListener);
        vRecyclerView.addOnScrollListener(vListener);
    }

    public void unsync() {
        hRecyclerView.removeOnScrollListener(hListener);
        vRecyclerView.removeOnScrollListener(vListener);
    }

    // onHorizontalScroll computes the position that hRecyclerView scrolls, it's a
    // normalized value, which can used to compute the scroll distance by multiply
    // a unit(width or height). By multiplying height of vRecyclerView, we get the
    // scroll distance of vRecyclerView.
    private void onHorizontalScroll(RecyclerView recyclerView) {
        if (hRecyclerView.getChildCount() == 0 || vRecyclerView.getChildCount() == 0) {
            return;
        }
        // h
        final int offsetX = hScrollWatcher.getOffsetX();
        final int width = recyclerView.getChildAt(0).getWidth();
        final float position = offsetX/(float)width;

        // v
        View v = vRecyclerView.getChildAt(0);
        int offsetY = vScrollWatcher.getOffsetY();
        int height = v.getHeight();
        int delta = (int)(position * height) - offsetY;
        vRecyclerView.smoothScrollBy(0, delta);
    }

    private void onVerticalScroll(RecyclerView recyclerView) {
        if (hRecyclerView.getChildCount() == 0 || vRecyclerView.getChildCount() == 0) {
            return;
        }
        // v
        final int offsetY = vScrollWatcher.getOffsetY();
        final int height = recyclerView.getChildAt(0).getHeight();
        final float position = offsetY/(float)height;

        // h
        View v = hRecyclerView.getChildAt(0);
        int width = v.getWidth();
        int offsetX = hScrollWatcher.getOffsetX();
        int delta = (int)(position * width) - offsetX;
        hRecyclerView.smoothScrollBy(delta, 0);
    }

    // horizontal scroll
    private RecyclerView.OnScrollListener hListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            //vRecyclerView.removeOnScrollListener(vListener);
            if (source.From(hRecyclerView)) {
                onHorizontalScroll(recyclerView);
            }
        }
    };

    // vertical scroll
    private RecyclerView.OnScrollListener vListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            //hRecyclerView.removeOnScrollListener(hListener);
            if (source.From(vRecyclerView)) {
                onVerticalScroll(recyclerView);
            }
        }
    };


    /**
     * A helper class to distinguish which class emits the scroll action.
     */
    static class EventSource {
        private RecyclerView rv1;
        private RecyclerView rv2;
        private boolean fromRv1;
        private boolean fromRv2;

        public EventSource(RecyclerView rv1, RecyclerView rv2) {
            this.rv1 = rv1;
            this.rv2 = rv2;

            rv1.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                    fromRv1 = true;
                    fromRv2 = false;
                    return false;
                }

                @Override
                public void onTouchEvent(RecyclerView rv, MotionEvent e) {

                }

                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                }
            });
            rv2.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                    fromRv2 = true;
                    fromRv1 = false;
                    return false;
                }

                @Override
                public void onTouchEvent(RecyclerView rv, MotionEvent e) {

                }

                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                }
            });
        }

        public boolean From(RecyclerView rv) {
            if (rv == rv1 && fromRv1) {
                return true;
            }
            if (rv == rv2 && fromRv2) {
                return true;
            }
            return false;
        }
    }

    /**
     * RecyclerView does not implement View.getScrollX() and View.getScrollY() method.
     * ScrollWatcher watches the scroll action and remember the scroll distance.
     */
    static class ScrollWatcher extends RecyclerView.OnScrollListener {
        private int offsetX;
        private int offsetY;

        public ScrollWatcher(RecyclerView recyclerView) {
            recyclerView.addOnScrollListener(this);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            offsetX += dx;
            offsetY += dy;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }
    }
}
