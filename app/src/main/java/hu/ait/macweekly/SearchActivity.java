package hu.ait.macweekly;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.ait.macweekly.adapter.SearchRecyclerAdapter;
import hu.ait.macweekly.data.Article;
import hu.ait.macweekly.data.GuestAuthor;
import hu.ait.macweekly.listeners.ArticleViewClickListener;
import hu.ait.macweekly.listeners.EndlessRecyclerViewScrollListener;

public class SearchActivity extends MacWeeklyApiActivity implements ArticleViewClickListener {

    boolean showingNewsFeed = false;
    String searchQuery = "";

    // Constants
    private final String LOG_TAG = "SearchActivity - ";

    // Members
    private SearchRecyclerAdapter mArticleAdapter;
    private EndlessRecyclerViewScrollListener mEndlessScrollListener;

    // Views
    @BindView(R.id.refresh_view) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.search_field) EditText mSearchField;
    @BindView(R.id.errorButton) Button mErrorButtonView;

    // Code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentViews();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prepareNewsAPI();

        prepareContentViews();

        setUpBackButton();
    }

    private void prepareContentViews() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mEndlessScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view, String authorName) {}
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view, int categoryId, String searchString) {
                addArticles(page, categoryId, searchString);
            }
        };
        EndlessRecyclerViewScrollListener.ParamManager paramManager = mEndlessScrollListener.getParamManager();

        mArticleAdapter = new SearchRecyclerAdapter(getApplicationContext(), this, paramManager);
        mArticleAdapter.setDataSet(new ArrayList<Article>());

        mMainContent.setLayoutManager(linearLayoutManager);
        mMainContent.setAdapter(mArticleAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetArticlesWithSearch(searchQuery);
            }
        });

        mMainContent.addOnScrollListener(mEndlessScrollListener);

        // Setting up search view
        TextView.OnEditorActionListener editListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                // If the enter key was hit
                // Note: There might be an error here. Not sure if this is the enter button for all
                //       devices... I think it is, but if there is a bug with search enter key check here
                if (i == EditorInfo.IME_ACTION_DONE ||
                        (keyEvent != null && keyEvent.getAction() == KeyEvent.KEYCODE_ENTER)) {

                    closeKeyboard();

                    searchQuery = "";
                    if(mSearchField.getText() != null) searchQuery = mSearchField.getText().toString();

                    // If the search is valid
                    if(isValidSearchQuery(searchQuery)) {
                        resetArticlesWithSearch(searchQuery);
                    } else {
                        Log.d(LOG_TAG, "Search input with only spaces or empty is invalid");
                        showScreenHint(SC_STARTSEARCH);
                    }
                }
                return true;
            }
        };

        mErrorButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetArticlesWithSearch(searchQuery);
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mSearchField.setOnEditorActionListener(editListener);

        showScreenHint(SC_STARTSEARCH);
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean isValidSearchQuery(String query) {
        if(query == null) return false;

        String noWhiteSpace = query.replaceAll("\\s+","");
        return !MacWeeklyUtils.isTextEmpty(noWhiteSpace);
    }

    private void initContentViews() {
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
    }

    private void resetArticlesWithSearch(String searchString) {
        if(isValidSearchQuery(searchString)) {
            resetArticles(EndlessRecyclerViewScrollListener.ParamManager.NO_CATEGORY, searchString);
        } else {
            Log.d(LOG_TAG, "Search input with only spaces or empty is invalid");
            showScreenHint(SC_STARTSEARCH);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void resetArticles(int categoryId, String searchString) {
        mArticleAdapter.clearDataSet();
        mArticleAdapter.notifyDataSetChanged();
        showScreenHint(SC_MAINCONTENT);
        mEndlessScrollListener.resetState(mMainContent, categoryId, searchString);
        mEndlessScrollListener.startListener(mMainContent);
    }

    private void addArticles(int pageNum, int categoryId, String searchString) {
        final int startSize = mArticleAdapter.getItemCount();
        MacWeeklyApiActivity.ArticleCallback articleCallback = new MacWeeklyApiActivity.ArticleCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                if (!showingNewsFeed) showScreenHint(SC_MAINCONTENT);
                mArticleAdapter.addToDataSet(articles);
                mArticleAdapter.notifyItemRangeChanged(startSize, ARTICLES_PER_CALL);
            }

            @Override
            public void onFailure() {
                if (mArticleAdapter.getDataSet().size() == 0) showScreenHint(SC_ERROR);
            }
        };
        callNewsAPI(pageNum, categoryId, searchString, articleCallback);
    }

    @Override
    void gotCallReturned() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    void onResponseSuccess(int pageNum, List<Article> newArticles) {
        if(!showingNewsFeed) showScreenHint(SC_MAINCONTENT);
    }

    @Override
    void onResponseSuccessEmptyBody(int pageNum) {
        if(mArticleAdapter.getItemCount() == 0) showScreenHint(SC_NORESULT);
    }

    @Override
    void onResponseFailure(int pageNum) {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void articleViewClicked(View view, int position) {
        showFullArticle(mArticleAdapter.getDataSet().get(position));
    }

    private void showFullArticle(Article targetArticle) {
        // These attributes might be null or missing
        String authorBio = "";
        String authorName = "";
        String authorImgUrl = "";
        if(targetArticle.guestAuthor != null) {

            GuestAuthor gAuthor = targetArticle.guestAuthor;

            if(gAuthor.name != null) {
                authorName = targetArticle.guestAuthor.name;
            }

            if(!MacWeeklyUtils.isTextEmpty(gAuthor.imgUrl)) {
                authorImgUrl = gAuthor.imgUrl;
            }

            if(!MacWeeklyUtils.isTextEmpty(gAuthor.bio)){
                authorBio = gAuthor.bio;
            }
        }

        Intent articleIntent = new Intent(this, ArticleActivity.class);
        articleIntent.putExtra(ArticleActivity.ARTICLE_CONTENT_KEY, targetArticle.content
                .rendered);
        articleIntent.putExtra(ArticleActivity.ARTICLE_DATE_KEY, targetArticle.date);
        articleIntent.putExtra(ArticleActivity.ARTICLE_TITLE_KEY, targetArticle.title.rendered);
        articleIntent.putExtra(ArticleActivity.ARTICLE_AUTHOR_KEY, authorName);
        articleIntent.putExtra(ArticleActivity.ARTICLE_LINK_KEY, targetArticle.link);
        articleIntent.putExtra(ArticleActivity.AUTHOR_IMG_URL_KEY, authorImgUrl);
        articleIntent.putExtra(ArticleActivity.AUTHOR_BIO_KEY, authorBio);

        startActivity(articleIntent);
    }
}
