
package hu.ait.macweekly.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Cury {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("href")
    @Expose
    public String href;
    @SerializedName("templated")
    @Expose
    public Boolean templated;

}
