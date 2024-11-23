package com.example.subeteya.Adapters;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.subeteya.Activities.EditActivity;
import com.example.subeteya.Modules.GlideApp;
import com.example.subeteya.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;

public class FotoEmpresaCarruselAdapter extends RecyclerView.Adapter<FotoEmpresaCarruselAdapter.FotoEmpresaCarruselViewHolder> {


    // -------------------
    //      ATRIBUTOS:
    // -------------------
    private List<String> listaRutasCarrusel;
    private HashMap<String, Uri> listaUriCarrusel;
    EditActivity editActivity;

    public FotoEmpresaCarruselAdapter(EditActivity editActivity) {
        this.editActivity = editActivity;
    }

    @NonNull
    @Override
    public FotoEmpresaCarruselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_foto_carrusel, parent, false);
        return new FotoEmpresaCarruselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoEmpresaCarruselViewHolder holder, int position) {
        String fotoRuta = listaRutasCarrusel.get(position);
        holder.bind(fotoRuta,holder,position);
    }

    @Override
    public int getItemCount() {
        return listaRutasCarrusel.size();
    }

    public void setListas(List<String> listaRutasCarrusel, HashMap<String, Uri> listaUriCarrusel) {
        this.listaRutasCarrusel = listaRutasCarrusel;
        this.listaUriCarrusel = listaUriCarrusel;
        notifyDataSetChanged();
    }

    public class FotoEmpresaCarruselViewHolder extends RecyclerView.ViewHolder {
        private ImageView fotoImageView;
        private CircularProgressIndicator progressParaImageView;
        private LinearLayout progressLayout;
        private ImageButton btnEditar;
        private ImageButton btnEliminar;

        FotoEmpresaCarruselViewHolder(View itemView) {
            super(itemView);
            fotoImageView = itemView.findViewById(R.id.fotito_carrusel);
            progressParaImageView = itemView.findViewById(R.id.progressParaImageViewCarrusel);
            progressLayout = itemView.findViewById(R.id.progress_layout_carrusel);
            btnEditar = itemView.findViewById(R.id.btn_edit);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
        }

        void bind(String fotoRuta, FotoEmpresaCarruselViewHolder holder, int position) {

            // Vista:
            if(position == 0){
                btnEditar.setVisibility(View.GONE);
                btnEliminar.setVisibility(View.GONE);
            }else{
                btnEditar.setVisibility(View.VISIBLE);
                btnEliminar.setVisibility(View.VISIBLE);
            }

            // Botones:
            btnEditar.setOnClickListener(l -> {
                editActivity.abrirSelectorDeFotosAlt(position);
            });

            btnEliminar.setOnClickListener(l -> {
                editActivity.eliminarFotoLocal(position);
            });

            // Carga de imágenes:

            if(listaUriCarrusel.containsKey(fotoRuta)){
                fotoImageView.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
                // Carga desde la Uri:
                Glide.with(editActivity)
                        .load(listaUriCarrusel.get(fotoRuta))
                        .into(fotoImageView);
            }else{
                // Carga desde Firebase Storage:
                cargarImagenDesdeStorage(fotoRuta);
            }



        }

        public void cargarImagenDesdeStorage(String ruta) {
            progressLayout.setVisibility(View.VISIBLE);
            fotoImageView.setVisibility(View.INVISIBLE);
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(ruta);
            GlideApp.with(editActivity)
                    .load(storageRef)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.d("TAG", "onLoadFailed: error");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            fotoImageView.setVisibility(View.VISIBLE);
                            progressLayout.setVisibility(View.GONE);
                            Log.d("TAG", "onLoad: YEI");
                            return false;
                        }
                    })
                    .into(fotoImageView);
        }
    }
}
