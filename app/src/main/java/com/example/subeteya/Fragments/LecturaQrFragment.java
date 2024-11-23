package com.example.subeteya.Fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.subeteya.Activities.LoginActivity;
import com.example.subeteya.Adapters.LineaBusAdapter;
import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.Beans.Usuario;
import com.example.subeteya.DTOs.LineaBusDTO;
import com.example.subeteya.DTOs.UsuarioDTO;
import com.example.subeteya.Modules.GlideApp;
import com.example.subeteya.R;
import com.example.subeteya.Viewmodels.FirebaseViewModel;
import com.example.subeteya.databinding.FragmentLecturaQrBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Date;
import java.util.HashMap;


public class LecturaQrFragment extends Fragment {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    FragmentLecturaQrBinding binding;
    FirebaseViewModel firebaseViewModel;
    LineaBusAdapter lineaBusAdapter;
    FirebaseFirestore db;
    FirebaseAuth auth;
    AlertDialog progresoDialog;
    Boolean tieneSuscripcion;
    long umbralTiempo = 1L;

    public LecturaQrFragment() {
    }

    public static LecturaQrFragment newInstance(String param1, String param2) {
        LecturaQrFragment fragment = new LecturaQrFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Firebase:
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Binding:
        binding = FragmentLecturaQrBinding.inflate(inflater, container, false);

        // Manejo de ViewModels:
        firebaseViewModel = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        // Vista inicial:
        binding.layoutImagenViajeUsuario.setVisibility(View.GONE);
        binding.layoutInfoViajeUsuario.setVisibility(View.GONE);
        binding.textEstadoActual.setText("En ningún viaje");

        // Validación inicial de suscripción:
        tieneSuscripcion = false;
        if(firebaseViewModel.getUsuarioActual().getValue() != null && firebaseViewModel.getUsuarioActual().getValue().getLineaBus() != null) {
            for(LineaBus lb: firebaseViewModel.getUsuarioActual().getValue().getLineaBusSuscripcion()){
                if(lb.getUid().equals(firebaseViewModel.getUsuarioActual().getValue().getLineaBus().getUid())){
                    tieneSuscripcion = true;
                }
            }
        }

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

        // |-> Observadores:

        // -> Datos del usuario:


        actualizarVistaFlujoViaje(firebaseViewModel.getUsuarioActual().getValue());

        firebaseViewModel.getUsuarioActual().observe(getViewLifecycleOwner(), usuario -> {
            binding.textUsuarioActual.setText(usuario.getNombreCompleto());
            binding.textSaldoActual.setText("S/. "+String.format("%.1f",usuario.getSaldo()));
            if(usuario.getLineaBus() == null){
                // Forzamos a que el usuario tenga todos sus atributos mapeados:
                db.collection("usuario").document(usuario.getUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                UsuarioDTO usuarioDTO = documentSnapshot.toObject(UsuarioDTO.class);
                                firebaseViewModel.mapearUsuarioDtoABean(usuarioDTO, documentSnapshot.getId()).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Usuario usuarioAux = task.getResult();
                                        actualizarVistaFlujoViaje(usuarioAux);
                                        Log.d("Firestore", "Usuario mapeado correctamente: " + usuario);
                                    } else {
                                        Log.d("Firestore", "Error al mapear usuario", task.getException());
                                    }
                                });
                            }
                        });
            }else{
                actualizarVistaFlujoViaje(usuario);
            }
        });

        // -> Leer QR:
        binding.btnLeerQr.setOnClickListener(l -> {
            iniciarEscaneoQr();
        });

        return binding.getRoot();
    }

    // -------------------
    //      MÉTODOS:
    // -------------------

    // -> Fotito de Storage:
    public void cargarImagenDesdeStorage(LineaBus lineaBus) {
        // Se carga solo la primera foto del carrusel:
        if(lineaBus.getRutasCarrusel() == null || lineaBus.getRutasCarrusel().isEmpty()){
            Glide.with(getContext())
                    .load(R.drawable.placeholder)
                    .into(binding.circularImageView);
        }else{
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(lineaBus.getRutasCarrusel().get(0));
            GlideApp.with(getContext())
                    .load(storageRef)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(binding.circularImageView);
        }
    }

    // -> Métodos para el flujo de viaje:
    public void actualizarVistaFlujoViaje(Usuario usuario){
        binding.textUsuarioActual.setText(usuario.getNombreCompleto());
        binding.textSaldoActual.setText("S/. "+String.format("%.1f",usuario.getSaldo()));
        if(usuario.getLineaBus() != null){
            // Primera etapa: En ningún viaje (Fecha inicio nula o Fecha de fin no nula)
            if(usuario.getFechaInicio() == null){
                binding.layoutImagenViajeUsuario.setVisibility(View.GONE);
                binding.layoutInfoViajeUsuario.setVisibility(View.GONE);
                binding.textEstadoActual.setText("En ningún viaje");
            }else{
                // Segunda etapa: En viaje (Lectura QR, pago completo del costo original)
                if(usuario.getFechaFin() == null){
                    Log.d("TAG", "usuario BUS: "+usuario.getLineaBus().toString());
                    Log.d("TAG", "usuario BUS nombre: "+usuario.getLineaBus().getNombre());
                    binding.layoutImagenViajeUsuario.setVisibility(View.VISIBLE);
                    binding.layoutInfoViajeUsuario.setVisibility(View.VISIBLE);
                    binding.textEstadoActual.setText("En viaje");
                    binding.textLineaBusViaje.setText(usuario.getLineaBus().getNombre());
                    binding.textFechaInicioViaje.setText(usuario.getFechaInicioBonita());
                    binding.textFechaFinViaje.setText(usuario.getFechaFinBonita());
                    binding.textCostoOriginalViaje.setText("S/. "+usuario.getLineaBus().getPrecioUnitario());
                    cargarImagenDesdeStorage(usuario.getLineaBus());
                    if(tieneSuscripcion){
                        binding.textCostoFinalViaje.setText("Ninguno, usted posee una suscripción");
                    }else{
                        binding.textCostoFinalViaje.setText("-");
                    }
                }else{
                    // Tercera etapa: Fin de viaje (Lectura QR, cashback del pago)
                    if(usuario.getFechaInicio() != null && usuario.getFechaFin() != null){
                        Log.d("TAG", "usuario: "+usuario.getLineaBus().toString());
                        Log.d("TAG", "usuario BUS nombre: "+usuario.getLineaBus().getNombre());
                        binding.layoutImagenViajeUsuario.setVisibility(View.VISIBLE);
                        binding.layoutInfoViajeUsuario.setVisibility(View.VISIBLE);
                        binding.textEstadoActual.setText("Viaje finalizado");
                        binding.textLineaBusViaje.setText(usuario.getLineaBus().getNombre());
                        binding.textFechaInicioViaje.setText(usuario.getFechaInicioBonita());
                        binding.textFechaFinViaje.setText(usuario.getFechaFinBonita());
                        cargarImagenDesdeStorage(usuario.getLineaBus());
                        binding.textCostoOriginalViaje.setText("S/. "+usuario.getLineaBus().getPrecioUnitario());

                        if(tieneSuscripcion){
                            binding.textCostoFinalViaje.setText("Ninguno, usted posee una suscripción");
                        }else{
                            long diferenciaMinutos = obtenerDiferenciaEnMinutos(usuario.getFechaFin(),usuario.getFechaInicio());
                            Double costoFinal;
                            if(diferenciaMinutos<umbralTiempo){
                                costoFinal = Double.parseDouble(String.format("%.1f",usuario.getLineaBus().getPrecioUnitario()-usuario.getLineaBus().getPrecioUnitario()*0.2));
                            }else{
                                costoFinal = Double.parseDouble(String.format("%.1f",usuario.getLineaBus().getPrecioUnitario()-usuario.getLineaBus().getPrecioUnitario()*0.05));
                            }
                            binding.textCostoFinalViaje.setText("S/. "+costoFinal);
                        }
                    }
                }
            }
        }else{
            // Forzamos a que el usuario tenga todos sus atributos mapeados:
            db.collection("usuario").document(usuario.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UsuarioDTO usuarioDTO = documentSnapshot.toObject(UsuarioDTO.class);
                            firebaseViewModel.mapearUsuarioDtoABean(usuarioDTO, documentSnapshot.getId()).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Usuario usuarioAux = task.getResult();
                                    actualizarVistaFlujoViaje(usuarioAux);
                                    Log.d("Firestore", "Usuario mapeado correctamente: " + usuario);
                                } else {
                                    Log.d("Firestore", "Error al mapear usuario", task.getException());
                                }
                            });
                        }
                    });
        }
    }

    // -> Métodos para el QR:
    private void iniciarEscaneoQr() {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setPrompt("Escanea el código QR de la línea del bus:");
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCameraId(0);
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setBarcodeImageEnabled(true);
        launcher.launch(scanOptions);
    }

    ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), result -> {
        if (result != null) {
            if (result.getContents() != null) {
                String lineaBusUid = result.getContents();
                // Ya está en viaje o hubo un viaje que finalizó:
                if(firebaseViewModel.getUsuarioActual().getValue().getLineaBus() != null && firebaseViewModel.getUsuarioActual().getValue().getLineaBus().getUid().equals(lineaBusUid)){
                    if(firebaseViewModel.getUsuarioActual().getValue().getFechaInicio() != null && firebaseViewModel.getUsuarioActual().getValue().getFechaFin() != null){
                        // Hubo un viaje que finalizó:

                        // Validamos si el QR corresponde a una línea de bus para empezar el viaje:
                        firebaseViewModel.validarDocumentoPorUid(lineaBusUid)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Boolean existe = task.getResult();
                                        if (existe) {
                                            firebaseViewModel.obtenerLineaBusActual(lineaBusUid);
                                            firebaseViewModel.recargarLineaBusActual(lineaBusUid);
                                            empezarViaje(lineaBusUid);
                                        } else {
                                            mostrarDialogGeneral("Atención!","El código QR leído no corresponde a ninguna línea de bus registrada");
                                        }
                                    }
                                });
                    }else{
                        // Está en un viaje:

                        // Validamos si el QR corresponde a una línea de bus para finalizar el viaje:
                        firebaseViewModel.validarDocumentoPorUid(lineaBusUid)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Boolean existe = task.getResult();
                                        if (existe) {
                                            if(lineaBusUid.equals(firebaseViewModel.getUsuarioActual().getValue().getLineaBus().getUid())){
                                                firebaseViewModel.obtenerLineaBusActual(lineaBusUid);
                                                firebaseViewModel.recargarLineaBusActual(lineaBusUid);
                                                finalizarViaje(lineaBusUid);
                                            }else{
                                                mostrarDialogGeneral("Atención!","El código QR leído no corresponde a la línea de bus de su viaje actual");
                                            }
                                        } else {
                                            mostrarDialogGeneral("Atención!","El código QR leído no corresponde a ninguna línea de bus registrada");
                                        }
                                    }
                                });
                    }

                }else
                    // Validamos si el QR corresponde a una línea de bus para empezar el viaje:
                    firebaseViewModel.validarDocumentoPorUid(lineaBusUid)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Boolean existe = task.getResult();
                                    if (existe) {
                                        if(firebaseViewModel.getUsuarioActual().getValue().getLineaBus() == null|| lineaBusUid.equals(firebaseViewModel.getUsuarioActual().getValue().getLineaBus().getUid())){
                                            firebaseViewModel.obtenerLineaBusActual(lineaBusUid);
                                            firebaseViewModel.recargarLineaBusActual(lineaBusUid);
                                            empezarViaje(lineaBusUid);
                                        }else{
                                            mostrarDialogGeneral("Atención!","El código QR leído no corresponde a la línea de bus de su viaje actual");
                                        }
                                    } else {
                                        mostrarDialogGeneral("Atención!","El código QR leído no corresponde a ninguna línea de bus registrada");
                                    }
                                }
                            });
            } else {
                // Si el qr no funcionó :v
            }
        }
    });

    // -> Métodos para los pagos, recaudación y el cashback:
    public void empezarViaje(String lineaBusUid){
        mostrarDialogoProgreso();
        db.collection("lineaBus").document(lineaBusUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        HashMap<String,Object> campos = new HashMap<>();
                        LineaBusDTO lineaBusDTO = documentSnapshot.toObject(LineaBusDTO.class);
                        // Asignación de fecha de inicio:
                        campos.put("fechaInicio", new Timestamp(new Date()));
                        // Reasignación de fecha de fin:
                        campos.put("fechaFin", null);
                        // Asignación de referencia de bus:
                        campos.put("refLineaBus", db.collection("lineaBus").document(lineaBusUid));
                        // Actualización de saldo (descuento del costo original):
                        Log.d("TAG", "empezarViaje: 2 "+firebaseViewModel.getUsuarioActual().getValue().getSaldo());
                        // Validación de suscripción:
                        Log.d("TAG", "empezarViaje: 3 "+firebaseViewModel.getUsuarioActual().getValue().getLineaBusSuscripcion().size());
                        if(firebaseViewModel.getUsuarioActual().getValue() != null && firebaseViewModel.getUsuarioActual().getValue().getLineaBus() != null) {
                            for(LineaBus lb: firebaseViewModel.getUsuarioActual().getValue().getLineaBusSuscripcion()){
                                if(lb.getUid().equals(lineaBusUid)){
                                    tieneSuscripcion = true;
                                    break;
                                }else{
                                    tieneSuscripcion = false;
                                }
                            }
                        }
                        Log.d("TAG", "empezarViaje: 4 suscri "+tieneSuscripcion);


                        if(!tieneSuscripcion){
                            campos.put("saldo", Double.parseDouble(String.format("%.1f",firebaseViewModel.getUsuarioActual().getValue().getSaldo()-lineaBusDTO.getPrecioUnitario())));

                            // Actualización de la recaudación del bus y de la empresa:
                            db.collection("lineaBus").document(lineaBusUid).update("recaudacion",Double.parseDouble(String.format("%.1f",lineaBusDTO.getRecaudacion()+lineaBusDTO.getPrecioUnitario())))
                                    .addOnSuccessListener(documentSnapshot1 -> {
                                        Log.d("TAG", "empezarViaje: 5  ");
                                        lineaBusDTO.getRefEmpresa().get()
                                                        .addOnSuccessListener(documentSnapshot2 -> {
                                                            if(documentSnapshot2.exists()){
                                                                Log.d("TAG", "empezarViaje: 6  ");
                                                                UsuarioDTO empresaDTO = documentSnapshot2.toObject(UsuarioDTO.class);
                                                                lineaBusDTO.getRefEmpresa().update("recaudacion",Double.parseDouble(String.format("%.1f",empresaDTO.getRecaudacion()+lineaBusDTO.getPrecioUnitario())))
                                                                        .addOnSuccessListener(documentSnapshot3 -> {
                                                                            Log.d("TAG", "empezarViaje: 7  ");
                                                                            db.collection("usuario").document(auth.getUid()).update(campos)
                                                                                    .addOnSuccessListener(documentSnapshot4 -> {
                                                                                        Log.d("TAG", "empezarViaje: 8  ");
                                                                                        cerrarDialogoProgreso();
                                                                                        actualizarVistaFlujoViaje(firebaseViewModel.getUsuarioActual().getValue());
                                                                                        mostrarDialogGeneral("Enhorabuena!","Acaba de iniciar su viaje con la línea '"+lineaBusDTO.getNombre()+"'.");
                                                                                    });
                                                                        });
                                                            }
                                                        });
                                    });
                        }else{
                            db.collection("usuario").document(auth.getUid()).update(campos)
                                    .addOnSuccessListener(documentSnapshot4 -> {
                                        cerrarDialogoProgreso();
                                        actualizarVistaFlujoViaje(firebaseViewModel.getUsuarioActual().getValue());
                                        mostrarDialogGeneral("Enhorabuena!","Acaba de iniciar su viaje con su suscripción a la línea '"+lineaBusDTO.getNombre()+"'.");
                                    });
                        }
                    }
                });
    }

    public void finalizarViaje(String lineaBusUid){
        mostrarDialogoProgreso();
        db.collection("lineaBus").document(lineaBusUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        HashMap<String,Object> campos = new HashMap<>();
                        LineaBusDTO lineaBusDTO = documentSnapshot.toObject(LineaBusDTO.class);
                        // Asignación de fecha de fin:
                        Timestamp fechaFin = new Timestamp(new Date());
                        campos.put("fechaFin", new Timestamp(new Date()));
                        long diferenciaMinutos = obtenerDiferenciaEnMinutos(fechaFin,firebaseViewModel.getUsuarioActual().getValue().getFechaInicio());

                        Log.d("TAG", "diferencia en minutos: "+diferenciaMinutos);

                        // Validación de suscripción:
                        if(firebaseViewModel.getUsuarioActual().getValue() != null && firebaseViewModel.getUsuarioActual().getValue().getLineaBus() != null) {
                            for(LineaBus lb: firebaseViewModel.getUsuarioActual().getValue().getLineaBusSuscripcion()){
                                if(lb.getUid().equals(lineaBusUid)){
                                    tieneSuscripcion = true;
                                }
                            }
                        }

                        if(!tieneSuscripcion){

                            // Actualización de saldo y recaudación (cálculo de cashback):

                            Double cashback;
                            Integer cashbackPorcentaje;

                            if(diferenciaMinutos<umbralTiempo){
                                cashback = lineaBusDTO.getPrecioUnitario()*0.2;
                                cashbackPorcentaje = 20;
                            }else{
                                cashback = lineaBusDTO.getPrecioUnitario()*0.05;
                                cashbackPorcentaje = 5;
                            }

                            campos.put("saldo", Double.parseDouble(String.format("%.1f",firebaseViewModel.getUsuarioActual().getValue().getSaldo()+cashback)));

                            db.collection("lineaBus").document(lineaBusUid).update("recaudacion",Double.parseDouble(String.format("%.1f",lineaBusDTO.getRecaudacion()-cashback)))
                                    .addOnSuccessListener(documentSnapshot1 -> {
                                        lineaBusDTO.getRefEmpresa().get()
                                                .addOnSuccessListener(documentSnapshot2 -> {
                                                    if(documentSnapshot2.exists()){
                                                        UsuarioDTO empresaDTO = documentSnapshot2.toObject(UsuarioDTO.class);
                                                        lineaBusDTO.getRefEmpresa().update("recaudacion",Double.parseDouble(String.format("%.1f",empresaDTO.getRecaudacion()-cashback)))
                                                                .addOnSuccessListener(documentSnapshot3 -> {
                                                                    db.collection("usuario").document(auth.getUid()).update(campos)
                                                                            .addOnSuccessListener(documentSnapshot4 -> {
                                                                                cerrarDialogoProgreso();
                                                                                actualizarVistaFlujoViaje(firebaseViewModel.getUsuarioActual().getValue());
                                                                                mostrarDialogGeneral("Felicidades!","Acaba de finalizar su viaje con la línea '"+lineaBusDTO.getNombre()+"' con un cashback del "+cashbackPorcentaje+"%.");
                                                                            });
                                                                });
                                                    }
                                                });
                                    });
                        }else{
                            db.collection("usuario").document(auth.getUid()).update(campos)
                                    .addOnSuccessListener(documentSnapshot4 -> {
                                        cerrarDialogoProgreso();
                                        actualizarVistaFlujoViaje(firebaseViewModel.getUsuarioActual().getValue());
                                        mostrarDialogGeneral("Felicidades!","Acaba de finalizar su viaje con su suscripción a la línea '"+lineaBusDTO.getNombre()+"'.");
                                    });
                        }
                    }
                });
    }

    // -> Diálogos:
    private void mostrarDialogoProgreso() {
        View view = getLayoutInflater().inflate(R.layout.progreso, null);
        progresoDialog = new MaterialAlertDialogBuilder(getContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
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

    public void mostrarDialogGeneral(String titulo, String mensaje){
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Entendido", (dialog, which) -> {
                   dialog.dismiss();
                })
                .show();
    }

    // -> Funciones auxiliares:
    public long obtenerDiferenciaEnMinutos(Timestamp timestamp1, Timestamp timestamp2) {
        if (timestamp1 == null || timestamp2 == null) {
            throw new IllegalArgumentException("Los Timestamps no pueden ser nulos");
        }

        long tiempo1 = timestamp1.getSeconds() * 1000 + timestamp1.getNanoseconds() / 1000000;
        long tiempo2 = timestamp2.getSeconds() * 1000 + timestamp2.getNanoseconds() / 1000000;
        long diferenciaEnMilisegundos = Math.abs(tiempo1 - tiempo2);
        long diferenciaEnMinutos = diferenciaEnMilisegundos / (1000 * 60);

        return diferenciaEnMinutos;
    }


}