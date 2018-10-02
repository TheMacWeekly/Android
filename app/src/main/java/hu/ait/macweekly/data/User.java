package hu.ait.macweekly.data;

import com.google.firebase.auth.FirebaseUser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import hu.ait.macweekly.MacWeeklyUtils;

public class User {
    @SerializedName("uid")
    @Expose
    public String uid;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("isGuest")
    @Expose
    public boolean isGuest;
    @SerializedName("isAlumni")
    @Expose
    public boolean isAlumni;
    @SerializedName("hasAlumniAccount")
    @Expose
    public boolean hasAlumniAccount;

    public User() {}

    public User(FirebaseUser user) {
        this.uid = user.getUid();
        this.email = user.getEmail();
        this.name = user.getDisplayName() == null ? (this.email == null ? "The Mac Weekly" : this.email) : user.getDisplayName();
        this.isGuest = user.isAnonymous();
        this.isAlumni = !this.isGuest && !MacWeeklyUtils.isMacalesterEmail(this.email);
        this.hasAlumniAccount = false;
    }
}
