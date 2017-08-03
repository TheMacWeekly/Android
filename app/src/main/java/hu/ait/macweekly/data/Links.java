
package hu.ait.macweekly.data;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Links {

    @SerializedName("self")
    @Expose
    public List<Self> self = null;
    @SerializedName("collection")
    @Expose
    public List<Collection> collection = null;
    @SerializedName("about")
    @Expose
    public List<About> about = null;
    @SerializedName("author")
    @Expose
    public List<Author> author = null;
    @SerializedName("replies")
    @Expose
    public List<Reply> replies = null;
    @SerializedName("version-history")
    @Expose
    public List<VersionHistory> versionHistory = null;
    @SerializedName("wp:featuredmedia")
    @Expose
    public List<WpFeaturedmedium> wpFeaturedmedia = null;
    @SerializedName("wp:attachment")
    @Expose
    public List<WpAttachment> wpAttachment = null;
    @SerializedName("wp:term")
    @Expose
    public List<WpTerm> wpTerm = null;
    @SerializedName("curies")
    @Expose
    public List<Cury> curies = null;

}
