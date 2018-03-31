package hu.ait.macweekly;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;

public class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;
    private View rootLayout;
    private BroadcastReceiver mBroadcastRec;
    private Snackbar mConnectIssueBar;

    protected void setUpBaseActMembers(View rootLayout) {
        this.rootLayout = rootLayout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBroadcastRec = new ConnectivityIssueReceiver(this);

        registerBroadcastReciever();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialConnectionCheck();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregReciever();
    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.wait));
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public void showSnackbar(String text, int duration) {
        Snackbar.make(rootLayout , text, duration).show();
    }


    private void initialConnectionCheck() {
        if(Settings.System.getInt(this.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0) {
            getConnectBar().show();
        } else {
            getConnectBar().dismiss();
        }
    }

    public Snackbar getConnectBar() {
        if(mConnectIssueBar == null) {
            View parentView = findViewById(android.R.id.content);
            mConnectIssueBar = Snackbar.make(parentView, "Cannot connect to network...", Snackbar.LENGTH_INDEFINITE);
            View sbView = mConnectIssueBar.getView();
            TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            sbView.setBackgroundColor(getApplication().getResources().getColor(R.color.colorPrimaryDark));
        }
        return mConnectIssueBar;
    }

    private void registerBroadcastReciever() {

        IntentFilter intentFilter = new IntentFilter("android.intent.action.AIRPLANE_MODE");

        getApplicationContext().registerReceiver(mBroadcastRec, intentFilter);
    }

    private void unregReciever() {
        if(mBroadcastRec != null)
            getApplicationContext().unregisterReceiver(mBroadcastRec);
    }

    protected void setUpBackButton() {
        Drawable drawable= ResourcesCompat.getDrawable(this.getResources(), R.drawable.arrow_left, null);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(drawable);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}