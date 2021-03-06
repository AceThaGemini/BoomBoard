package creationsofali.boomboard.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import creationsofali.boomboard.R;
import creationsofali.boomboard.appfonts.MyCustomAppFont;
import creationsofali.boomboard.datamodels.RequestCode;
import creationsofali.boomboard.datamodels.Student;
import creationsofali.boomboard.helpers.DrawerTypefaceHelper;
import creationsofali.boomboard.helpers.EmailHelper;
import creationsofali.boomboard.helpers.NetworkHelper;
import creationsofali.boomboard.helpers.PackageInfoHelper;
import creationsofali.boomboard.helpers.SharedPreferenceEditor;
import creationsofali.boomboard.helpers.TwitterHelper;
import dmax.dialog.SpotsDialog;

public class SignInActivity extends AppCompatActivity {

    Toolbar toolbar;
    // Button buttonSignIn;
    LinearLayout buttonSignIn;
    SpotsDialog signInWaitDialog;
    SpotsDialog buildSignInDialog;
    DrawerLayout drawerLayout;
    NavigationView navigationDrawer;
    ActionBarDrawerToggle drawerToggle;
    TextView textNavSignIn;

    GoogleApiClient googleApiClient;
    FirebaseAuth firebaseAuth;

    boolean isFirstTime;

    private static final String TAG = SignInActivity.class.getSimpleName();

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        isFirstTime = getIntent().getBooleanExtra("isFirstTime", false);

        firebaseAuth = FirebaseAuth.getInstance();
        // buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        // buttonSignIn.setTypeface(Typeface.createFromAsset(
        //       getResources().getAssets(), "fonts/Hind-Regular.ttf"));

        // show progress dialog while signing in
        signInWaitDialog = new SpotsDialog(SignInActivity.this, R.style.SignInDialogStyle);
        signInWaitDialog.setCancelable(false);

        buildSignInDialog = new SpotsDialog(SignInActivity.this, R.style.BuildSignInDialogStyle);
        buildSignInDialog.setCancelable(false);


        drawerLayout = findViewById(R.id.drawerLayout);
        navigationDrawer = findViewById(R.id.navigationDrawer);
        View headerView = navigationDrawer.getHeaderView(0);
        textNavSignIn = headerView.findViewById(R.id.textNavSignIn);

        textNavSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(GravityCompat.START);
                //showSignInDialog();
                // initiate sign in
                initiateSignInIntent();
            }
        });

        drawerToggle = new ActionBarDrawerToggle(
                SignInActivity.this,
                drawerLayout,
                R.string.drawer_opened,
                R.string.drawer_closed);
        drawerLayout.setDrawerListener(drawerToggle);

        navigationDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navAbout)
                    showAboutDialog();

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // showSignInDialog();
                // initiate sign in
                initiateSignInIntent();
            }
        });


        // drawer items typeface
        new DrawerTypefaceHelper(SignInActivity.this).executeTask(navigationDrawer.getMenu());

        // set my custom app font
        View rootView = findViewById(android.R.id.content);
        new MyCustomAppFont(getApplicationContext(), rootView);
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close drawer if open onBackPressed
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);

        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // double back to exit
            getSupportFragmentManager().popBackStack();

//        } else if (!doubleBackToExitPressedOnce) {
//
//            this.doubleBackToExitPressedOnce = true;
//            // now ask for the second backPress to exit
//            Toast.makeText(
//                    getApplicationContext(),
//                    R.string.press_again_exit,
//                    Toast.LENGTH_SHORT).show();
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    // reset values after time delay
//                    doubleBackToExitPressedOnce = false;
//                }
//            }, DELAY_TIME);

        } else {
            // default
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.RC_SIGN_IN) {
            // if (buildSignInDialog.isShowing())
            buildSignInDialog.dismiss();

            // sign in with google sign in api
            GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (signInResult.isSuccess()) {
                GoogleSignInAccount account = signInResult.getSignInAccount();
                signInWaitDialog.show();
                firebaseAuthWithGoogleSignInApi(account);
                Log.d(TAG, "onActivityResult:signInResult::isSuccess");

            } else {
                // sign in failed
                showSnackbar(getString(R.string.sorry_error_try_again));
                Log.d(TAG, "fail status: " + signInResult.getStatus().toString());
            }
        }
    }

//    private Intent getGoogleAuthIntent() {
//        // signInIntent
//        return Auth.GoogleSignInApi.getSignInIntent(getGoogleApiClientInstance());
//    }

    private GoogleApiClient getGoogleApiClientInstance() {
        if (googleApiClient == null) {
            GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    // .requestProfile()
                    //  .requestId()
                    .requestIdToken(getString(R.string.web_client_id))
                    .build();

            googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    //.enableAutoManage()
                    .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                    .build();
            Log.d(TAG, "getGoogleApiClientInstance: create new client instance");
        }

        return googleApiClient;
    }

    private void firebaseAuthWithGoogleSignInApi(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                Log.d(TAG, "firebaseAuthWithGoogleSignInApi:onSuccess");

                if (isFirstTime) {
                    // check if user profile exists in database
                    checkUserProfileInDatabase(authResult.getUser().getUid());

                } else {
                    // there're some profile info in SharedPreferences
                    String signedUid = authResult.getUser().getUid();

                    SharedPreferences sharedPreferences = getSharedPreferences(
                            getString(R.string.app_name), MODE_PRIVATE);

                    String gsonStudent = sharedPreferences.getString("student", null);
                    Student student = new Gson().fromJson(gsonStudent, Student.class);

                    if (student.getUid().equals(signedUid))
                        // same same account
                        // go to main activity
                        gotoMain();
                    else
                        // different account
                        // check user profile in database
                        checkUserProfileInDatabase(authResult.getUser().getUid());

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "firebaseAuthWithGoogleSignInApi:onFailure::" + e.getMessage());
                // dismiss progress dialog
                hideSignInDialog();

                // check if the error is caused by network problems
                if (!NetworkHelper.isOnline(SignInActivity.this)) {
                    showSnackbar("Device is offline, check internet settings and try again.");
                } else
                    showSnackbar(getString(R.string.sorry_error_try_again));
            }
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(
                findViewById(R.id.coordinatorSignIn),
                message,
                Snackbar.LENGTH_INDEFINITE)
                .setDuration(8000).show();
    }

    public void showAboutDialog() {
        final View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_about, null);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignInActivity.this);
        dialogBuilder.setView(dialogView)
                .setCancelable(true);

        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        TextView textVersion = dialogView.findViewById(R.id.textVersion);
        textVersion.setText(PackageInfoHelper.getVersionName(SignInActivity.this));

        TextView textRights = dialogView.findViewById(R.id.textRights);
        String unicodeCopyRight = "\u00A9"; // or (char) 169 = 10101001 = 0x00A9
        String unicodeTradeMark = "\u2122"; // or (char) 8482 = 0x2122
        // (c) 2017 TINTech tm
        //      All Rights Reserved.
        textRights.setText(unicodeCopyRight);
        textRights.append(" 2017 " + "TINTech" + unicodeTradeMark);
        textRights.append("\n" + "All Rights Reserved.");


        dialogView.findViewById(R.id.iconTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TwitterHelper.launchTwitter("AceThaGemini", getApplicationContext());
            }
        });

        dialogView.findViewById(R.id.iconEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EmailHelper.launchComposeEmail("allysuley@gmail.com", getApplicationContext());
            }
        });

        dialogView.findViewById(R.id.textClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  onPositiveButtonClick
                dialog.dismiss();
            }
        });
    }

    private void initiateSignInIntent() {
        // show progress
        buildSignInDialog.show();
        startActivityForResult(
                Auth.GoogleSignInApi.getSignInIntent(getGoogleApiClientInstance()),
                RequestCode.RC_SIGN_IN);

    }

    private void checkUserProfileInDatabase(String uid) {
        showSignInDialog("Checking Profile");
        // database reference
        DatabaseReference studentsReference = FirebaseDatabase.getInstance()
                .getReference().child("students");

        // profile reference
        studentsReference.child(uid).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "checkUserProfileInDatabase#onDataChange: " + dataSnapshot.toString());

                if (dataSnapshot.getValue() != null) {
                    // student's profile exists in the database ref
                    Log.d(TAG, "checkUserProfileInDatabase#onDataChange: profile found");

                    Student student = dataSnapshot.getValue(Student.class);
                    // update sharedPrefs
                    new SharedPreferenceEditor(SignInActivity.this).execute(student);
                    // go to main
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // goto main
                            gotoMain();
                        }
                    }, 100);

                } else {
                    // snapshotValue = null
                    // no profile reference
                    gotoProfileSetup();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "checkUserProfileInDatabase#onCancelled: err " + databaseError.getCode() +
                        " " + databaseError.getMessage());

                hideSignInDialog();

                if (databaseError.getCode() == DatabaseError.UNAVAILABLE)
                    // profile doesn't exist in the ref
                    // goto profile set up
                    gotoProfileSetup();
                else
                    showSnackbar(getString(R.string.sorry_error_try_again));

            }
        });

    }

    private void gotoMain() {
        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        hideSignInDialog();
        // kill to clear top
        finish();
    }

    private void gotoProfileSetup() {
        startActivity(new Intent(SignInActivity.this, ProfileSetupActivity.class));
        hideSignInDialog();
        // kill to clear top
        finish();
    }

    private void showSignInDialog(String message) {
        signInWaitDialog.setMessage(message);

        if (!signInWaitDialog.isShowing())
            signInWaitDialog.show();
    }

    private void hideSignInDialog() {
        if (signInWaitDialog.isShowing())
            signInWaitDialog.dismiss();
    }

}
