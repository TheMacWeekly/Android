package hu.ait.macweekly;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import hu.ait.macweekly.data.User;

public class AlumniRegistrationActivity extends BaseActivity {

    static String email;

    private static final String TAG = "AlumniLogin";

    private int rootViewId = R.id.root_alumni_registration;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;

    @BindView(R.id.etEmail)
    EditText etEmail;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.etConfirmPassword)
    EditText etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumni_registration);
        setUpBaseActMembers(findViewById(rootViewId));
        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
    }

    @OnClick(R.id.btnCreate)
    public void createAccount() {
        if (!isFormValid()) {
            return;
        }

        showProgressDialog();
        mFirebaseAuth.createUserWithEmailAndPassword(
                etEmail.getText().toString(), etPassword.getText().toString()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideProgressDialog();
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                    mUsersDatabaseReference.child(firebaseUser.getUid()).setValue(new User(firebaseUser));

                    Log.d(TAG, "createUserWithEmail:success");
                    Toast.makeText(AlumniRegistrationActivity.this,
                            getString(R.string.registrationok) + ". " + getString(R.string.verification_email_sent),
                            Toast.LENGTH_SHORT).show();
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    sendEmailVerification(user);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                Toast.makeText(AlumniRegistrationActivity.this,
                        getString(R.string.error) + ": " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
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

        if (TextUtils.equals(etPassword.getText().toString(), etConfirmPassword.getText().toString())) {
            etConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            return false;
        }

        if (etPassword.length() < 8) {
            etPassword.setError(getString(R.string.pass_leng));
            return false;
        }

        return true;
    }
}
