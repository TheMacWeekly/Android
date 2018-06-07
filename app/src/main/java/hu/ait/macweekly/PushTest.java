package hu.ait.macweekly;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PushTest extends BaseActivity {

    private EditText mSubject;
    private EditText mBody;
    private Button mSendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSubject = (EditText) findViewById(R.id.etSubject);
        mBody = (EditText)findViewById(R.id.etBody);
        mSendBtn = (Button) findViewById(R.id.btnPush);

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subject = mSubject.getText().toString().trim();
                String body = mBody.getText().toString().trim();
                sendPushNotification(subject, body);
            }
        });
    }
}
