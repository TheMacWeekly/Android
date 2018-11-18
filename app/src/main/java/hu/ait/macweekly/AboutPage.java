package hu.ait.macweekly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AboutPage extends AppCompatActivity {

    // KEYS
    public static final String VERSION = BuildConfig.VERSION_NAME;

    @BindView(R.id.textView3) TextView version;
    @BindView(R.id.tvPrivacy) TextView privacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_page);

        ButterKnife.bind(this);

        version.setText(getString(R.string.version_stub)  + " " + VERSION);

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AboutPage.this, PrivacyPolicyActivity.class);
                startActivity(i);

            }
        });
    }
}
