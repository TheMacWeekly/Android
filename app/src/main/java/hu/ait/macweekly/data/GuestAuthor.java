package hu.ait.macweekly.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mackhartley on 1/31/18.
 */

public class GuestAuthor {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("slug")
    @Expose
    public String slug;
    @SerializedName("mail")
    @Expose
    public String mail;
    @SerializedName("show_mail")
    @Expose
    public String showMail;
    @SerializedName("link")
    @Expose
    public String link;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("img")
    @Expose
    public String img;
    @SerializedName("job")
    @Expose
    public String job;
    @SerializedName("company")
    @Expose
    public String company;
    @SerializedName("company_link")
    @Expose
    public String companyLink;
    @SerializedName("bio")
    @Expose
    public String bio;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("facebook")
    @Expose
    public String facebook;
    @SerializedName("twitter")
    @Expose
    public String twitter;
    @SerializedName("linkedin")
    @Expose
    public String linkedin;
    @SerializedName("googleplus")
    @Expose
    public String googleplus;
    @SerializedName("youtube")
    @Expose
    public String youtube;
}
