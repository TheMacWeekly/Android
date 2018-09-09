package hu.ait.macweekly;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import org.immutables.value.Value;

import java.util.List;
import java.util.concurrent.Callable;

import hu.ait.macweekly.data.Article;
import hu.ait.macweekly.network.NewsAPI;
import okhttp3.HttpUrl;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MacWeeklyAPI {
    public static HttpUrl baseURL;
    static {
        baseURL = new HttpUrl.Builder()
                .scheme("https")
                .host("themacweekly.com")
                .build();
    }

    @Value.Immutable
    public static abstract class Test {
        public abstract int num();
    }

    @Value.Immutable
    public static abstract class Posts {
        private static NewsAPI apiClient;
        static {
            apiClient = new Retrofit.Builder()
                    .baseUrl(MacWeeklyAPI.baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(NewsAPI.class);
        }

        public static final int DEFAULT_PAGE_SIZE = 25;

        public static HttpUrl baseURL = MacWeeklyAPI.baseURL.resolve("wp-json/wp/v2/posts");

        public enum Category {
            NEWS,
            SPORTS,
            FEATURES,
            OPINION,
            ARTS,
            FOOD_AND_DRINK;

            public int toAPIID() {
                switch (this) {
                    case NEWS:
                        return 3;
                    case SPORTS:
                        return 5;
                    case FEATURES:
                        return 4;
                    case OPINION:
                        return 7;
                    case ARTS:
                        return 6;
                    case FOOD_AND_DRINK:
                        return 28;
                }
                throw new UnsupportedOperationException("Unsupported enum value");
            }

            public static Category fromAPIID(int id) {
                switch(id) {
                    case 3:
                        return NEWS;
                    case 5:
                        return SPORTS;
                    case 4:
                        return FEATURES;
                    case 7:
                        return OPINION;
                    case 6:
                        return ARTS;
                    case 28:
                        return FOOD_AND_DRINK;
                }

                throw new UnsupportedOperationException("Unsupported enum value");
            }

            public String getCatString() {
                switch(this) {
                    case NEWS:
                        return "News";
                    case SPORTS:
                        return "Sports";
                    case FEATURES:
                        return "Features";
                    case OPINION:
                        return "Opinion";
                    case ARTS:
                        return "Arts";
                    case FOOD_AND_DRINK:
                        return "Food & Drink";
                }
                throw new UnsupportedOperationException("Unsupported enum value");
            }
        }

        public retrofit2.Call<List<Article>> query(Callable<List<Article>> onSuccess) {
            HttpUrl.Builder url = baseURL.newBuilder();

            return this.apiClient.getArticles(this.pageNum(),
                    this.pageSize(),
                    this.category().transform(new Function<Category, Integer>() {
                        @Override
                        public Integer apply(Category cat) {
                            return Integer.valueOf(cat.toAPIID());
                        }
                    }).orNull(),
                    this.searchString().orNull());
        }

        abstract public Optional<String> searchString();
        abstract public Optional<Category> category();

        @Value.Default
        public int pageNum() {
            return 0;
        }
        @Value.Default
        public int pageSize() {
            return DEFAULT_PAGE_SIZE;
        }

    }

}
