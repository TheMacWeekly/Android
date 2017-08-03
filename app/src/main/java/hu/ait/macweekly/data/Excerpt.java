
package hu.ait.macweekly.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Excerpt {

    @SerializedName("rendered")
    @Expose
    public String rendered;
    @SerializedName("protected")
    @Expose
    public Boolean _protected;

}
