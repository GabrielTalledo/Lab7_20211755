package com.example.subeteya.Adapters;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.subeteya.Activities.DetailsActivity;
import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.Modules.GlideApp;
import com.example.subeteya.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FotoCarruselAdapter extends RecyclerView.Adapter<FotoCarruselAdapter.FotoCarruselViewHolder> {


    // -------------------
    //      ATRIBUTOS:
    // -------------------
    private List<String> listaRutasCarrusel;
    DetailsActivity detailsActivity;
    boolean flag;
    int fotoPlaceholder = R.drawable.placeholder;

    public FotoCarruselAdapter(DetailsActivity detailsActivity) {
        this.flag = true;
        this.detailsActivity = detailsActivity;
    }

    @NonNull
    @Override
    public FotoCarruselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_foto, parent, false);
        return new FotoCarruselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoCarruselViewHolder holder, int position) {
        String fotoRuta = listaRutasCarrusel.get(position);
        holder.bind(fotoRuta,holder);
    }

    @Override
    public int getItemCount() {
        if(listaRutasCarrusel == null){
            return 0;
        }
        return listaRutasCarrusel.size();
    }

    public void setListaRutasCarrusel(List<String> listaRutasCarrusel) {
        flag = true;
        if(listaRutasCarrusel == null || listaRutasCarrusel.isEmpty()){
            flag = false;
            listaRutasCarrusel = new ArrayList<>();
            listaRutasCarrusel.add("R.drawable.placeholder");
        }
        this.listaRutasCarrusel = listaRutasCarrusel;
        notifyDataSetChanged();
    }

    public class FotoCarruselViewHolder extends RecyclerView.ViewHolder {
        private ImageView fotoImageView;
        private CircularProgressIndicator progressParaImageView;
        private LinearLayout progressLayout;

        FotoCarruselViewHolder(View itemView) {
            super(itemView);
            fotoImageView = itemView.findViewById(R.id.fotito_carrusel);
            progressParaImageView = itemView.findViewById(R.id.progressParaImageViewCarrusel);
            progressLayout = itemView.findViewById(R.id.progress_layout_carrusel);
        }

        void bind(String fotoRuta, FotoCarruselAdapter.FotoCarruselViewHolder holder) {
            Log.d("TAG", "OLI");
            progressLayout.setVisibility(View.VISIBLE);
            fotoImageView.setVisibility(View.INVISIBLE);
            Log.d("TAG", "OLI2");
            cargarImagenDesdeStorage(fotoRuta);
        }

        public void cargarImagenDesdeStorage(String ruta) {

            if(!flag){
                fotoImageView.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
                Glide.with(detailsActivity)
                        .load(fotoPlaceholder)
                        .into(fotoImageView);
            }else{
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(ruta);
                GlideApp.with(detailsActivity)
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
}
