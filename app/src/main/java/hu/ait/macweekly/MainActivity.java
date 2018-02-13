package hu.ait.macweekly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.ait.macweekly.adapter.ArticleRecyclerAdapter;
import hu.ait.macweekly.data.Article;
import hu.ait.macweekly.data.GuestAuthor;
import hu.ait.macweekly.listeners.ArticleViewClickListener;
import hu.ait.macweekly.listeners.EndlessRecyclerViewScrollListener;
import hu.ait.macweekly.network.NewsAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ArticleViewClickListener {

    boolean showingNewsFeed = false;

    // Constants
    private final String LOG_TAG = "MainActivity - ";

    private final int ARTICLES_PER_CALL = 10;

    // Members
    private NewsAPI newsAPI;
    private ArticleRecyclerAdapter mArticleAdapter;
    private EndlessRecyclerViewScrollListener mEndlessScrollListener;

    // Views
    @BindView(R.id.main_content) RecyclerView mMainContent;
    @BindView(R.id.refresh_view) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.newsFeedErrorView) LinearLayout mErrorView;
    @BindView(R.id.errorButton) Button mButtonView;

    // Code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentViews();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prepareDrawer(toolbar);

//        prepareNavView();

        prepareNewsAPI();

        prepareContentViews();

    }

    private void prepareContentViews() {
        mArticleAdapter = new ArticleRecyclerAdapter(getApplicationContext(), this);
        mArticleAdapter.setDataSet(new ArrayList<Article>());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mMainContent.setLayoutManager(linearLayoutManager);
        mMainContent.setAdapter(mArticleAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                callNewsAPI();
                resetArticles();
            }
        });
        mSwipeRefreshLayout.post(new Runnable() { // TODO: 10/29/17 Need this?
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mEndlessScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                addArticles(page);
            }
        };
        mMainContent.addOnScrollListener(mEndlessScrollListener);

        mButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetArticles();
            }
        });
    }

    private void initContentViews() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    private void prepareNavView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void prepareDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED); // TODO: 10/30/17 Turn this back on when feature finished
    }

    public void prepareNewsAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://themacweekly.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        newsAPI = retrofit.create(NewsAPI.class);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            resetArticles();
            return true;
        }else if (id == R.id.about_page) {
            goToAboutPage();
            resetArticles();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToAboutPage() {
        Intent aboutPageIntent = new Intent(this, AboutPage.class);
        startActivity(aboutPageIntent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showNewsFeed() {
        mErrorView.setVisibility(View.GONE);
        mMainContent.setVisibility(View.VISIBLE);
        showingNewsFeed = true;
    }
    public void showErrorScreen() {
        mMainContent.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
        showingNewsFeed = false;
    }

    public interface ArticleCallback {
        void onSuccess(List<Article> articles);
        void onFailure();
    }

    private void callNewsAPI(int pageNum, final ArticleCallback articleCallback) {

        Call<List<Article>> articleCall = newsAPI.getArticles(pageNum, ARTICLES_PER_CALL);
        articleCall.enqueue(new Callback<List<Article>>() {
            @Override
            public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {

                if (response.body() != null) {
                    List<Article> uncleanedResponse = response.body();
                    List<Article> cleanedResponse = cleanResponse(uncleanedResponse);
                    Log.d(LOG_TAG, "Got response back");
                    mSwipeRefreshLayout.setRefreshing(false);
                    if(!showingNewsFeed) showNewsFeed();

                    articleCallback.onSuccess(cleanedResponse);
                } else {
                    // TODO: 10/28/17 Show visual issue here
                    Log.e(LOG_TAG, "api response body is null");
                    mSwipeRefreshLayout.setRefreshing(false);
                    showErrorScreen();

                    articleCallback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<List<Article>> call, Throwable t) {
                Log.e(LOG_TAG, "call failed");
                mSwipeRefreshLayout.setRefreshing(false);
                showErrorScreen();
            }
        });
    }

    private void resetArticles() {
        mEndlessScrollListener.resetState();
        mArticleAdapter.setDataSet(new ArrayList<Article>());
        mArticleAdapter.notifyDataSetChanged();
    }

    private void addArticles(int pageNum) {
        final int startSize = mArticleAdapter.getItemCount();
        callNewsAPI(pageNum, new ArticleCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                mArticleAdapter.addToDataSet(articles);
                mArticleAdapter.notifyItemRangeChanged(startSize, ARTICLES_PER_CALL);
            }

            @Override
            public void onFailure() {
                // TODO: 10/29/17 handle this
            }
        });
    }

    private List<Article> cleanResponse(List<Article> uncleanedResponse) {
        int MIN_CHAR_COUNT_FOR_ARTICLE = 1200; // Articles with char count < this val likely only have a video or audio link which our app doesn't handle.
        //TODO: This also means however that we aren't loading things like comics or single images.
        //Ultimately we want to be able to load videos or audio.


        for (int i = uncleanedResponse.size() - 1; i >= 0; i--) {
            Article article = uncleanedResponse.get(i);

            if (MacWeeklyUtils.isTextEmpty(article.excerpt.rendered) || article.content.rendered.length() < MIN_CHAR_COUNT_FOR_ARTICLE) {

                uncleanedResponse.remove(i);
            }
        }
        return uncleanedResponse;
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
        articleIntent.putExtra(ArticleActivity.ARTICLE_AUTHOR_KEY, "Author name here");
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
