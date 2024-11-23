package com.example.subeteya.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.subeteya.Fragments.LecturaQrFragment;
import com.example.subeteya.Fragments.LineaBusFragment;
import com.example.subeteya.R;
import com.example.subeteya.Viewmodels.FirebaseViewModel;
import com.example.subeteya.databinding.ActivityUsuarioOperativoBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UsuarioOperativoActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    ActivityUsuarioOperativoBinding binding;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseViewModel firebaseViewModel;
    BottomNavigationView bottomNav;
    AlertDialog progresoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase:
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Binding:
        binding = ActivityUsuarioOperativoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Bottom menú:
        bottomNav = binding.bottomNavigation;
        bottomNav.setOnNavigationItemSelectedListener(this);
        bottomNav.setSelectedItemId(R.id.empresa_linea_bus);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new LineaBusFragment()).commit();
        }

        // Manejo de ViewModels:
        firebaseViewModel = new ViewModelProvider(this).get(FirebaseViewModel.class);
        firebaseViewModel.obtenerUsuarioActual(auth.getCurrentUser().getUid());
        firebaseViewModel.recargarUsuarioActual(auth.getCurrentUser().getUid());
        firebaseViewModel.obtenerListaLineasBuses();
        firebaseViewModel.recargarListaLineasBuses();

        // Gestión de intent (mensajito):
        Intent intent = getIntent();
        if(intent.getStringExtra("mensaje") != null){
            Snackbar.make(binding.getRoot(), intent.getStringExtra("mensaje"), Snackbar.LENGTH_LONG).setAnchorView(binding.bottomNavigation).show();
        }


    }

    // -------------------
    //      MÉTODOS:
    // -------------------

    // -> Bottom menú:

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){

        if (firebaseViewModel == null || firebaseViewModel.getUsuarioActual().getValue() == null) {
            return false;
        }

        Fragment fragmentoSeleccionado = null;

        if(item.getItemId() == R.id.empresa_linea_bus){
            fragmentoSeleccionado = new LineaBusFragment();
        }
        if(item.getItemId() == R.id.usuario_qr){
            fragmentoSeleccionado = new LecturaQrFragment();
        }

        if(item.getItemId() != bottomNav.getSelectedItemId()){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, fragmentoSeleccionado).commit();
        }

        return true;
    }

}