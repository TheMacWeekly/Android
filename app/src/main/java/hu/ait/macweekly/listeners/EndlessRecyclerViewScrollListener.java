package hu.ait.macweekly.listeners;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.common.base.Optional;

import hu.ait.macweekly.MacWeeklyAPI;
import hu.ait.macweekly.MacWeeklyAPI.Posts.Category;


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
    private Optional<Category> selectedCategory = Optional.absent();
    private Optional<String> searchString = Optional.absent();

    private RecyclerView.LayoutManager mLayoutManager;

    public void setLoading(boolean loading)
    {
        this.loading = loading;
    }

    // TODO: 4/5/18 Separate the category manager from the endless scroll listener. Very much needed
    public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
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
            onLoadMore(currentPage, totalItemCount, view, selectedCategory, searchString);
        }
    }

    // Call this method whenever performing new searches
    public void resetState(RecyclerView view, Optional<Category> category, Optional<String> searchString) {
        this.selectedCategory = category;
        this.searchString = searchString;

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
    public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view, Optional<Category> categoryId, Optional<String> searchString);

}