package hu.ait.macweekly.network;

import java.util.List;

import hu.ait.macweekly.data.Article;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Mack on 7/3/2017.
 */

public interface NewsAPI {

    @GET("wp-json/wp/v2/posts")
    Call<List<Article>> getArticles(@Query("page") int pageNum, @Query("per_page") int amountPerPage);
    @GET("wp-json/wp/v2/posts")
    Call<List<Article>> getArticles(@Query("page") int pageNum, @Query("per_page") int amountPerPage, @Query("category") int categoryId);
    @GET("wp-json/wp/v2/posts")
    Call<List<Article>> getArticles(@Query("page") int pageNum, @Query("per_page") int amountPerPage, @Query("search") String searchString);
    @GET("wp-json/wp/v2/posts")
    Call<List<Article>> getArticles(@Query("page") int pageNum, @Query("per_page") int amountPerPage, @Query("category") Integer categoryId, @Query("search") String searchString);
}
