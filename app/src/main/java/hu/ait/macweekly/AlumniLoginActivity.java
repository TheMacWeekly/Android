package hu.ait.macweekly;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.support.annotation.NonNull;

import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AlumniLoginActivity extends BaseActivity {

    static String email;

    private static final String TAG = "AlumniLogin";

    private int rootViewId = R.id.root_alumni_login;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;

    @BindView(R.id.etEmail)
    EditText etEmail;
    @BindView(R.id.etPassword)
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumni_login);
        setUpBaseActMembers(findViewById(rootViewId));
        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
    }

    @OnClick(R.id.btnLogin)
    public void alumniLogin() {
        if (!isFormValid()) {
            return;
        }

        showProgressDialog();

        mFirebaseAuth.signInWithEmailAndPassword(
                etEmail.getText().toString(),
                etPassword.getText().toString()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideProgressDialog();
                if (task.isSuccessful()) {
                    if (mFirebaseAuth.getCurrentUser().isEmailVerified()) {
                        setResult(RESULT_OK);
                        finish();
                    }
                    else {
                        mFirebaseAuth.signOut();
                        Toast.makeText(AlumniLoginActivity.this,
                                getString(R.string.failed) + ": " + getString(R.string.email_not_verified),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(
                            AlumniLoginActivity.this,
                            getString(R.string.failed) + ": " + task.getException().getLocalizedMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @OnClick(R.id.tv_dont_have_account)
    public void alumniAccountAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("To create an alumni account you'll have to contact The Mac Weekly development team. Press \"Email\" to continue");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Email",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto","themacweeklyapp@gmail.com", null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Alumni Account Request");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                        startActivity(Intent.createChooser(emailIntent, "Send email..."));
                    }
                });

        builder1.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert1 = builder1.create();
        alert1.show();
    }

    private void resetPassword(String emailAddress) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    private boolean isFormValid() {
        if (TextUtils.isEmpty(etEmail.getText().toString())) {
            etEmail.setError(getString(R.string.notempty));
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
            etEmail.setError(getString(R.string.error_must_be_email));
            return false;
        }

        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            etPassword.setError(getString(R.string.notempty));
            return false;
        }

        if (etPassword.length() < 8) {
            etPassword.setError(getString(R.string.pass_leng));
            return false;
        }

        return true;
    }
}
