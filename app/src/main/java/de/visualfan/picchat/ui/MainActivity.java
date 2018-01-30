package de.visualfan.picchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import de.visualfan.picchat.R;

/**
 * Created by Berenice on 19.01.18.
 */

public class MainActivity extends AppCompatActivity implements MessageListFragment.MyFragmentListenerImpl {
    private static final String TAG = "MainActivity";

    private final ComposerFragment mComposerFragment = new ComposerFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onMessageItemSelected(String downloadImgUrlString, String authorName, String description) {
        startPhotoDetailActivity(downloadImgUrlString, authorName, description);
    }

    @Override
    public void onFabButtonClicked() {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mComposerFragment)
                .addToBackStack(null)
                .commit();
    }


    private void startPhotoDetailActivity(String downloadImgUrlString, String authorName, String description){
        //Log.d(TAG, "INTENT = " + authorName + description);
        Intent intent = new Intent(MainActivity.this, PhotoDetailActivity.class);
        intent.putExtra("photoUrlExtra", downloadImgUrlString);
        intent.putExtra("userNameExtra", authorName);
        intent.putExtra("descriptionExtra", description);
        startActivity(intent);
    }
}
