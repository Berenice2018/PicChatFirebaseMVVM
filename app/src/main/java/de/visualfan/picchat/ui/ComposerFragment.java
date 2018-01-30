package de.visualfan.picchat.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import de.visualfan.picchat.DataRepository;
import de.visualfan.picchat.R;
import de.visualfan.picchat.databinding.ComposerFragmentBinding;
import de.visualfan.picchat.viewmodel.MessagesListViewModel;

import static android.app.Activity.RESULT_CANCELED;

/**
 * Created by Berenice on 26.01.18.
 */

public class ComposerFragment extends Fragment {

    private static final String TAG = "##ComposerFragment";
    private static final int RC_PHOTO_PICKER = 1;
    public static final String ARG_POSITION = "USER_NAME";

    private ComposerFragmentBinding mBinding;
    private MessagesListViewModel mViewModel;
    private String mUserName;
    private Snackbar mSnackbar;


    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);

        mUserName = DataRepository.getInstance().getUserName();

        // DataBinding
        mBinding = DataBindingUtil.inflate(inflater, R.layout.composer_fragment, container, false);

        // progressbar
        mBinding.setIsLoading(false);

        // get the view model
        mViewModel = ViewModelProviders.of(getActivity()).get(MessagesListViewModel.class);

        // attach listeners
        mViewModel.getPictureUploadIsSuccessful().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isSuccess) {
                if(isSuccess){
                    //  unshow progress bar
                    mBinding.setIsLoading(false);
                    //Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                    mSnackbar.setText("Picture upload successful").show();

                    if(!mViewModel.getPhotoUrl().isEmpty()){
                        Glide.with(mBinding.imgvPhotoPreview.getContext())
                                .load(mViewModel.getPhotoUrl())
                                .thumbnail(0.01f)
                                .into(mBinding.imgvPhotoPreview);
                    }
                } else{
                    mBinding.setIsLoading(false);
                    //Toast.makeText(getContext(), "Could not fetch the picture!", Toast.LENGTH_LONG).show();
                    mSnackbar.setText("Could not fetch the picture!").show();
                }
            }
        });

        mViewModel.getMessageUploadIsSuccessful().observe(this, new Observer<Boolean>(){
            @Override
            public void onChanged(@Nullable Boolean isSuccessful) {
                if(isSuccessful && !mViewModel.getPhotoUrl().isEmpty()){
                    mBinding.setIsLoading(false);
                    mSnackbar.setText("Message Upload successful").show();
                } else {
                    mBinding.setIsLoading(false);
                    //Toast.makeText(getContext(), "Message was NOT uploaded", Toast.LENGTH_LONG).show();
                    mSnackbar.setText("Could not upload the message").show();
                }
            }
        });


        // ImagePickerButton shows an image picker to upload a image for a message
        mBinding.photoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Fire an intent to show an image picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "complete action using"), RC_PHOTO_PICKER);
            }
        });


        // Enable Send button when there's text to send
        mBinding.messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mBinding.sendButton.setEnabled(true);
                } else {
                    mBinding.sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        // Send button sends a message and clears the EditText
        mBinding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String descriptionText = mBinding.messageEditText.getText().toString();

                if(mViewModel.getPhotoUrl() != null && !mViewModel.getPhotoUrl().isEmpty()){
                    mViewModel.createAndSendPhotoMessageToDataBase(
                            mUserName,
                            descriptionText,
                            mViewModel.getPhotoUrl());
                    // Clear input box
                    mBinding.messageEditText.setText("");
                    mViewModel.setPhotoUrl("");
                    dismissKeyboard();
                    // return to other fragment
                    getActivity().onBackPressed();
                } else {
                    mSnackbar.setText("Please, choose a picture.").show();
                }

            }
        });

        mSnackbar = Snackbar.make(mBinding.getRoot(), "Hello", Snackbar.LENGTH_SHORT)
                .setAction("Action", null);
        ViewGroup group = (ViewGroup) mSnackbar.getView();
        group.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));

    return mBinding.getRoot();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "result code = " + String.valueOf(resultCode));

        if(resultCode == RESULT_CANCELED){
            mSnackbar.setText("Action canceled").show();
            return;
        }

        switch(requestCode){
            case RC_PHOTO_PICKER:
                // show the progress bar
                mBinding.setIsLoading(true);
                mViewModel.uploadPicture(data);
                break;
            default: Log.w(TAG, "switch(requestCode), case not implemented.");
        }

    }


    private void dismissKeyboard(){
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && null != getActivity().getCurrentFocus())
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus()
                    .getApplicationWindowToken(), 0);
    }

}
