package com.example.subeteya.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.subeteya.Adapters.LineaBusEmpresaAdapter;
import com.example.subeteya.R;
import com.example.subeteya.Viewmodels.FirebaseViewModel;
import com.example.subeteya.databinding.ActivityEmpresaBinding;
import com.example.subeteya.databinding.ActivityUsuarioOperativoBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EmpresaActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    ActivityEmpresaBinding binding;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseViewModel firebaseViewModel;
    BottomNavigationView bottomNav;
    LineaBusEmpresaAdapter lineaBusEmpresaAdapter;
    RecyclerView rvLineasBusesEmpresa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase:
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Binding:
        binding = ActivityEmpresaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Bottom menú:
        bottomNav = binding.bottomNavigation;
        bottomNav.setOnNavigationItemSelectedListener(this);
        bottomNav.setSelectedItemId(R.id.empresa_linea_bus);

        // Manejo de ViewModels:
        firebaseViewModel = new ViewModelProvider(this).get(FirebaseViewModel.class);
        firebaseViewModel.obtenerUsuarioActual(auth.getCurrentUser().getUid());
        firebaseViewModel.recargarUsuarioActual(auth.getCurrentUser().getUid());
        firebaseViewModel.obtenerListaLineasBusesEmpresa(auth.getCurrentUser().getUid());
        firebaseViewModel.recargarListaLineasBusesEmpresa(auth.getCurrentUser().getUid());

        // Gestión de intent (mensajito):
        Intent intent = getIntent();
        if(intent.getStringExtra("mensaje") != null){
            Snackbar.make(binding.getRoot(), intent.getStringExtra("mensaje"), Snackbar.LENGTH_LONG).setAnchorView(binding.bottomNavigation).show();
        }

        // ------------
        //   Lógica:
        // ------------

        // -> Logout:
        binding.btnLogout.setOnClickListener(l -> {
            FirebaseAuth.getInstance().signOut();
            Intent intentP = new Intent(this, LoginActivity.class);
            intentP.putExtra("mensaje","Sesión finalizada exitosamente!");
            startActivity(intentP);
            supportFinishAfterTransition();
        });

        // -> Configuración de RecyclerView y Adaptador:
        rvLineasBusesEmpresa = binding.rvLineasBuses;
        lineaBusEmpresaAdapter = new LineaBusEmpresaAdapter(this);
        lineaBusEmpresaAdapter.setListaLineasBusesEmpresa(new ArrayList<>());
        rvLineasBusesEmpresa.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvLineasBusesEmpresa.setAdapter(lineaBusEmpresaAdapter);

        // -> Observadores:

        // -> Datos del usuario:
        firebaseViewModel.getUsuarioActual().observe(this, usuario -> {
            binding.textUsuarioActual.setText(usuario.getNombreEmpresa());
            binding.textSaldoActual.setText("S/. "+String.format("%.1f",usuario.getRecaudacion()));
        });

        // -> Datos de la lista de líneas de buses:
        firebaseViewModel.getListaLineasBusesEmpresa().observe(this, listaLineasBusesEmpresa -> {
            if(listaLineasBusesEmpresa != null){
                lineaBusEmpresaAdapter.setListaLineasBusesEmpresa(listaLineasBusesEmpresa);
            }
        });
    }

    // -------------------
    //      MÉTODOS:
    // -------------------

    // -> Bottom menú:

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        return true;
    }
}