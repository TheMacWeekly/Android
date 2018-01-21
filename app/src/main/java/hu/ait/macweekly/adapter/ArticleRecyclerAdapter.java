package hu.ait.macweekly.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.ait.macweekly.listeners.ArticleViewClickListener;
import hu.ait.macweekly.R;
import hu.ait.macweekly.data.Article;

/**
 * Created by Mack on 7/4/2017.
 */

public class ArticleRecyclerAdapter extends RecyclerView.Adapter<ArticleRecyclerAdapter
        .ArticleViewHolder>{

    private List<Article> mDataSet;
    private Context mContext;
    private static ArticleViewClickListener mArticleClickListener;

//    private View.OnClickListener mArticleClickListener;

    public ArticleRecyclerAdapter (Context context, ArticleViewClickListener
            articleClickListener) {
        this.mContext = context;
        this.mArticleClickListener = articleClickListener;
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_list_item,
                parent, false);
        ArticleViewHolder articleViewHolder = new ArticleViewHolder(view);
        return articleViewHolder;
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {
        Article article = mDataSet.get(position);

        holder.title.setText(article.title.rendered);
        holder.sum.setText(Html.fromHtml(article.excerpt.rendered));

        String tempDateVal = article.date;
        holder.date.setText(tempDateVal.replaceAll("T.*", ""));
    }

    @Override
    public int getItemCount() {
        return mDataSet != null ? mDataSet.size() : 0;
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

        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.summary) TextView sum;
        @BindView(R.id.date) TextView date;

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
}
