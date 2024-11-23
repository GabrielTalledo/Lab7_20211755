package com.example.subeteya.Adapters;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.subeteya.Activities.DetailsActivity;
import com.example.subeteya.Activities.UsuarioOperativoActivity;
import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.Modules.GlideApp;
import com.example.subeteya.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class LineaBusAdapter extends RecyclerView.Adapter<LineaBusAdapter.LineaBusViewHolder> {
    
    // -------------------
    //      ATRIBUTOS:
    // -------------------
    private List<LineaBus> listaLineasBuses = new ArrayList<>();
    private UsuarioOperativoActivity usuarioActivity;


    public LineaBusAdapter(UsuarioOperativoActivity UsuarioOperativoActivity) {
        this.usuarioActivity = UsuarioOperativoActivity;
    }


    @Override
    public LineaBusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(com.example.subeteya.R.layout.item_elemento, parent, false);
        return new LineaBusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LineaBusViewHolder holder, int position) {
        LineaBus lineaBus = listaLineasBuses.get(position);
        holder.bind(lineaBus,holder);
    }

    @Override
    public int getItemCount() {
        return listaLineasBuses.size();
    }

    public void setListaLineasBuses(List<LineaBus> listaLineasBuses) {
        this.listaLineasBuses = listaLineasBuses;
        notifyDataSetChanged();
    }

    class LineaBusViewHolder extends RecyclerView.ViewHolder {
        private TextView nombreTextView;
        private ShapeableImageView fotoImageView;
        private Button btnInformacion;
        private CircularProgressIndicator progressParaImageView;
        private LinearLayout progressLayout;

        LineaBusViewHolder(View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.text_nombre_linea_bus);
            fotoImageView = itemView.findViewById(R.id.circularImageView);
            btnInformacion = itemView.findViewById(R.id.btn_informacion_linea_bus);
            progressParaImageView = itemView.findViewById(R.id.progressParaImageView);
            progressLayout = itemView.findViewById(R.id.progress_layout);
        }

        void bind(LineaBus lineaBus, LineaBusViewHolder holder) {
            nombreTextView.setText(lineaBus.getNombre());
            progressLayout.setVisibility(View.VISIBLE);
            progressParaImageView.setVisibility(View.VISIBLE);
            fotoImageView.setVisibility(View.GONE);

            cargarImagenDesdeStorage(lineaBus);

            btnInformacion.setOnClickListener(l -> {
                Intent intent = new Intent(usuarioActivity, DetailsActivity.class);
                intent.putExtra("lineaBus", lineaBus);
                usuarioActivity.startActivity(intent);
            });
        }

        public void cargarImagenDesdeStorage(LineaBus lineaBus) {
            // Se carga solo la primera foto del carrusel:
            if(lineaBus.getRutasCarrusel() != null && !lineaBus.getRutasCarrusel().isEmpty()){
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(lineaBus.getRutasCarrusel().get(0));
                GlideApp.with(usuarioActivity)
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
                Glide.with(usuarioActivity)
                        .load(R.drawable.placeholder)
                        .into(fotoImageView);
            }

        }
    }


}

