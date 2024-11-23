package com.example.subeteya.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.subeteya.Adapters.FotoCarruselAdapter;
import com.example.subeteya.Adapters.FotoEmpresaCarruselAdapter;
import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.Beans.Usuario;
import com.example.subeteya.R;
import com.example.subeteya.Viewmodels.FirebaseViewModel;
import com.example.subeteya.databinding.ActivityDetailsBinding;
import com.example.subeteya.databinding.ActivityEditBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EditActivity extends AppCompatActivity {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    ActivityEditBinding binding;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseViewModel firebaseViewModel;
    RecyclerView rvCarrusel;
    FotoEmpresaCarruselAdapter carruselAdapter;
    LineaBus lineaBus;
    AlertDialog progresoDialog;

    // Elementos auxiliares:
    List<String> listaFotosRutasCarrusel;
    HashMap<String,Uri> listaUriCarrusel;
    List<String> listaFotosEliminar;
    Uri fotoUri;
    int posicion;

    // Códigos para intents:
    static int PERMISO_CAMARA = 92;
    static int INTENT_CAMARA = 1;
    static int INTENT_GALERIA = 2;
    static int INTENT_CAMARA_ALT = 3;
    static int INTENT_GALERIA_ALT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase:
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Binding:
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Vista inicial:
        binding.btnEditarBus.setText("Cargando");
        binding.btnEditarBus.setEnabled(false);
        binding.btnEditarBus.setAlpha(0.3f);



        // ------------
        //   Lógica:
        // ------------

        // -> Gestión del intent (Datos de la línea de bus):
        Intent intent = getIntent();
        if(intent.getSerializableExtra("lineaBus") != null){
            lineaBus = (LineaBus) intent.getSerializableExtra("lineaBus");
        }else{
            supportFinishAfterTransition();
        }

        // -> Listas para la edición del carrusel:
        if (lineaBus.getRutasCarrusel() != null && !lineaBus.getRutasCarrusel().isEmpty() ) {
            listaFotosRutasCarrusel = lineaBus.getRutasCarrusel();
        }else{
            listaFotosRutasCarrusel = new ArrayList<>();
        }
        listaUriCarrusel = new HashMap<>();
        listaFotosEliminar = new ArrayList<>();


        // -> Configuración de RecyclerView y Adapter:
        rvCarrusel = binding.rvCarruselBus;
        carruselAdapter = new FotoEmpresaCarruselAdapter(this);
        carruselAdapter.setListas(new ArrayList<>(),new HashMap<>());
        rvCarrusel.setAdapter(carruselAdapter);


        // -> Manejo de ViewModels:
        firebaseViewModel = new ViewModelProvider(this).get(FirebaseViewModel.class);
        firebaseViewModel.obtenerUsuarioActual(auth.getCurrentUser().getUid());
        firebaseViewModel.recargarUsuarioActual(auth.getCurrentUser().getUid());
        firebaseViewModel.obtenerLineaBusActual(lineaBus.getUid());
        firebaseViewModel.recargarLineaBusActual(lineaBus.getUid());

        firebaseViewModel.getUsuarioActual().observe(this, usuario -> {
            if(lineaBus.getRutasCarrusel() == null){
                lineaBus.setRutasCarrusel(new ArrayList<>());
            }
            actualizarUI(usuario,lineaBus);
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

        // -> Manejo de la edición del carrusel:
        binding.btnEditarBus.setOnClickListener(l -> {
            subirImagenesAFirebase();
        });

        // -> Manejo de botón nueva foto:
        binding.btnNuevaFotoBus.setOnClickListener(l -> {
            abrirSelectorDeFotos();
        });

        // -> Permisos de cámara:
        pedirPermisoCamara();
    }

    // -------------------
    //      MÉTODOS:
    // -------------------


    // -> Permisos cámara:
    private void pedirPermisoCamara(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},PERMISO_CAMARA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_CAMARA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        }else{
            mostrarDialogoPermisos();
        }
    }


    // -> Fotos:
    public void eliminarFotoLocal(int posicionAdapter){
        mostrarDialogoProgreso();
        posicion = posicionAdapter;
        if(listaUriCarrusel.containsKey(listaFotosRutasCarrusel.get(posicion))){
            listaUriCarrusel.remove(listaFotosRutasCarrusel.get(posicion));
            listaFotosRutasCarrusel.remove(posicion);
            posicion -= 1;
            cerrarDialogoProgreso();
            actualizarUI(firebaseViewModel.getUsuarioActual().getValue(),lineaBus);
        }else{
            String ruta = listaFotosRutasCarrusel.get(posicion);
            listaFotosRutasCarrusel.remove(posicion);
            posicion -= 1;
            listaFotosEliminar.add(ruta);
            cerrarDialogoProgreso();
            actualizarUI(firebaseViewModel.getUsuarioActual().getValue(),lineaBus);
        }
        //carruselAdapter.setListas(listaFotosRutasCarrusel,listaUriCarrusel);
    }


    public void eliminarFotoFirebase(String ruta){

        // Firestore:

        db.collection("lineaBus").document(lineaBus.getUid()).update("rutasCarrusel",listaFotosRutasCarrusel)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d("TAG", "eliminarFotoFirestore :D ");
                    }else{
                        Log.d("TAG", "eliminarFotoFirestore :C ");
                    }
                });

        // Storage:

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(ruta);
        storageRef.delete()
                .addOnCompleteListener(l -> {
                    if(l.isSuccessful()){
                        actualizarUI(firebaseViewModel.getUsuarioActual().getValue(),lineaBus);
                        cerrarDialogoProgreso();
                        Log.d("Firebase", "Se eliminó la imagen");
                    }else{
                        cerrarDialogoProgreso();
                        Log.d("Firebase", "Error al eliminar la imagen" );
                    }
                });
    }

    public void eliminarFotosAntiguas(){

        // Storage:
        for(String ruta: listaFotosEliminar){
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(ruta);
            storageRef.delete()
                    .addOnCompleteListener(l -> {
                        if(l.isSuccessful()){
                            Log.d("Firebase", "Se eliminó la imagen");
                        }else{
                            Log.d("Firebase", "Error al eliminar la imagen" );
                        }
                    });
        }
    }

    public void abrirSelectorDeFotosAlt(int posicionAdapter) {
        posicion = posicionAdapter;
        final CharSequence[] opciones = { "Tomar foto", "Elegir de galería", "Cancelar" };
        new MaterialAlertDialogBuilder(this).setTitle("Selecciona una opción:")
                .setItems(opciones, (dialog, item) -> {
                    if (opciones[item].equals("Tomar foto")) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},PERMISO_CAMARA);
                        } else {
                            File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            try{
                                File fotoFile = File.createTempFile("foto"+System.currentTimeMillis(), ".jpg",storageDirectory);
                                fotoUri = FileProvider.getUriForFile(this, "com.example.subeteya.fileprovider", fotoFile);
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                                startActivityForResult(intent, INTENT_CAMARA_ALT);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    } else if (opciones[item].equals("Elegir de galería")) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, INTENT_GALERIA_ALT);
                    } else if (opciones[item].equals("Cancelar")) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void abrirSelectorDeFotos() {
        final CharSequence[] opciones = { "Tomar foto", "Elegir de galería", "Cancelar" };
        new MaterialAlertDialogBuilder(this).setTitle("Selecciona una opción:")
                .setItems(opciones, (dialog, item) -> {
                    if (opciones[item].equals("Tomar foto")) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},PERMISO_CAMARA);
                        } else {
                            File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            try{
                                File fotoFile = File.createTempFile("foto"+System.currentTimeMillis(), ".jpg",storageDirectory);
                                fotoUri = FileProvider.getUriForFile(this, "com.example.subeteya.fileprovider", fotoFile);
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                                startActivityForResult(intent, INTENT_CAMARA);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    } else if (opciones[item].equals("Elegir de galería")) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, INTENT_GALERIA);
                    } else if (opciones[item].equals("Cancelar")) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Botón de nueva foto:
            if (requestCode == INTENT_GALERIA) {
                fotoUri = data.getData();
                // Creación:
                String ruta = "carrusel/" + lineaBus.getUid() +"-"+ System.currentTimeMillis() + new Random().nextInt(69) + ".jpg";
                listaFotosRutasCarrusel.add(ruta);
                listaUriCarrusel.put(ruta,fotoUri);
                posicion = listaFotosRutasCarrusel.size()-1;
                actualizarUI(firebaseViewModel.getUsuarioActual().getValue(),lineaBus);
                //carruselAdapter.setListas(listaFotosRutasCarrusel,listaUriCarrusel);
            }
            if (requestCode == INTENT_CAMARA){
                // Creación:
                String ruta = "carrusel/" + lineaBus.getUid() +"-"+ System.currentTimeMillis() + new Random().nextInt(69) + ".jpg";
                listaFotosRutasCarrusel.add(ruta);
                listaUriCarrusel.put(ruta,fotoUri);
                posicion = listaFotosRutasCarrusel.size()-1;
                actualizarUI(firebaseViewModel.getUsuarioActual().getValue(),lineaBus);
                //carruselAdapter.setListas(listaFotosRutasCarrusel,listaUriCarrusel);
            }
            // Botón de editar foto:
            if (requestCode == INTENT_GALERIA_ALT) {
                fotoUri = data.getData();
                String ruta = listaFotosRutasCarrusel.get(posicion);

                // Guaradamos la imagen antigua para eliminarla:
                listaFotosEliminar.add(ruta);

                // Actualizamos la lista:
                listaUriCarrusel.remove(ruta);
                listaFotosRutasCarrusel.remove(posicion);
                ruta = "carrusel/" + lineaBus.getUid() +"-"+ System.currentTimeMillis() + new Random().nextInt(69) + ".jpg";
                listaFotosRutasCarrusel.add(posicion,ruta);
                listaUriCarrusel.put(ruta,fotoUri);
                actualizarUI(firebaseViewModel.getUsuarioActual().getValue(),lineaBus);
                //carruselAdapter.setListas(listaFotosRutasCarrusel,listaUriCarrusel);
            }
            if (requestCode == INTENT_CAMARA_ALT){
                String ruta = listaFotosRutasCarrusel.get(posicion);

                // Guaradamos la imagen antigua para eliminarla:
                listaFotosEliminar.add(ruta);

                // Actualizamos la lista:
                listaUriCarrusel.remove(ruta);
                listaFotosRutasCarrusel.remove(posicion);
                ruta = "carrusel/" + lineaBus.getUid() +"-"+ System.currentTimeMillis() + new Random().nextInt(69) + ".jpg";
                listaFotosRutasCarrusel.add(posicion,ruta);
                listaUriCarrusel.put(ruta,fotoUri);
                actualizarUI(firebaseViewModel.getUsuarioActual().getValue(),lineaBus);
                //carruselAdapter.setListas(listaFotosRutasCarrusel,listaUriCarrusel);
            }
        }
    }


    // -> Subir fotos:
    private void subirImagenesAFirebase() {

        mostrarDialogoProgreso();

        // Actualización en FireStore:

        db.collection("lineaBus").document(lineaBus.getUid()).update("rutasCarrusel",listaFotosRutasCarrusel)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d("TAG", "subirImagenesAFirestore :D ");
                    }else{
                        Log.d("TAG", "subirImagenesAFirestore :C ");
                    }
                });

        // Actualización en Storage:

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        int size = listaUriCarrusel.size();
        int index = 0;

        eliminarFotosAntiguas();

        if(size<1){
            cerrarDialogoProgreso();
            Intent intent = new Intent(this, EmpresaActivity.class);
            if(listaFotosEliminar.isEmpty()){
                intent.putExtra("mensaje","La línea de bus '"+lineaBus.getNombre()+"' no tuvo cambios adicionales.");
            }else{
                intent.putExtra("mensaje","Se editó exitosamente el carrusel de fotos de la línea '"+lineaBus.getNombre()+"'.");

            }
            startActivity(intent);
            supportFinishAfterTransition();
        }

        for (Map.Entry<String, Uri> entry : listaUriCarrusel.entrySet()) {
            index++;

            // Ruta:
            StorageReference archivoRef = storageRef.child(entry.getKey());

            if (index == size) {
                archivoRef.putFile(entry.getValue())
                        .addOnSuccessListener(taskSnapshot -> {
                            Log.d("Firebase", "Imagen subida. ");
                            cerrarDialogoProgreso();
                            Intent intent = new Intent(this, EmpresaActivity.class);
                            intent.putExtra("mensaje","Se editó exitosamente el carrusel de fotos de la línea '"+lineaBus.getNombre()+"'.");
                            startActivity(intent);
                            supportFinishAfterTransition();
                        })
                        .addOnFailureListener(e -> {
                            cerrarDialogoProgreso();
                            Log.e("Firebase", "Error al subir la imagen: " + e.getMessage());
                        });
            } else {
                archivoRef.putFile(entry.getValue())
                        .addOnSuccessListener(taskSnapshot -> {
                            Log.d("Firebase", "Imagen subida. ");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firebase", "Error al subir la imagen: " + e.getMessage());
                        });
            }
        }

    }


    // -> Diálogos:
    private void mostrarDialogoPermisos() {
        new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setCancelable(false)
                .setTitle("Permisos requeridos!")
                .setMessage("Para poder seguir con la edición del carrusel, debes otorgar permisos de cámara.")
                .setPositiveButton("Configuración", (dialog, which) -> {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Salir", (dialog, which) -> {
                    supportFinishAfterTransition();
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

    // -> Vistas:
    public void actualizarUI(Usuario usuario, LineaBus lineaBus){
        if(usuario == null){
            binding.btnEditarBus.setText("Cargando");
            binding.btnEditarBus.setEnabled(false);
            binding.btnEditarBus.setAlpha(0.3f);
        }else{
            binding.textNombreLineaBus.setText(lineaBus.getNombre());
            binding.textPrecioUnitarioBus.setText("S/. "+lineaBus.getPrecioUnitario().toString());
            binding.textPrecioSuscripcionBus.setText("S/. "+lineaBus.getPrecioSuscripcion().toString());
            binding.textRecaudacionBusEmpresa.setText("S/. "+String.format("%.1f",lineaBus.getRecaudacion()));

            binding.btnEditarBus.setText("Editar");
            binding.btnEditarBus.setEnabled(true);
            binding.btnEditarBus.setAlpha(1.0f);
            carruselAdapter.setListas(listaFotosRutasCarrusel,listaUriCarrusel);
            if(posicion > 0){
                rvCarrusel.smoothScrollToPosition(posicion);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}