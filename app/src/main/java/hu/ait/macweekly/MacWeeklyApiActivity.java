package hu.ait.macweekly;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.BindView;
import hu.ait.macweekly.data.Article;
import hu.ait.macweekly.listeners.EndlessRecyclerViewScrollListener;
import hu.ait.macweekly.network.NewsAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mackhartley on 3/25/18.
 */

public abstract class MacWeeklyApiActivity extends BaseActivity {

    /**
     * NOTE: Classes that extend this abstract class are expected to have two properties.
     *      1) They will be using the wordpress API to draw some sort of scrolling data from WP API
     *      2) They contain the main_content scroll recycler view
     *
     *      Not meeting both of these properties means you should probably make/extend a different class
     */

    // Constants
    private final String LOG_TAG = "MacWeeklyAPIActivity - ";
    public static final int ARTICLES_PER_CALL = 25;

    // Keys
    protected final static int SC_MAINCONTENT = -1;
    protected final static int SC_ERROR = 0;
    protected final static int SC_NORESULT = 1;
    protected final static int SC_STARTSEARCH = 2;

    // Members
    private NewsAPI newsAPI;
    private boolean showingPrimaryContent = false;

    public @BindView(R.id.main_content) RecyclerView mMainContent;
    public @Nullable @BindView(R.id.no_results_view) LinearLayout mNoResults;
    public @Nullable @BindView(R.id.screenhint_start_search) LinearLayout mStartSearch;
    public @Nullable @BindView(R.id.newsFeedErrorView) LinearLayout mErrorView;

    // Code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareNewsAPI();
    }

    protected void prepareNewsAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://themacweekly.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        newsAPI = retrofit.create(NewsAPI.class);
    }

    public interface ArticleCallback {
        void onSuccess(List<Article> articles);
        void onFailure();
    }

    protected void callNewsAPI(final int pageNum, int categoryId, String searchStr, final ArticleCallback articleCallback) {
        final Call<List<Article>> articleCall;

        boolean hasCategory = categoryId != EndlessRecyclerViewScrollListener.ParamManager.NO_CATEGORY;
        boolean hasSearch = !searchStr.equals(EndlessRecyclerViewScrollListener.ParamManager.NO_SEARCH);

        // Here we build our articleCall based on what information is passed to us
        if(hasCategory && hasSearch) { // If we have category or search string, use those...
            articleCall = newsAPI.getArticles(pageNum, ARTICLES_PER_CALL, categoryId, searchStr);
        } else if(hasCategory) {
            articleCall = newsAPI.getArticles(pageNum, ARTICLES_PER_CALL, categoryId);
        } else if(hasSearch) {
            articleCall = newsAPI.getArticles(pageNum, ARTICLES_PER_CALL, searchStr);
        } else {
            articleCall = newsAPI.getArticles(pageNum, ARTICLES_PER_CALL);
        }

        Log.d(LOG_TAG, "Sent article api call ----------------");
        articleCall.enqueue(new Callback<List<Article>>() {
            @Override
            public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {

                gotCallReturned();

                if (response.body() != null && response.body().size() > 0) {
                    Log.d(LOG_TAG, "Got response back. Page: "+pageNum+" -----------------");

                    List<Article> uncleanedResponse = response.body();
                    List<Article> cleanedResponse = cleanResponse(uncleanedResponse);

                    onResponseSuccess(pageNum, cleanedResponse);

                    articleCallback.onSuccess(cleanedResponse);

                } else {
                    Log.e(LOG_TAG, "api response body is null. Page: "+pageNum);
                    onResponseSuccessEmptyBody(pageNum);
                }
            }

            @Override
            public void onFailure(Call<List<Article>> call, Throwable t) {
                Log.e(LOG_TAG, "call failed. Could not retrieve page. Page: "+pageNum);
                onResponseFailure(pageNum);
                articleCallback.onFailure();
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

    abstract void gotCallReturned();

    abstract void onResponseSuccess(int pageNum, List<Article> newArticles);

    abstract void onResponseSuccessEmptyBody(int pageNum);

    abstract void onResponseFailure(int pageNum);

    public void showScreenHint(int screen) {
        if(mErrorView != null) mErrorView.setVisibility(View.GONE);
        if(mNoResults != null) mNoResults.setVisibility(View.GONE);
        if(mStartSearch != null) mStartSearch.setVisibility(View.GONE);

        mMainContent.setVisibility(View.GONE);
        showingPrimaryContent = false;

        switch(screen) {
            case 0:
                try {
                    mErrorView.setVisibility(View.VISIBLE);
                } catch(Exception e) {
                    Log.e(LOG_TAG, "Did not include referenced screenhint layout in activity");
                }
                break;
            case 1:
                try {
                    mNoResults.setVisibility(View.VISIBLE);
                } catch(Exception e) {
                    Log.e(LOG_TAG, "Did not include referenced screenhint layout in activity");
                }
                break;
            case 2:
                try {
                    mStartSearch.setVisibility(View.VISIBLE);
                } catch(Exception e) {
                    Log.e(LOG_TAG, "Did not include referenced screenhint layout in activity");
                }
                break;
            default:
                try {
                    mMainContent.setVisibility(View.VISIBLE);
                    showingPrimaryContent = true;
                } catch(Exception e) {
                    Log.e(LOG_TAG, "Did not find R.id.main_content recyclerview in layouts");
                }
                break;
        }
    }



}
