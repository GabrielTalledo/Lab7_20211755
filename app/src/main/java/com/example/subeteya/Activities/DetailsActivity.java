package com.example.subeteya.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.subeteya.Adapters.FotoCarruselAdapter;
import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.Beans.Usuario;
import com.example.subeteya.R;
import com.example.subeteya.Viewmodels.FirebaseViewModel;
import com.example.subeteya.databinding.ActivityDetailsBinding;
import com.example.subeteya.databinding.ActivityLoginBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    ActivityDetailsBinding binding;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseViewModel firebaseViewModel;
    RecyclerView rvCarrusel;
    FotoCarruselAdapter carruselAdapter;
    LineaBus lineaBus;
    AlertDialog progresoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase:
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Binding:
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Vista inicial:
        binding.btnSuscribirseBus.setText("Cargando");
        binding.btnSuscribirseBus.setEnabled(false);
        binding.btnSuscribirseBus.setAlpha(0.3f);

        // ------------
        //   Lógica:
        // ------------

        // -> Gestión del intent (Datos de la línea de bus):
        Intent intent = getIntent();
        if(intent.getSerializableExtra("lineaBus") != null){
            lineaBus = (LineaBus) intent.getSerializableExtra("lineaBus");
        }


        // -> Configuración de RecyclerView y Adapter:
        rvCarrusel = binding.rvCarruselBus;
        carruselAdapter = new FotoCarruselAdapter(this);
        carruselAdapter.setListaRutasCarrusel(new ArrayList<>());
        rvCarrusel.setAdapter(carruselAdapter);


        // -> Manejo de ViewModels:
        firebaseViewModel = new ViewModelProvider(this).get(FirebaseViewModel.class);
        firebaseViewModel.obtenerUsuarioActual(auth.getCurrentUser().getUid());
        firebaseViewModel.recargarUsuarioActual(auth.getCurrentUser().getUid());
        firebaseViewModel.obtenerLineaBusActual(lineaBus.getUid());
        firebaseViewModel.recargarLineaBusActual(lineaBus.getUid());

        firebaseViewModel.getLineaBusActual().observe(this, lineaBus -> {
            this.lineaBus = lineaBus;
            actualizarUI(firebaseViewModel.getUsuarioActual().getValue(), lineaBus);
        });

        firebaseViewModel.getUsuarioActual().observe(this, usuario -> {
            actualizarUI(firebaseViewModel.getUsuarioActual().getValue(), lineaBus);
        });

        // -> Back button:
        binding.btnVolverUsuario.setOnClickListener(l -> {
            onBackPressed();
        });

        // -> Logout:
        binding.btnLogout.setOnClickListener(l -> {
            FirebaseAuth.getInstance().signOut();
            Intent intentTwo = new Intent(this, LoginActivity.class);
            intentTwo.putExtra("mensaje","Sesión finalizada exitosamente!");
            startActivity(intentTwo);
            supportFinishAfterTransition();
        });

        // -> Manejo de la suscripción:
        binding.btnSuscribirseBus.setOnClickListener(l -> {
            mostrarDialogSuscripcion();
        });

    }

    // -------------------
    //      MÉTODOS:
    // -------------------

    // -> Diálogos:
    public void mostrarDialogSuscripcion(){
        new MaterialAlertDialogBuilder(this)
                .setTitle("Intención de suscripción!")
                .setMessage("Desea proceder con el proceso de suscripción a la línea de buses '"+lineaBus.getNombre()+"' por el precio de S/. "+lineaBus.getPrecioSuscripcion()+"?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    suscribirUsuarioBus();
                })
                .setNegativeButton("No",(dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void mostrarDialogoProgreso() {
        View view = getLayoutInflater().inflate(R.layout.progreso, null);
        progresoDialog = new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setView(view)
                .setCancelable(false)
                .create();
        progresoDialog.show();
    }

    private void cerrarDialogoProgreso() {
        if (progresoDialog != null && progresoDialog.isShowing()) {
            progresoDialog.dismiss();
        }
    }

    // -> Usuario:
    public void suscribirUsuarioBus(){
        Usuario usuario = firebaseViewModel.getUsuarioActual().getValue();
        if(usuario.getSaldo()<lineaBus.getPrecioSuscripcion()){
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Error al intentar suscribirse!")
                    .setMessage("Usted no cuenta con el saldo suficiente para comprar la suscripción a esta línea de buses. Actualmente cuenta solo con S/. "+usuario.getSaldo())
                    .setPositiveButton("Entendido", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }else{
            mostrarDialogoProgreso();
            // Actualización en el cliente:
            List<DocumentReference> listRefLineaBusSuscripcion = new ArrayList<>();
            for(LineaBus lb: usuario.getLineaBusSuscripcion()){
                listRefLineaBusSuscripcion.add(db.collection("lineaBus").document(lb.getUid()));
            }
            listRefLineaBusSuscripcion.add(db.collection("lineaBus").document(lineaBus.getUid()));
            HashMap<String, Object> campos = new HashMap();
            campos.put("refLineaBusSuscripcion",listRefLineaBusSuscripcion);
            campos.put("saldo",Double.parseDouble(String.format("%.1f",usuario.getSaldo()-lineaBus.getPrecioSuscripcion())));
            db.collection("usuario").document(usuario.getUid()).update(campos)
                    .addOnCompleteListener(document -> {
                        if(document.isSuccessful()){
                            // Actualización en la línea de bus:
                            db.collection("lineaBus").document(lineaBus.getUid()).update("recaudacion",Double.parseDouble(String.format("%.1f",lineaBus.getRecaudacion()+lineaBus.getPrecioSuscripcion())))
                                    .addOnCompleteListener(documente -> {
                                        if(documente.isSuccessful()){
                                            // Actualización de la empresa:
                                            db.collection("usuario").document(lineaBus.getEmpresa().getUid()).update("recaudacion",Double.parseDouble(String.format("%.1f",lineaBus.getEmpresa().getRecaudacion()+lineaBus.getPrecioSuscripcion())))
                                                    .addOnCompleteListener(documenti -> {
                                                        if(documenti.isSuccessful()){
                                                            cerrarDialogoProgreso();
                                                            Snackbar.make(this, binding.getRoot(), "Suscripción realizada con éxito! Saldo actual S/. "+(usuario.getSaldo()-lineaBus.getPrecioSuscripcion()), Snackbar.LENGTH_LONG).show();
                                                        }else{
                                                            Snackbar.make(this, binding.getRoot(), "Error al intentar suscribirse! (Empresa)", Snackbar.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }else{
                                            Snackbar.make(this, binding.getRoot(), "Error al intentar suscribirse! (Línea de Bus)", Snackbar.LENGTH_LONG).show();

                                        }
                                    });
                        }else{
                            Snackbar.make(this, binding.getRoot(), "Error al intentar suscribirse! (Usuario)", Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
    }

    // -> Vistas:
    public void actualizarUI(Usuario usuario, LineaBus lineaBus){
        if(usuario == null){
            binding.btnSuscribirseBus.setText("Cargando");
            binding.btnSuscribirseBus.setEnabled(false);
            binding.btnSuscribirseBus.setAlpha(0.3f);
        }else{
            binding.textNombreLineaBus.setText(lineaBus.getNombre());
            binding.textPrecioUnitarioBus.setText("S/. "+lineaBus.getPrecioUnitario().toString());
            binding.textPrecioSuscripcionBus.setText("S/. "+lineaBus.getPrecioSuscripcion().toString());

            binding.btnSuscribirseBus.setText("Suscribirse");
            binding.btnSuscribirseBus.setEnabled(true);
            binding.btnSuscribirseBus.setAlpha(1.0f);
            for(LineaBus lb: usuario.getLineaBusSuscripcion()){
                if(lb.getUid().equals(lineaBus.getUid())){
                    binding.btnSuscribirseBus.setText("Suscrito");
                    binding.btnSuscribirseBus.setEnabled(false);
                    binding.btnSuscribirseBus.setAlpha(0.5f);
                }
            }
            carruselAdapter.setListaRutasCarrusel(lineaBus.getRutasCarrusel());
        }
    }

}