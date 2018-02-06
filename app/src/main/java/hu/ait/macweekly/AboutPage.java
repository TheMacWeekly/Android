package hu.ait.macweekly;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AboutPage extends AppCompatActivity {

    // KEYS
    public static final String VERSION = BuildConfig.VERSION_NAME;

    @BindView(R.id.textView3) TextView version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_page);

        ButterKnife.bind(this);

        version.setText(getString(R.string.version_stub) + VERSION);
    }
}
