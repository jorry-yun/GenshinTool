package com.example.paimon;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.paimon.databinding.ActivityTipBinding;

public class TipActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityTipBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}