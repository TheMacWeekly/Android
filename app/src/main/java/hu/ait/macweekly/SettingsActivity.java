package hu.ait.macweekly;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private final int SEND_EMAIL_REQUEST = 0;
    public static SettingsActivity tActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new FragSettingsFragment()).commit();
        tActivity = this;

        getLayoutInflater().inflate(R.layout.app_bar_tool, (ViewGroup)findViewById(android.R.id.content));
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpBackButton();

        // Not working f0r some reason.
        int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin) + 20, getResources().getDisplayMetrics());
        getListView().setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);

    }

    public static class FragSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            Preference logoutPref = (Preference) findPreference("btn_log_out");
            logoutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    logout();
                    Intent i = new Intent(FragSettingsFragment.this.getView().getContext(), LoginActivity.class);
                    startActivity(i);
                    return false;
                }
            });

            Preference sendFeedback = (Preference) findPreference("btn_send_feedback");
            sendFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    long currentTime = System.currentTimeMillis();
                    Resources res = getResources();
                    String mStr = "User Feedback - " + Long.toString(currentTime);
                    tActivity.sendFeedback(mStr);
                    return false;
                }
            });

            Preference bugReport = (Preference) findPreference("btn_report_issue");
            bugReport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.e("hi", "hit");
                    long currentTime = System.currentTimeMillis();
                    Resources res = getResources();
                    String mStr = "Bug Report - " + Long.toString(currentTime);
                    tActivity.sendFeedback(mStr);
                    return false;
                }
            });
        }

        private void logout() {
            //TODO: Implement logout here
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);
            if(v != null) {
                ListView lv = (ListView) v.findViewById(android.R.id.list);
                int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
                int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
                int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) getResources().getDimension(R.dimen.activity_vertical_margin) + 30, getResources().getDisplayMetrics());

                lv.setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
            }
            return v;
        }

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
    public void sendFeedback(String emailSubject) {
        final String[] recipients = {"themacweeklyapp@gmail.com"};
        Resources res = getResources();

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
            SettingsActivity.this.setResult(Activity.RESULT_OK, sendFeedbackIntent);
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
                showAlertDialogue(getResources().getString(R.string.error_title),
                        getResources().getString(R.string.email_not_sent));
            }
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

    private void setUpBackButton() {
        Drawable drawable= ResourcesCompat.getDrawable(this.getResources(), R.drawable.arrow_left, null);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            Log.e("not null", "bar not null");
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(drawable);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.e("clicked", "clicked");
                setResult(0, new Intent());
                super.onBackPressed();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
