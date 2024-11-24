package com.example.subeteya.Adapters;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.subeteya.Activities.UsuarioOperativoActivity;
import com.example.subeteya.Beans.Auxiliar;
import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.Beans.Viaje;
import com.example.subeteya.Modules.GlideApp;
import com.example.subeteya.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    private Auxiliar auxiliar;
    private UsuarioOperativoActivity usuarioOperativoActivity;


    public HistorialAdapter(UsuarioOperativoActivity usuarioOperativoActivity) {
        this.usuarioOperativoActivity = usuarioOperativoActivity;
    }


    @Override
    public HistorialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_elemento_viaje, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistorialViewHolder holder, int position) {

        Viaje viaje = auxiliar.getListaViajes().get(position);
        Log.d("TAG", "onBindViewHolder: viaje "+viaje.getFechaInicioBonita());
        LineaBus lineaBus = auxiliar.getListaLineasBuses().get(position);
        Log.d("TAG", "onBindViewHolder: bus "+lineaBus.getNombre());
        holder.bind(viaje,lineaBus,holder,position);
    }

    @Override
    public int getItemCount() {
        return auxiliar.getListaViajes().size();
    }

    public void setAuxiliar(Auxiliar auxiliar) {
        this.auxiliar = auxiliar;
        Log.d("TAG", "setAuxiliar "+auxiliar.getListaViajes().size());
        notifyDataSetChanged();
    }

    class HistorialViewHolder extends RecyclerView.ViewHolder {
        private TextView nombreTextView;
        private TextView fechaInicioTextView;
        private TextView fechaFinTextView;
        private ShapeableImageView fotoImageView;
        private Button btnDetalles;
        private CircularProgressIndicator progressParaImageView;
        private LinearLayout progressLayout;

        HistorialViewHolder(View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.text_nombre_linea_bus);
            fechaInicioTextView = itemView.findViewById(R.id.text_fecha_inicio_viaje_historial);
            fechaFinTextView = itemView.findViewById(R.id.text_fecha_fin_viaje_historial);
            fotoImageView = itemView.findViewById(R.id.circularImageView);
            btnDetalles = itemView.findViewById(R.id.btn_detalles_historial);
            progressParaImageView = itemView.findViewById(R.id.progressParaImageView);
            progressLayout = itemView.findViewById(R.id.progress_layout);
        }

        void bind(Viaje viaje, LineaBus lineaBus, HistorialViewHolder holder, int position) {
            Log.d("TAG", "bind "+viaje.getFechaInicioBonitaMetodo());
            nombreTextView.setText("#"+(position+1)+" - "+lineaBus.getNombre());
            fechaInicioTextView.setText(viaje.getFechaInicioBonitaMetodo());
            fechaFinTextView.setText(viaje.getFechaFinBonitaMetodo());
            progressLayout.setVisibility(View.VISIBLE);
            progressParaImageView.setVisibility(View.VISIBLE);
            fotoImageView.setVisibility(View.GONE);

            cargarImagenDesdeStorage(lineaBus);

            btnDetalles.setOnClickListener(l -> {
                usuarioOperativoActivity.mostrarViajeDialog(viaje,lineaBus,position+1);
            });
        }

        public void cargarImagenDesdeStorage(LineaBus lineaBus) {
            if(lineaBus.getRutasCarrusel() != null && !lineaBus.getRutasCarrusel().isEmpty()){
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(lineaBus.getRutasCarrusel().get(0));
                GlideApp.with(usuarioOperativoActivity)
                        .load(storageRef)
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressParaImageView.setVisibility(View.GONE);
                                progressLayout.setVisibility(View.GONE);
                                fotoImageView.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .into(fotoImageView);
            }else{
                progressParaImageView.setVisibility(View.GONE);
                progressLayout.setVisibility(View.GONE);
                fotoImageView.setVisibility(View.VISIBLE);
                Glide.with(usuarioOperativoActivity)
                        .load(R.drawable.placeholder)
                        .into(fotoImageView);
            }
        }

    }


}

