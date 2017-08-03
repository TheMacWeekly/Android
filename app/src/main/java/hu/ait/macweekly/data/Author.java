
package hu.ait.macweekly.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Author {

    @SerializedName("embeddable")
    @Expose
    public Boolean embeddable;
    @SerializedName("href")
    @Expose
    public String href;

}
