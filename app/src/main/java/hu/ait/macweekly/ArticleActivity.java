package hu.ait.macweekly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ShareActionProvider;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleActivity extends BaseActivity {

    // Keys
    public static final String ARTICLE_TITLE_KEY = "articleTitleKey";
    public static final String ARTICLE_AUTHOR_KEY = "articleAuthorKey";
    public static final String ARTICLE_DATE_KEY = "articleDateKey";
    public static final String ARTICLE_CONTENT_KEY = "articleContentKey";
    public static final String ARTICLE_LINK_KEY = "articleLinkKey";
    public static final String AUTHOR_IMG_URL_KEY = "authorImgUrlKey";
    public static final String AUTHOR_BIO_KEY = "authorBioKey";

    // Views
    @BindView(R.id.article_title) TextView mTitleView;
    @BindView(R.id.article_date) TextView mDateView;
    @BindView(R.id.article_author) TextView mAuthorView;
    @BindView(R.id.author_snipit_image) ImageView mAuthorSnipitImgView;
    @BindView(R.id.author_snipit_name) TextView mAuthorSnipitNameView;
    @BindView(R.id.author_snipit_bio) TextView mAuthorBioView;
    @BindView(R.id.author_snipit) CardView mAuthorSnipitView;
    @BindView(R.id.webview) WebView webView;

    // Data
    String mTitleData;
    String mAuthorName;
    String mDateData;
    String mContentData;
    String mLinkData;
    String mAuthorUrl;
    String mAuthorBio;
    ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView();

        cancelActivityIfBadIntent();

        populateDataMembers();

        bindArticleViews();

        setUpBackButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_article, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem shareItem = menu.findItem(R.id.action_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        // Return true to display menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_share) {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, mLinkData);
            mShareActionProvider.setShareIntent(sendIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void bindArticleViews() {
        mTitleView.setText(HTMLCompat.getInstance(getApplicationContext()).fromHtml(mTitleData));
        mDateView.setText(mDateData);
        mAuthorView.setText(mAuthorName);
        if(!MacWeeklyUtils.isTextEmpty(mAuthorUrl)) {
            mAuthorSnipitView.setVisibility(View.VISIBLE);
            Glide.with(this).load(mAuthorUrl).into(mAuthorSnipitImgView);
            mAuthorSnipitNameView.setText(mAuthorName);
            mAuthorBioView.setText(HTMLCompat.getInstance(this).fromHtml(mAuthorBio));
        } else {
            mAuthorSnipitView.setVisibility(View.GONE);
        }
    }

    private void populateDataMembers() {
        Intent validIntent = getIntent();
        mTitleData = validIntent.getStringExtra(ARTICLE_TITLE_KEY);

        String authorText = validIntent.getStringExtra(ARTICLE_AUTHOR_KEY);
        if(authorText != null) mAuthorName = authorText; //Currently the mac weekly is a mess and when they don't have guest authors stored the title is stored here instead :/
        else mAuthorName = "";

        String authorBio = validIntent.getStringExtra(AUTHOR_BIO_KEY);
        if(authorBio != null) mAuthorBio = authorBio;
        else mAuthorBio = "";

        String authorImgUrl = validIntent.getStringExtra(AUTHOR_IMG_URL_KEY);
        if(authorImgUrl != null) {
            mAuthorUrl = authorImgUrl;
            mAuthorSnipitView.setVisibility(View.VISIBLE);
        } else {
            mAuthorUrl = "";
            mAuthorSnipitView.setVisibility(View.GONE);
        }

        String tempDateVal = validIntent.getStringExtra(ARTICLE_DATE_KEY);
        mDateData = MacWeeklyUtils.formatDateFull(tempDateVal);
        mContentData = validIntent.getStringExtra(ARTICLE_CONTENT_KEY);
        mLinkData = validIntent.getStringExtra(ARTICLE_LINK_KEY);

        // Set up webview for watching videos
        String data = formatHtmlForMobile(mContentData);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true); // It has a warning about this. Ignore it. It allows us to play video
        webView.loadDataWithBaseURL(mLinkData, data,"text/html", "UTF-8", null);
    }

    // TODO: 4/4/18 Move this to Kotlin since it has multi line support. This is ugly. Java is dumb for not supporting multiline strings
    private String formatHtmlForMobile(String mContentData) {

        String cleanedContent = mContentData.replaceAll("]<a.*>\\d<\\/a>", ""); // Gets rid of the weird ]1 and ]2 etc tags the mac weekly puts under photos. DOESNT WORK
        cleanedContent = cleanedContent.replaceAll("<iframe.*soundcloud.*<\\/iframe>", ""); // TODO: 4/6/18 add soundcloud support?

        return "<html>" +
        "    <head>" +
        "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
"" +
        "        <style>" +
        "body {" +
        "    font-family: sans-serif;" +
        "    margin:0;" +
        "}" +
        "img {" +
        "    max-width: 100%;" +
        "    height: auto;" +
        "    margin-top: 0.5em;" +
        "    margin-bottom: 0.5em;" +
        "}" +
        "figure {" +
        "    margin-left: 0.5em;" +
        "    margin-right: 0.5em;" +
        "}" +
        "figcaption {" +
        "    color:  #999999;" +
        "    font-size: 12pt;" +
        "}" +
"" +
        "p {" +
        "    font-size: 14pt;" +
        "    line-height: 1.25;" +
        "    margin-bottom: 1em;" +
        "}" +
        "        </style>" +
                "        <script>\n" +
                "            window.onload = function() {\n" +
                "                for (let frame of document.querySelectorAll(\"iframe\")) {\n" +
                "                    let ratio = document.body.clientWidth / frame.width\n" +
                "                    frame.width = ratio * frame.width\n" +
                "                    frame.height = ratio * frame.height\n" +
                "                }\n" +
                "            }\n" +
                "        </script>"+
        "    </head>" +
        "    <body>" +
        cleanedContent +
        "        </body>" +
        "</html>";
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
