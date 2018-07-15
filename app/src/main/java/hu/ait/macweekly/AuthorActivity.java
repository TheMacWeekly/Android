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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.ait.macweekly.adapter.SearchRecyclerAdapter;
import hu.ait.macweekly.data.Article;
import hu.ait.macweekly.data.GuestAuthor;
import hu.ait.macweekly.listeners.ArticleViewClickListener;
import hu.ait.macweekly.listeners.EndlessRecyclerViewScrollListener;

public class AuthorActivity extends MacWeeklyApiActivity implements ArticleViewClickListener {

    boolean showingNewsFeed = false;

    // Keys
    public static final String ARTICLE_AUTHOR_KEY = "articleAuthorKey";
    public static final String AUTHOR_IMG_URL_KEY = "authorImgUrlKey";
    public static final String AUTHOR_BIO_KEY = "authorBioKey";

    // Constants
    private final String LOG_TAG = "AuthorActivity - ";

    // Members
    private SearchRecyclerAdapter mArticleAdapter;
    private EndlessRecyclerViewScrollListener mEndlessScrollListener;

    // Views
    @BindView(R.id.refresh_view) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.errorButton) Button mErrorButtonView;
    @BindView(R.id.ivAuthor) ImageView mAuthorImageView;
    @BindView(R.id.tvAuthorName) TextView mAuthorNameView;
    @BindView(R.id.tvAuthorBio) TextView mAuthorBioView;

    // Data
    String mAuthorName;
    String mAuthorBio;
    String mAuthorImgUrl;

    // Code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Loading AuthorActivity");
        initContentViews();

        populateDataMembers();

        prepareNewsAPI();

        prepareContentViews();

        setUpBackButton();
    }

    private void prepareContentViews() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mEndlessScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view, String authorName) {
                addArticles(page, authorName);
            }
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view, int categoryId, String searchString) {}
        };
        EndlessRecyclerViewScrollListener.ParamManager paramManager = mEndlessScrollListener.getParamManager();
        paramManager.setAuthorName(mAuthorName);

        mArticleAdapter = new SearchRecyclerAdapter(getApplicationContext(), this, paramManager);
        mArticleAdapter.setDataSet(new ArrayList<Article>());

        mMainContent.setLayoutManager(linearLayoutManager);
        mMainContent.setAdapter(mArticleAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetArticles();
            }
        });

        mMainContent.addOnScrollListener(mEndlessScrollListener);

    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initContentViews() {
        setContentView(R.layout.activity_author);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
    }

    private void resetArticles() {
        mArticleAdapter.clearDataSet();
        mArticleAdapter.notifyDataSetChanged();
        showScreenHint(SC_MAINCONTENT);
        mEndlessScrollListener.resetState(mMainContent, mAuthorName);
        mEndlessScrollListener.startListener(mMainContent);
    }

    private void addArticles(int pageNum, String authorName) {
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
        callAuthoredNewsAPI(pageNum, authorName, articleCallback);
    }

    private void populateDataMembers() {
        Intent validIntent = getIntent();

        String authorText = validIntent.getStringExtra(ARTICLE_AUTHOR_KEY);
        if(authorText != null) mAuthorName = authorText; //Currently the mac weekly is a mess and when they don't have guest authors stored the title is stored here instead :/
        else mAuthorName = "";

        mAuthorNameView.setText(mAuthorName);

        String authorBio = validIntent.getStringExtra(AUTHOR_BIO_KEY);
        if(authorBio != null) mAuthorBio = authorBio;
        else mAuthorBio = "";

        mAuthorBioView.setText(mAuthorBio);

        String authorImgUrl = validIntent.getStringExtra(AUTHOR_IMG_URL_KEY);
        if(authorImgUrl != null) {
            mAuthorImgUrl = authorImgUrl;
            Glide.with(this).load(mAuthorImgUrl).into(mAuthorImageView);
            mAuthorImageView.setVisibility(View.VISIBLE);
        } else {
            mAuthorImgUrl = "";
            mAuthorImageView.setVisibility(View.GONE);
        }
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
