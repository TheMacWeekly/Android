package hu.ait.macweekly.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.ait.macweekly.HTMLCompat;
import hu.ait.macweekly.MacWeeklyUtils;
import hu.ait.macweekly.listeners.ArticleViewClickListener;
import hu.ait.macweekly.R;
import hu.ait.macweekly.data.Article;
import hu.ait.macweekly.listeners.EndlessRecyclerViewScrollListener;

/**
 * Created by Mack on 7/4/2017.
 */

public class ArticleRecyclerAdapter extends RecyclerView.Adapter<RecyclerView
        .ViewHolder>{

    private static String LOG_TAG = "NEWSFEEDADAPTER";

    private List<Article> mDataSet;
    private Context mContext;
    private static ArticleViewClickListener mArticleClickListener;
    private EndlessRecyclerViewScrollListener.ParamManager mParamManager;

    public ArticleRecyclerAdapter (Context context, ArticleViewClickListener articleClickListener,
                                   EndlessRecyclerViewScrollListener.ParamManager paramManager) {
        this.mContext = context;
        this.mArticleClickListener = articleClickListener;
        this.mParamManager = paramManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case 0:
                View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_feed_header,
                        parent, false);
                HeaderViewHolder headerViewHolder = new HeaderViewHolder(headerView);
                return headerViewHolder;
            case 1:
                View articleView = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_list_item,
                        parent, false);
                ArticleViewHolder articleViewHolder = new ArticleViewHolder(articleView);
                return articleViewHolder;
            default:
                Log.e(LOG_TAG, "Invalid viewholder in onCreateViewHolder for newsfeed adapter");
                return null; //If it gets here, not good.
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            //This position is reserved for the header. No work needs to be done here. This is
            //just for readability
            HeaderViewHolder headerVH = (HeaderViewHolder) holder;

            String headerText = "All Stories";
            if(mParamManager.usingSearchStr()) {
                headerText = "Search: "+mParamManager.getSearchStr();
            } else if(mParamManager.usingCatId()) {
                headerText = "Cateogry: "+mParamManager.getCatString(mParamManager.getCatId());
            }
            headerVH.headerText.setText(headerText);
        } else {
            ArticleViewHolder summaryViewHolder = (ArticleViewHolder) holder;
            Article article = mDataSet.get(position-1);

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
    }

    @Override
    public int getItemCount() {
        return mDataSet != null || mDataSet.size() == 0 ? mDataSet.size() + 1 : 0;
    }

    public void setDataSet(List<Article> newArticles) {
        mDataSet = newArticles;
        notifyDataSetChanged();
    }

    public void addToDataSet(List<Article> newArticles) {
        int oldIndexEnd = mDataSet.size() - 1;
        mDataSet.addAll(newArticles);
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
            mArticleClickListener.articleViewClicked(view, this.getLayoutPosition()-1);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.header_text) TextView headerText;

        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public List<Article> getDataSet() {
        return mDataSet;
    }

    public void clearDataSet() {
        mDataSet.clear();
    }
}
