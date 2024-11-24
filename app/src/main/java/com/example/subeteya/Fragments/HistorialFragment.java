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
import com.example.subeteya.Adapters.HistorialAdapter;
import com.example.subeteya.Adapters.LineaBusAdapter;
import com.example.subeteya.Beans.Auxiliar;
import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.Beans.Viaje;
import com.example.subeteya.DTOs.LineaBusDTO;
import com.example.subeteya.R;
import com.example.subeteya.Viewmodels.FirebaseViewModel;
import com.example.subeteya.databinding.FragmentHistorialBinding;
import com.example.subeteya.databinding.FragmentLineaBusBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class HistorialFragment extends Fragment {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    FragmentHistorialBinding binding;
    FirebaseViewModel firebaseViewModel;
    HistorialAdapter historialAdapter;
    RecyclerView rvHistorial;

    List<LineaBus> listaBusesAuxiliar;

    public HistorialFragment() {
        // Required empty public constructor
    }

    public static HistorialFragment newInstance(String param1, String param2) {
        HistorialFragment fragment = new HistorialFragment();
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
        binding = FragmentHistorialBinding.inflate(inflater, container, false);

        // Firebase Auth:
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Manejo de ViewModels:
        firebaseViewModel = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        // Inicio:
        binding.textAlertaHistorial.setVisibility(View.VISIBLE);
        binding.textAlertaHistorial.setText("No hay registros");

        // ------------
        //   Lógica:
        // ------------

        // -> Logout:
        binding.btnLogout.setOnClickListener(l -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.putExtra("mensaje", "Sesión finalizada exitosamente!");
            startActivity(intent);
            requireActivity().supportFinishAfterTransition();
        });

        // -> Configuración de RecyclerView y Adaptador:
        rvHistorial = binding.rvHistorialViajes;
        historialAdapter = new HistorialAdapter((UsuarioOperativoActivity) requireActivity());
        historialAdapter.setAuxiliar(new Auxiliar(new ArrayList<>(), new ArrayList<>()));
        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvHistorial.setAdapter(historialAdapter);

        // -> Observadores:

        // -> Datos del usuario:
        firebaseViewModel.getUsuarioActual().observe(getViewLifecycleOwner(), usuario -> {
            binding.textUsuarioActual.setText(usuario.getNombreCompleto());
            binding.textSaldoActual.setText("S/. " + String.format("%.1f", usuario.getSaldo()));
        });

        // -> Datos de la lista de viajes:
        firebaseViewModel.obtenerListaViaje(auth.getCurrentUser().getUid());
        firebaseViewModel.getListaViajes().observe(getViewLifecycleOwner(), listaViajes -> {
            if (listaViajes == null || listaViajes.isEmpty()) {
                binding.textAlertaHistorial.setVisibility(View.VISIBLE);
                binding.textAlertaHistorial.setText("Aún no ha realizado viajes con la aplicación");
            } else {
                binding.textAlertaHistorial.setVisibility(View.GONE);

                // Creación del auxiliar:
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Auxiliar auxiliar = new Auxiliar();
                auxiliar.setListaViajes(listaViajes);

                listaBusesAuxiliar = new ArrayList<>();

                // Lista de tareas que vamos a esperar
                List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                // Cargando los datos de los buses de los viajes
                for (Viaje viaje : listaViajes) {
                    Task<DocumentSnapshot> lineaBusTask = db.collection("lineaBus").document(viaje.getLineaBusUid()).get();
                    tasks.add(lineaBusTask);
                }

                // Espera a que todas las tareas de Firebase se completen
                Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
                    for (int i = 0; i < tasks.size(); i++) {
                        Task<DocumentSnapshot> lineaBusTask = tasks.get(i);
                        if (lineaBusTask.isSuccessful() && lineaBusTask.getResult() != null && lineaBusTask.getResult().exists()) {
                            LineaBusDTO lineaBusDTO = lineaBusTask.getResult().toObject(LineaBusDTO.class);
                            LineaBus lineaBusAuxiliar = new LineaBus();
                            lineaBusAuxiliar.setNombre(lineaBusDTO.getNombre());
                            lineaBusAuxiliar.setRutasCarrusel(lineaBusDTO.getRutasCarrusel());

                            // Añadir al auxiliar en el orden correcto
                            listaBusesAuxiliar.add(lineaBusAuxiliar);
                        }
                    }

                    // Ahora que tenemos todos los datos, asignamos el auxiliar y actualizamos el adaptador
                    auxiliar.setListaLineasBuses(listaBusesAuxiliar);
                    historialAdapter.setAuxiliar(auxiliar);
                });
            }

        });

        return binding.getRoot();
    }
}

