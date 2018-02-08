package hu.ait.macweekly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;

import javax.crypto.Mac;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleActivity extends AppCompatActivity {

    // Keys
    public static final String ARTICLE_TITLE_KEY = "articleTitleKey";
    public static final String ARTICLE_AUTHOR_KEY = "articleAuthorKey";
    public static final String ARTICLE_DATE_KEY = "articleDateKey";
    public static final String ARTICLE_CONTENT_KEY = "articleContentKey";
    public static final String ARTICLE_LINK_KEY = "articleLinkKey";

    // Views
    @BindView(R.id.article_content) TextView mContentTextView;
    @BindView(R.id.article_title) TextView mTitleView;
    @BindView(R.id.article_date) TextView mDateView;
    @BindView(R.id.article_author) TextView mAuthorView;

    // Data
    String mTitleData;
    String mAutorData;
    String mDateData;
    String mContentData;
    String mLinkData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView();

        cancelActivityIfBadIntent();

        populateDataMembers();

        bindArticleViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.share_article) {
            
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void bindArticleViews() {
        mContentTextView.setText(HTMLCompat.getInstance(getApplicationContext()).fromImageHtml(mContentData, mContentTextView, this));
        mTitleView.setText(HTMLCompat.getInstance(getApplicationContext()).fromHtml(mTitleData));
        mDateView.setText(mDateData);
        mAuthorView.setText(mAutorData); // TODO: 10/30/17 Get author info
    }

    private void populateDataMembers() {
        Intent validIntent = getIntent();
        mTitleData = validIntent.getStringExtra(ARTICLE_TITLE_KEY);

        String authorText = validIntent.getStringExtra(ARTICLE_AUTHOR_KEY);
        if(authorText != null) mAutorData = authorText; //Currently the mac weekly is a mess and when they don't have guest authors stored the title is stored here instead :/
        else mAutorData = "";

        String tempDateVal = validIntent.getStringExtra(ARTICLE_DATE_KEY);
        mDateData = MacWeeklyUtils.formatDateFull(tempDateVal);
        mContentData = validIntent.getStringExtra(ARTICLE_CONTENT_KEY);
        mLinkData = validIntent.getStringExtra(ARTICLE_LINK_KEY);
    }

    private void initContentView() {
        setContentView(R.layout.activity_article);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
    }

    private void cancelActivityIfBadIntent() {
        if (isBadIntent()) {
            finish(); // Don't start the activity
            return;
        }
    }

    private boolean isBadIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(ARTICLE_AUTHOR_KEY) && intent.hasExtra(ARTICLE_DATE_KEY) && intent
                    .hasExtra(ARTICLE_CONTENT_KEY) && intent.hasExtra(ARTICLE_TITLE_KEY) && intent
                    .hasExtra(ARTICLE_LINK_KEY)) {
                return false;
            }
        }
        return true;
    }
}
