package com.example.subeteya.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.subeteya.Activities.LoginActivity;
import com.example.subeteya.Activities.UsuarioOperativoActivity;
import com.example.subeteya.Adapters.LineaBusAdapter;
import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.R;
import com.example.subeteya.Viewmodels.FirebaseViewModel;
import com.example.subeteya.databinding.ActivityUsuarioOperativoBinding;
import com.example.subeteya.databinding.FragmentLineaBusBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class LineaBusFragment extends Fragment {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    FragmentLineaBusBinding binding;
    FirebaseViewModel firebaseViewModel;
    LineaBusAdapter lineaBusAdapter;
    RecyclerView rvLineasBuses;

    public LineaBusFragment() {

    }

    public static LineaBusFragment newInstance(String param1, String param2) {
        LineaBusFragment fragment = new LineaBusFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Binding:
        binding = FragmentLineaBusBinding.inflate(inflater, container, false);

        // Manejo de ViewModels:
        firebaseViewModel = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        // ------------
        //   Lógica:
        // ------------

        // -> Logout:
        binding.btnLogout.setOnClickListener(l -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.putExtra("mensaje","Sesión finalizada exitosamente!");
            startActivity(intent);
            requireActivity().supportFinishAfterTransition();
        });

        // -> Configuración de RecyclerView y Adaptador:
        rvLineasBuses = binding.rvLineasBuses;
        lineaBusAdapter = new LineaBusAdapter((UsuarioOperativoActivity)requireActivity());
        lineaBusAdapter.setListaLineasBuses(new ArrayList<>());
        rvLineasBuses.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvLineasBuses.setAdapter(lineaBusAdapter);

        // -> Observadores:

        // -> Datos del usuario:
        firebaseViewModel.getUsuarioActual().observe(getViewLifecycleOwner(), usuario -> {
            binding.textUsuarioActual.setText(usuario.getNombreCompleto());
            binding.textSaldoActual.setText("S/. "+String.format("%.1f",usuario.getSaldo()));
        });

        // -> Datos de la lista de líneas de buses:
        firebaseViewModel.getlistaLineasBuses().observe(getViewLifecycleOwner(), listaLineasBuses -> {
            lineaBusAdapter.setListaLineasBuses(listaLineasBuses);
        });

        return binding.getRoot();
    }


}