package de.visualfan.picchat.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import de.visualfan.picchat.DataRepository;
import de.visualfan.picchat.R;
import de.visualfan.picchat.data.PhotoMessageEntity;
import de.visualfan.picchat.databinding.MessageItemBinding;
import de.visualfan.picchat.databinding.MessageListFragmentBinding;
import de.visualfan.picchat.model.Message;
import de.visualfan.picchat.viewmodel.MessagesListViewModel;

import static android.app.Activity.RESULT_CANCELED;


/**
 * Created by Berenice on 23.01.18.
 */

public class MessageListFragment extends Fragment implements MessageAdapter.MessageAdapterOnClickHandler {

    private static final String TAG = "MessageListFragment";
    private static final String ANONYMOUS = "anonymous";
    private static final int RC_SIGN_IN = 1;

    private MessagesListViewModel mModel ;
    private MessageListFragmentBinding mBinding;
    private MyFragmentListenerImpl mFragmentCallback;
    private MessageAdapter mMessageAdapter = new MessageAdapter(this);
    private Snackbar mSnackbar;

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Container Activity must implement this interface
    interface MyFragmentListenerImpl {
        void onMessageItemSelected(String downloadImgUrlString, String authorName, String description);
        void onFabButtonClicked();
    }



    //  MessageAdapter.MessageAdapterOnClickHandler onClick()
    @Override
    public void onClick(View view, int position, Message message) {
        Log.d(TAG, "message CLICK. "+ message.getUserName() + message.getDescription() + message.getPhotoUrl());
        Log.d(TAG, "lifecycle stat = " + getLifecycle().getCurrentState());

        // check the state. If the other fragment is visible, do not allow item clicks
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
            // delegate to container Activity
            mFragmentCallback.onMessageItemSelected(
                    message.getPhotoUrl(),
                    message.getUserName(),
                    message.getDescription()
            );
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mModel = ViewModelProviders.of(getActivity()).get(MessagesListViewModel.class);
        FirebaseApp.initializeApp(getActivity());
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //mModel.setUserName(user.getDisplayName());
                    DataRepository.getInstance().setUserName(user.getDisplayName());
                } else {
                    Log.d(TAG, "user is NULL");
                    DataRepository.getInstance().setUserName(ANONYMOUS);;
                    // Choose authentication providers
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build() );
                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .setTheme(R.style.LoginTheme)
                                    .setLogo(R.mipmap.ic_launcher)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        Log.d(TAG, "on Create View");

        mBinding = DataBindingUtil.inflate(inflater, R.layout.message_list_fragment,
               container, false );

        mBinding.setIsLoading(true);

        //mBinding.tvUsername.setText(mModel.getUserName());

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        mBinding.recyclerview.setLayoutManager(layoutManager);


        // the FAB
        mBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show fragment on click
                mFragmentCallback.onFabButtonClicked();
            }
        });

        // the toolbar
        mBinding.myToolbar.inflateMenu(R.menu.main_menu);
        mBinding.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.sign_out_menu:
                        AuthUI.getInstance().signOut(getContext());
                        return true;
                    default:
                        Log.d(TAG, "clicked item does not exist");
                        return false;
                }
            }
        });
        mBinding.myToolbar.setNavigationIcon(R.mipmap.ic_launcher);

        mSnackbar = Snackbar.make(mBinding.recyclerview, "Hello", Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        ViewGroup group = (ViewGroup) mSnackbar.getView();
        group.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));

        return mBinding.getRoot();
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "on attach");

        // Make sure that the container activity has implemented the callback interface.
        try {
            mFragmentCallback = (MyFragmentListenerImpl) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState ){
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "– onActivityCreated: setting adapter.");

        mBinding.recyclerview.setAdapter(mMessageAdapter);

        // subscribe UI
        // Update the list when the data changes
        if(mModel != null){
            LiveData<List<PhotoMessageEntity>> liveData = mModel.getMessageListLiveData();
            liveData.observe(getActivity(), (List<PhotoMessageEntity> photoMessageEntities) -> {
                mMessageAdapter.setMessageList(photoMessageEntities);
            });
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "result code = " + String.valueOf(resultCode));

        if(resultCode == RESULT_CANCELED){
           // TODO mSnackbar.setText("Action cancelled").show();
            return;
        }

        switch(requestCode){
            case RC_SIGN_IN:
                //Toast.makeText(getContext(), "Signed in", Toast.LENGTH_LONG).show();
                mSnackbar.setText("Signed in").show();

                break;

            default: Log.w(TAG, "switch(requestCode), case not implemented.");
        }

    }


    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "on resume");
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }



    @Override
    public void onPause(){
        Log.d(TAG, "## onPause");
        super.onPause();
        if(mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }

    }



}
