package hu.ait.macweekly.listeners;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;


/**
 * Code from https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews-and-RecyclerView
 */

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleThreshold = 15;
    // The current offset index of data you have loaded
    private int currentPage = 0;
    // The total number of items in the dataset after the last load
    private int previousTotalItemCount = 0;
    // True if we are still waiting for the last set of data to load.
    private boolean loading = false;
    // Sets the starting page index
    private int startingPageIndex = 0;

    private RecyclerView.LayoutManager mLayoutManager;
    private ParamManager mParamManager;

    public void setLoading(boolean loading)
    {
        this.loading = loading;
    }

    // TODO: 4/5/18 Separate the category manager from the endless scroll listener. Very much needed
    public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
        this.mParamManager = new ParamManager();
    }

    public int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            }
            else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        Log.w("ENDLESS_SCROLL", Boolean.toString(loading));
        int lastVisibleItemPosition = 0;
        int totalItemCount = mLayoutManager.getItemCount();

        lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!loading && (lastVisibleItemPosition + visibleThreshold) > totalItemCount) {
            currentPage++;
            Log.w("ENDLESS_SCROLL", "loadingmore");
            onLoadMore(currentPage, totalItemCount, view, mParamManager.getCatId(), mParamManager.getSearchStr());
        }
    }

    // Call this method whenever performing new searches
    public void resetState(RecyclerView view, int categoryId, String searchString) {
        this.mParamManager.setCatId(categoryId);
        this.mParamManager.setSearchStr(searchString);
        this.currentPage = this.startingPageIndex;
        this.previousTotalItemCount = 0;
        this.loading = true;
        onScrolled(view, 0, 0);
    }

    // For some classes this needs to be called every time you reset the news/search feed. If your
    // feed already comes with 1 or more items in it, like the news feed with the header view, then
    // you don't need to call this. Otherwise if you are just loading only a list of items from the api,
    // like with search, you need to call this on every reset/restart.
    public void startListener(RecyclerView view) {
        this.loading = false;
        onScrolled(view, 0, 0);
    }

    // Defines the process for actually loading more data based on page
    public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view, int categoryId, String searchString);

    public ParamManager getParamManager() {
        return mParamManager;
    }

    /**
     * This class is in charge of keeping track of what the current category or search parameters are.
     * Stores a category id (Which could stand for no category) and a search string (Which could stand
     * for no search param).
     */
    public class ParamManager {
        public static final String NO_SEARCH = "";
        public static final int NO_CATEGORY = -1;

        int categoryId = NO_CATEGORY;
        String searchStr = NO_SEARCH;

        protected void setCatId(int catId) {
            this.categoryId = catId;
        }

        public int getCatId() {return this.categoryId;}

        public boolean usingCatId() {return this.categoryId != NO_CATEGORY;}

        public String getCatString(int catId) {
            switch(catId) {
                case -1:
                    return "All Stories";
                case 3:
                    return "News";
                case 5:
                    return "Sports";
                case 4:
                    return "Features";
                case 7:
                    return "Opinion";
                case 6:
                    return "Arts";
                case 28:
                    return "Food & Drink";
                default:
                    return "All Stories";
            }
        }

        protected void setSearchStr(String searchStr) {
            this.searchStr = searchStr;
        }

        public String getSearchStr() {return this.searchStr;}

        public boolean usingSearchStr() {return !this.searchStr.equals(NO_SEARCH);}
    }

}