package com.example.subeteya.Dialogs;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION_CODES.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.Beans.Viaje;
import com.example.subeteya.Modules.GlideApp;
import com.example.subeteya.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ViajeBottomSheetDialog extends BottomSheetDialogFragment {

    TextView numeroViajeTextView;
    TextView nombreBusTextView;
    TextView fechaInicioTextView;
    TextView fechaFinTextView;
    TextView costoOriginalTextView;
    TextView costoFinalTextView;
    TextView duracionTextView;

    ShapeableImageView fotoImageView;

    Viaje viaje;
    LineaBus lineaBus;
    int numeroViaje = 0;

    FirebaseFirestore db;


    public ViajeBottomSheetDialog(Viaje viaje,LineaBus lineaBus,int numeroViaje){
        this.viaje = viaje;
        this.lineaBus = lineaBus;
        this.numeroViaje = numeroViaje;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(com.example.subeteya.R.layout.dialog_viaje, container, false);

        // Mapeo de campos:

        numeroViajeTextView = view.findViewById(com.example.subeteya.R.id.numero_viaje);
        nombreBusTextView = view.findViewById(com.example.subeteya.R.id.text_linea_bus_viaje);
        fechaInicioTextView = view.findViewById(com.example.subeteya.R.id.text_fecha_inicio_viaje);
        fechaFinTextView = view.findViewById(com.example.subeteya.R.id.text_fecha_fin_viaje);
        costoOriginalTextView = view.findViewById(com.example.subeteya.R.id.text_costo_original_viaje);
        costoFinalTextView = view.findViewById(com.example.subeteya.R.id.text_costo_final_viaje);
        fotoImageView = view.findViewById(com.example.subeteya.R.id.circularImageView);
        duracionTextView = view.findViewById(com.example.subeteya.R.id.text_duracion_viaje);

        // Seteo de campos:

        numeroViajeTextView.setText("Viaje N° "+numeroViaje);
        nombreBusTextView.setText(lineaBus.getNombre());
        fechaInicioTextView.setText(viaje.getFechaInicioBonitaMetodo());
        fechaFinTextView.setText(viaje.getFechaFinBonitaMetodo());
        costoOriginalTextView.setText("S/. "+viaje.getCostoOriginal());
        if (viaje.getCostoFinal() == null) {
            costoFinalTextView.setText("-");
        } else if (viaje.getCostoFinal() > 0.0f) {
            costoFinalTextView.setText("S/. " + viaje.getCostoFinal());
        } else {
            costoFinalTextView.setText("Ninguno, contaba con suscripción");
        }
        duracionTextView.setText(viaje.getDuracionBonitaMetodo());

        // Foto:

        if(lineaBus.getRutasCarrusel() != null && !lineaBus.getRutasCarrusel().isEmpty()){
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(lineaBus.getRutasCarrusel().get(0));
            GlideApp.with(this)
                    .load(storageRef)
                    .into(fotoImageView);
        }else{
            fotoImageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(com.example.subeteya.R.drawable.placeholder)
                    .into(fotoImageView);
        }

        return view;
    }

}

