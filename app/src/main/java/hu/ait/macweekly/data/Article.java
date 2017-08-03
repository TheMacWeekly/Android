
package hu.ait.macweekly.data;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Article {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("date")
    @Expose
    public String date;
    @SerializedName("date_gmt")
    @Expose
    public String dateGmt;
    @SerializedName("guid")
    @Expose
    public Guid guid;
    @SerializedName("modified")
    @Expose
    public String modified;
    @SerializedName("modified_gmt")
    @Expose
    public String modifiedGmt;
    @SerializedName("slug")
    @Expose
    public String slug;
    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("link")
    @Expose
    public String link;
    @SerializedName("title")
    @Expose
    public Title title;
    @SerializedName("content")
    @Expose
    public Content content;
    @SerializedName("excerpt")
    @Expose
    public Excerpt excerpt;
    @SerializedName("author")
    @Expose
    public Integer author;
    @SerializedName("featured_media")
    @Expose
    public Integer featuredMedia;
    @SerializedName("comment_status")
    @Expose
    public String commentStatus;
    @SerializedName("ping_status")
    @Expose
    public String pingStatus;
    @SerializedName("sticky")
    @Expose
    public Boolean sticky;
    @SerializedName("template")
    @Expose
    public String template;
    @SerializedName("format")
    @Expose
    public String format;
    @SerializedName("meta")
    @Expose
    public List<Object> meta = null;
    @SerializedName("categories")
    @Expose
    public List<Integer> categories = null;
    @SerializedName("tags")
    @Expose
    public List<Integer> tags = null;
    @SerializedName("_links")
    @Expose
    public Links links;

}
