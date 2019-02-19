package com.teamalpha.aichef;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements CameraPreview.PreviewListener{
    private SearchView searchView = null;
    private CameraPreview mPreview;
    private boolean isPaused = false; // whether the preview is paused
    private FrameLayout mCamFrame = null;
    private static final int CAMERA_REQUEST_ID = 1;

    private Button shoppingListButton;
    private Button recipesListButton;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamFrame = findViewById(R.id.camFrame);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Check format of query string and send it to
                // image classifier to check against vegetable classes
                if (!query.matches("[a-zA-Z\\s]+")) {
                    // Wrong format of input query
                    Snackbar wrongFormat = Snackbar.make(
                            mCamFrame,
                            "An ingredient should only contain letters and whitespaces.",
                            Snackbar.LENGTH_LONG);
                    wrongFormat.getView().setBackgroundColor(Color.RED);
                    wrongFormat.show();
                    clearSearchView();
                }
                else {
                    // Make input all lowercase and replace whitespace with underline
                    query = query.toLowerCase().replaceAll("\\s+", "_");
                    /* TODO: Send query to back end and potentially reformat ingredient string before adding to the list. */
                    String replyFromApi = "some nicely formatted ingredient";
                    if (!replyFromApi.equals("NOT FOUND"))
                        popIngredientDialog(replyFromApi);
                    else
                        Toast.makeText(MainActivity.this, "Ingredient not found", Toast.LENGTH_SHORT).show();
                    clearSearchView();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Don't want to do anything on text change
                return false;
            }
        });

        shoppingListButton = findViewById(R.id.shoppingListButton);
        shoppingListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseOrResumeCamera();
                /* TODO: Switch to shopping list page. */
            }
        });

        recipesListButton = findViewById(R.id.recipesListButton);
        recipesListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* TODO: Switch to recipes list page. */
            }
        });

        askCameraPermission();

        // Permission has been granted
        initialiseState();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted
                    initialiseState();
                } else {
                    // Permission has been denied; since camera permission is critical, show user an
                    // explanation of why it needed it
                    Snackbar explanation = Snackbar.make(
                            mCamFrame,
                            "This app needs camera permission in order to scan your ingredients.",
                            Snackbar.LENGTH_LONG);
                    explanation.show();
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initialiseState() {
        searchView.setQueryHint("Search for ingredient");

        // Create preview and set it as the content of the frame
        mPreview = new CameraPreview(this);
        mCamFrame.addView(mPreview);

        searchView.bringToFront();
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // Have been denied permission before, so show user an explanation as to why we
                // need camera permission
                Snackbar explanation = Snackbar.make(
                        mCamFrame,
                        "This app needs camera permission in order to scan your ingredients.",
                        Snackbar.LENGTH_LONG);
                explanation.show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_ID);
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_ID);
            }
        }
    }

    @Override
    public void onPreviewUpdated(byte[] data, int width, int height) {
        if (data != null) {
            /* TODO: Send image data to back end. */
            String replyFromAPI = "NOT FOUND";

            if (!replyFromAPI.equals("NOT FOUND")) {
                if (!inList(replyFromAPI))
                    popIngredientDialog(replyFromAPI);
            }

            if (isPaused)
                Log.e("PREVIEW", "Paused, but still in preview!");
        }
    }

    public void pauseOrResumeCamera() {
        isPaused = !isPaused;
        mPreview.pauseOrResume(isPaused);
        if (!isPaused) {
            mPreview.resetBuffer();
        }
    }

    private void popIngredientDialog(final String ingredient) {
        AddIngredientDialogFragment dialog = AddIngredientDialogFragment.newInstance(ingredient);
        dialog.setOnYesNoClick(new AddIngredientDialogFragment.OnYesNoClick() {
            @Override
            public void onYesClicked() {
                /* TODO: Add ingredient to list. */

                Toast.makeText(MainActivity.this, ingredient + " added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNoClicked() {
                // Don't want to do anything on no click
            }
        });
        dialog.show(getFragmentManager(), "New ingredient");
    }

    private void clearSearchView() {
        if(searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        } else
            Log.e("SEARCH BAR", "Trying to clear a null search view!");
    }

    private boolean inList(String ingredient) {
        /* TODO: check whether ingredient is already in the ingredients list. */
        return false;
    }
}
