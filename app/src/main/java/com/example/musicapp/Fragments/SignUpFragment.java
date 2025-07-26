package com.example.musicapp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicapp.Activities.MainActivity;
import com.example.musicapp.Activities.MusicPlayer;
import com.example.musicapp.MusicManager.MusicManagerActivity;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class SignUpFragment extends Fragment {
    private TextView alreadyHaveAnAccount;
    private FrameLayout frameLayout;

    private EditText userName;
    private EditText email;
    private EditText password;
    private EditText confirmPass;
    private Button signUpButton;
    private ProgressBar signUpProgress;

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        alreadyHaveAnAccount = view.findViewById(R.id.already_have_account);
        frameLayout = getActivity().findViewById(R.id.register_frame_layout);

        userName = view.findViewById(R.id.userName);
        email = view.findViewById(R.id.emailID);
        password = view.findViewById(R.id.password);
        confirmPass = view.findViewById(R.id.confirmPass);
        signUpButton = view.findViewById(R.id.signUpButton);
        signUpProgress = view.findViewById(R.id.signUpProgress);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new SignInFragment());
            }
        });

        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        confirmPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpButton.setEnabled(false);
                signUpButton.setTextColor(getResources().getColor(R.color.white));
                signUpWithFirebase();
            }
        });
    }

    private void signUpWithFirebase() {
        if(email.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            if(password.getText().toString().equals(confirmPass.getText().toString())){
                signUpProgress.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                signUpProgress.setVisibility(View.INVISIBLE);
                                signUpButton.setEnabled(true);
                                signUpButton.setTextColor(getResources().getColor(R.color.white));
                                if (task.isSuccessful()){
                                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.replace(R.id.register_frame_layout, new SignInFragment()); // R.id.fragment_container là id của container chứa fragment
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
                                } else {
                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                confirmPass.setError("Password doesn't match.");
                signUpButton.setEnabled(true);
                signUpButton.setTextColor(getResources().getColor(R.color.white));
            }
        } else {
            email.setError("Invalid Email Pattern:");
            signUpButton.setEnabled(true);
            signUpButton.setTextColor(getResources().getColor(R.color.white));
        }
    }


    private void setFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.from_left, R.anim.out_from_right);
        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }

    private void checkInputs()
    {
        if(!userName.getText().toString().isEmpty()){
            if(!email.getText().toString().isEmpty()){
                if(!password.getText().toString().isEmpty() && password.getText().toString().length()>=6){
                    if(!confirmPass.getText().toString().isEmpty()){
                        signUpButton.setEnabled(true);
                        signUpButton.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        signUpButton.setEnabled(false);
                        signUpButton.setTextColor(getResources().getColor(R.color.white));
                    }

                    } else{
                    signUpButton.setEnabled(false);
                    signUpButton.setTextColor(getResources().getColor(R.color.white));
                }

            } else {
                signUpButton.setEnabled(false);
                signUpButton.setTextColor(getResources().getColor(R.color.white));
            }

        } else {
            signUpButton.setEnabled(false);
            signUpButton.setTextColor(getResources().getColor(R.color.white));
        }
    }
}