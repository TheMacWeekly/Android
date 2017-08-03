package hu.ait.macweekly.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import hu.ait.macweekly.ArticleViewClickListener;
import hu.ait.macweekly.R;

/**
 * Created by Mack on 7/9/2017.
 */

//public class ArticleViewHolder extends RecyclerView.ViewHolder
//        implements View.OnClickListener{
//
//    private ArticleViewClickListener mClickListener;
//
//    @BindView(R.id.title) TextView title;
//    @BindView(R.id.summary) TextView sum;
//    @BindView(R.id.date) TextView date;
//
//    public ArticleViewHolder(View view, ArticleViewClickListener listener) {
//        this.mClickListener = listener;
//        super(view);
//        ButterKnife.bind(this, view);
//        view.setOnClickListener(this);
//
//    }
//
//    @Override
//    public void onClick(View view) {
//        mClickListener.onItemClick(getAdapterPosition(), view);
//        System.out.println();
//    }
//}
