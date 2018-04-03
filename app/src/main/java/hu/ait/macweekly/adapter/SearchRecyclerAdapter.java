package hu.ait.macweekly.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.ait.macweekly.HTMLCompat;
import hu.ait.macweekly.MacWeeklyUtils;
import hu.ait.macweekly.R;
import hu.ait.macweekly.data.Article;
import hu.ait.macweekly.listeners.ArticleViewClickListener;
import hu.ait.macweekly.listeners.EndlessRecyclerViewScrollListener;

/**
 * Created by mackhartley on 3/25/18.
 */

public class SearchRecyclerAdapter extends RecyclerView.Adapter<RecyclerView
        .ViewHolder> {

    private static String LOG_TAG = "SEARCH_ADAPTER - ";

    private List<Article> mDataSet;
    private Context mContext;
    private static ArticleViewClickListener mArticleClickListener;
    private EndlessRecyclerViewScrollListener.ParamManager mParamManager;

    public SearchRecyclerAdapter(Context context, ArticleViewClickListener articleClickListener,
                                   EndlessRecyclerViewScrollListener.ParamManager paramManager) {
        this.mContext = context;
        this.mArticleClickListener = articleClickListener;
        this.mParamManager = paramManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View articleView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_article_view,
                parent, false);
        ArticleViewHolder articleViewHolder = new ArticleViewHolder(articleView);
        return articleViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ArticleViewHolder summaryViewHolder = (ArticleViewHolder) holder;
        Article article = mDataSet.get(position);

        // Load title
        summaryViewHolder.title.setText(HTMLCompat.getInstance(mContext).fromHtml(article.title.rendered));

        // Load excerpt
        summaryViewHolder.sum.setText(HTMLCompat.getInstance(mContext).fromHtml(article.excerpt.rendered));

        // Load the date, get it in a nice readable format first
        String tempDateVal = article.date;
        String correctlyFormattedDate = MacWeeklyUtils.formatDateTimeAgo(tempDateVal);
        summaryViewHolder.date.setText(correctlyFormattedDate);

        // Load guest author name if it exists. If not just put Mac Weekly as Author
        String authorName;
        if(article.guestAuthor == null || MacWeeklyUtils.isTextEmpty(article.guestAuthor.name)) authorName = "The Mac Weekly";
        else authorName = article.guestAuthor.name;
        summaryViewHolder.author.setText(HTMLCompat.getInstance(mContext).fromHtml(authorName));

        // Load image thumbnail if url exists
        if(!MacWeeklyUtils.isTextEmpty(article.normalThumbnailUrl)) {
            String fullImgUrl = article.normalThumbnailUrl;
            Glide.with(mContext).load(fullImgUrl).into(summaryViewHolder.thumbnailView);
            summaryViewHolder.thumbnailView.setVisibility(View.VISIBLE);
        } else {
            Glide.clear(summaryViewHolder.thumbnailView);
            summaryViewHolder.thumbnailView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setDataSet(List<Article> newArticles) {
        mDataSet = newArticles;
        notifyDataSetChanged();
    }

    public void addToDataSet(List<Article> newArticles) {
        int oldIndexEnd = mDataSet.size() - 1;
        mDataSet.addAll(newArticles);
//        notifyDataSetChanged();
        notifyItemRangeChanged(oldIndexEnd, mDataSet.size());
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        @BindView(R.id.title) TextView title;
        @BindView(R.id.summary) TextView sum;
        @BindView(R.id.date) TextView date;
        @BindView(R.id.authorField) TextView author;
        @BindView(R.id.articleThumbnail) ImageView thumbnailView;

        public ArticleViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mArticleClickListener.articleViewClicked(view, this.getLayoutPosition());
        }
    }

    public List<Article> getDataSet() {
        return mDataSet;
    }

    public void clearDataSet() {
        mDataSet.clear();
    }
}
