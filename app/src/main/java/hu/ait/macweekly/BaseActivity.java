package hu.ait.macweekly;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;


public class BaseActivity extends AppCompatActivity {

    private final int SEND_EMAIL_REQUEST = 0;

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
        View parentView = findViewById(android.R.id.content);
        Snackbar mSnackbar = Snackbar.make(parentView, text, duration);
        View sbView = mSnackbar.getView();
        TextView tv = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        sbView.setBackgroundColor(getApplication().getResources().getColor(R.color.colorPrimaryDark));
        mSnackbar.show();
    }

    public void showAlertDialogue(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final boolean alertVal;

        builder.setMessage(content).setTitle(title).setPositiveButton(R.string.confirm_email_failure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //okay
            }
        });
        AlertDialog dialog = builder.create();

        dialog.show();
    }


    private void initialConnectionCheck() {
        if(Settings.System.getInt(this.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
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

    /**
     * Check that a mail client is present
     */
    public boolean isMailClientPresent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);

        if (list.size() == 0) {
            showSnackbar("No email applications found.", Snackbar.LENGTH_LONG);
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Send email feedback
     */
    public void sendFeedback() {
        final String[] recipients = {"themacweeklyapp@gmail.com"};
        long currentTime = System.currentTimeMillis();
        Resources res = getResources();
        String emailSubject = String.format(res.getString(R.string.feedback_email_subject), currentTime);

        Intent sendFeedbackIntent = new Intent(Intent.ACTION_SEND); //We have to use ACTION_SEND to include a single attachment - via Android dev docs
        sendFeedbackIntent.setType("text/plain");
        sendFeedbackIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
        sendFeedbackIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);

        String phoneDesc;

        FileOutputStream fos = null;
        File reportInfoFile = null;
        try {
            reportInfoFile = new File(getFilesDir(), "tempReportFile.txt");
            fos = openFileOutput("tempReportFile.txt", Context.MODE_PRIVATE);
            String writerString = getPhoneDetailsString();
            fos.write(writerString.getBytes());

        } catch (IOException e) {
            showSnackbar(e.getMessage(), Snackbar.LENGTH_LONG);
        } finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Uri fileUri = FileProvider.getUriForFile(this,
                "hu.ait.macweekly.fileprovider",
                reportInfoFile);

        if (fileUri != null) {
            sendFeedbackIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendFeedbackIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            BaseActivity.this.setResult(Activity.RESULT_OK, sendFeedbackIntent);
            sendFeedbackIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

            String defaultBody = "Please write your thoughts or describe your issue."; //Feel free to change this.

            sendFeedbackIntent.putExtra(Intent.EXTRA_TEXT, defaultBody);

            if (isMailClientPresent()) {
                startActivityForResult(Intent.createChooser(sendFeedbackIntent, res.getString(R.string.email_app_chooser)), SEND_EMAIL_REQUEST);
            }
        }

    }

    /**
     * Format and return a string containing details about
     * the user's phone
     * @return
     */
    public String getPhoneDetailsString() {

        Resources res = getResources();
        StringBuilder writer = new StringBuilder();

        writer.append(String.format(res.getString(R.string.phone_manufacturer), Build.MANUFACTURER));
        writer.append(System.getProperty("line.separator"));

        writer.append(String.format(res.getString(R.string.phone_os), Build.VERSION.RELEASE));
        writer.append(System.getProperty("line.separator"));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        writer.append(String.format(res.getString(R.string.phone_screen_resolution), displayMetrics.heightPixels, displayMetrics.widthPixels));
        writer.append(System.getProperty("line.separator"));

        writer.append(String.format(res.getString(R.string.phone_model), Build.MODEL));
        writer.append(System.getProperty("line.separator"));

        writer.append(String.format(res.getString(R.string.phone_hardware), Build.HARDWARE));
        writer.append(System.getProperty("line.separator"));

        writer.append(String.format(res.getString(R.string.phone_serial), Build.SERIAL));
        writer.append(System.getProperty("line.separator"));

        writer.append(String.format(res.getString(R.string.phone_tags), Build.TAGS));
        writer.append(System.getProperty("line.separator"));

        return writer.toString();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEND_EMAIL_REQUEST) {
            if (resultCode == RESULT_OK) {
                File tempData = new File(getFilesDir(), "tempReportFile.txt");
                if (tempData.exists()) {
                    tempData.delete();
                }
                showSnackbar(getResources().getString(R.string.email_sent), Snackbar.LENGTH_SHORT);
            } else {
                showAlertDialogue(getResources().getString(R.string.email_report_cancel),
                        getResources().getString(R.string.email_not_sent));
            }
        }
    }
}