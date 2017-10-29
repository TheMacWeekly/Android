package hu.ait.macweekly;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

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
        mContentTextView.setText(Html.fromHtml(mContentData));
        mTitleView.setText(Html.fromHtml(mTitleData));
        mDateView.setText(mDateData);
        mAuthorView.setText("Needto Make. Work");
    }

    private void populateDataMembers() {
        Intent validIntent = getIntent();
        mTitleData = validIntent.getStringExtra(ARTICLE_TITLE_KEY);
        mAutorData = validIntent.getStringExtra(ARTICLE_AUTHOR_KEY);
        mDateData = validIntent.getStringExtra(ARTICLE_DATE_KEY);
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
