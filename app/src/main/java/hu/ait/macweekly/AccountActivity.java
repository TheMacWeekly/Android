package hu.ait.macweekly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AccountActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_setup_alumni_account)
    public void setupAlumniAccount() {
        startActivity(new Intent(this, AlumniRegistrationActivity.class));
    }
}
