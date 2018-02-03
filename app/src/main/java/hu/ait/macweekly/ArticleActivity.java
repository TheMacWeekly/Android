package hu.ait.macweekly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import javax.crypto.Mac;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleActivity extends AppCompatActivity {

    // Keys
    public static final String ARTICLE_TITLE_KEY = "articleTitleKey";
    public static final String ARTICLE_AUTHOR_KEY = "articleAuthoKey";
    public static final String ARTICLE_DATE_KEY = "articleDateKey";
    public static final String ARTICLE_CONTENT_KEY = "articleContentKey";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView();

        cancelActivityIfBadIntent();

        populateDataMembers();

        bindArticleViews();
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
        if(!authorText.equals(mTitleData)) mAutorData = authorText; //Currently the mac weekly is a mess and when they don't have guest authors stored the title is stored here instead :/
        else mAutorData = "";

        String tempDateVal = validIntent.getStringExtra(ARTICLE_DATE_KEY);
        mDateData = MacWeeklyUtils.formatDateFull(tempDateVal);
        mContentData = validIntent.getStringExtra(ARTICLE_CONTENT_KEY);
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
                    .hasExtra(ARTICLE_CONTENT_KEY) && intent.hasExtra(ARTICLE_TITLE_KEY)) {
                return false;
            }
        }
        return true;
    }
}
