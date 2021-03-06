package hu.ait.macweekly;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.*;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import hu.ait.macweekly.adapter.NewsFeedRecyclerAdapter;
import hu.ait.macweekly.data.Article;
import hu.ait.macweekly.data.GuestAuthor;
import hu.ait.macweekly.data.User;
import hu.ait.macweekly.listeners.ArticleViewClickListener;
import hu.ait.macweekly.listeners.EndlessRecyclerViewScrollListener;

public class MainActivity extends MacWeeklyApiActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ArticleViewClickListener {


    boolean showingNewsFeed = false;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;

    // Constants
    private final String LOG_TAG = "MainActivity - ";

    // Members
    private NewsFeedRecyclerAdapter mArticleAdapter;
    private EndlessRecyclerViewScrollListener mEndlessScrollListener;

    // Views
    @BindView(R.id.refresh_view) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.errorButton) Button mErrorButtonView;


    private static Context mContext;

    public static Context getAppContext(){
        return mContext;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    // Code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
        mContext = this.getApplicationContext();

        initContentViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prepareDrawer(toolbar);

        prepareNewsAPI();

        prepareContentViews();
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

        mArticleAdapter = new NewsFeedRecyclerAdapter(getApplicationContext(), this, paramManager);
        mArticleAdapter.setDataSet(new ArrayList<Article>());

        mMainContent.setLayoutManager(linearLayoutManager);
        mMainContent.setAdapter(mArticleAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                callNewsAPI();
                resetArticlesKeepCategory();
            }
        });
//        mSwipeRefreshLayout.post(new Runnable() { // TODO: 10/29/17 Need this?
//            @Override
//            public void run() {
//                mSwipeRefreshLayout.setRefreshing(true);
//            }
//        });

        mMainContent.addOnScrollListener(mEndlessScrollListener);

        mErrorButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetArticlesKeepCategory();
//                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void initContentViews() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    private void prepareDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void updateUI() {

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        final Menu menu = navigationView.getMenu();

        View headerView = navigationView.getHeaderView(0);
        final TextView navUsername = headerView.findViewById(R.id.navUsername);

        @NonNull final FirebaseUser firebaseUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser());
        mUsersDatabaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    User user = dataSnapshot.getValue(User.class);
                    navUsername.setText(user.name);
                    if (user.isGuest) {
                        menu.findItem(R.id.nav_login).setVisible(true);
                        menu.findItem(R.id.nav_signOut).setVisible(false);
                    }
                    else {
                        menu.findItem(R.id.nav_login).setVisible(false);
                        menu.findItem(R.id.nav_signOut).setVisible(true);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // TODO: 3/31/18 Fix this! banner logo does not exist
//    public void onBannerPressedScrollUp() {
//        ImageView bannerImg = (ImageView) findViewById(R.id.banner_logo);
//        bannerImg.setClickable(true);
//        bannerImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mMainContent.smoothScrollToPosition(0);
//            }
//        });
//    }

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
            resetArticlesKeepCategory();
            return true;
        }else if (id == R.id.about_page) {
            goToAboutPage();
            return true;
        }else if (id == R.id.action_feedback) {
            sendFeedback();
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToAboutPage() {
        Intent aboutPageIntent = new Intent(this, AboutPage.class);
        startActivity(aboutPageIntent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            startSearchActivity();
        } else if (id == R.id.nav_allStories) { // TODO: 3/31/18 Find better way than hardcoding this
            resetArticlesClear();
        } else if (id == R.id.nav_news) {
            resetArticlesWithCategory(3);
        } else if (id == R.id.nav_sports) {
            resetArticlesWithCategory(5);
        } else if (id == R.id.nav_features) {
            resetArticlesWithCategory(4);
        } else if (id == R.id.nav_opinion) {
            resetArticlesWithCategory(7);
        } else if (id == R.id.nav_arts) {
            resetArticlesWithCategory(6);
        } else if (id == R.id.nav_foodDrink) {
            resetArticlesWithCategory(28);
        } else if(id == R.id.nav_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(i, 0);
        } else if (id == R.id.nav_signOut || id == R.id.nav_login) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startSearchActivity() {
        Intent searchActivity = new Intent(this, SearchActivity.class);
        startActivity(searchActivity);
    }

    private void resetArticlesClear() {
        resetArticles(EndlessRecyclerViewScrollListener.ParamManager.NO_CATEGORY, EndlessRecyclerViewScrollListener.ParamManager.NO_SEARCH);
    }

    private void resetArticlesKeepCategory() {
        resetArticles(mEndlessScrollListener.getParamManager().getCatId(), EndlessRecyclerViewScrollListener.ParamManager.NO_SEARCH);
    }

    private void resetArticlesWithCategory(int categoryId) {
        resetArticles(categoryId, EndlessRecyclerViewScrollListener.ParamManager.NO_SEARCH);
    }

    private void resetArticlesWithSearch(String searchString) {
        resetArticles(EndlessRecyclerViewScrollListener.ParamManager.NO_CATEGORY, searchString);
    }

    private void resetArticlesWithCatAndSearch(int categoryId, String searchString) {
        resetArticles(categoryId, searchString);
    }

    private void resetArticles(int categoryId, String searchString) {
        mSwipeRefreshLayout.setRefreshing(true);
        mArticleAdapter.clearDataSet();
        mArticleAdapter.notifyDataSetChanged();
        showScreenHint(SC_MAINCONTENT);
        mEndlessScrollListener.resetState(mMainContent, categoryId, searchString);
    }

    private void addArticles(int pageNum, int categoryId, String searchString) {
        final int startSize = mArticleAdapter.getItemCount();
        MacWeeklyApiActivity.ArticleCallback articleCallback = new MacWeeklyApiActivity.ArticleCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                mArticleAdapter.addToDataSet(articles);
                mArticleAdapter.notifyItemRangeChanged(startSize, ARTICLES_PER_CALL);
                mEndlessScrollListener.setLoading(true);
            }

            @Override
            public void onFailure() {
                mEndlessScrollListener.setLoading(false);
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

    }

    @Override
    void onResponseFailure(int pageNum) {
        if (mArticleAdapter.getDataSet().size() == 0) showScreenHint(SC_ERROR);
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
