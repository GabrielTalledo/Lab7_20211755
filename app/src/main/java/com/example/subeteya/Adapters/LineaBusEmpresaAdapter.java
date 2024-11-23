package com.example.subeteya.Adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.subeteya.Activities.DetailsActivity;
import com.example.subeteya.Activities.EditActivity;
import com.example.subeteya.Activities.EmpresaActivity;
import com.example.subeteya.Activities.UsuarioOperativoActivity;
import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.Modules.GlideApp;
import com.example.subeteya.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.List;

public class LineaBusEmpresaAdapter extends RecyclerView.Adapter<LineaBusEmpresaAdapter.LineaBusViewHolder> {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    private List<LineaBus> listaLineasBusesEmpresa = new ArrayList<>();
    private EmpresaActivity empresaActivity;


    public LineaBusEmpresaAdapter(EmpresaActivity empresaActivity) {
        this.empresaActivity = empresaActivity;
    }


    @Override
    public LineaBusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_elemento_alt, parent, false);
        return new LineaBusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LineaBusViewHolder holder, int position) {
        LineaBus lineaBus = listaLineasBusesEmpresa.get(position);
        holder.bind(lineaBus,holder);
    }

    @Override
    public int getItemCount() {
        return listaLineasBusesEmpresa.size();
    }

    public void setListaLineasBusesEmpresa(List<LineaBus> listaLineasBuses) {
        this.listaLineasBusesEmpresa = listaLineasBuses;
        notifyDataSetChanged();
    }

    class LineaBusViewHolder extends RecyclerView.ViewHolder {
        private TextView nombreTextView;
        private TextView recaudacionTextView;
        private ShapeableImageView fotoImageView;
        private Button btnEditar;
        private Button btnQr;
        private CircularProgressIndicator progressParaImageView;
        private LinearLayout progressLayout;

        LineaBusViewHolder(View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.text_nombre_linea_bus);
            recaudacionTextView = itemView.findViewById(R.id.text_recaudacion_bus);
            fotoImageView = itemView.findViewById(R.id.circularImageView);
            btnEditar = itemView.findViewById(R.id.btn_editar_linea_bus);
            btnQr = itemView.findViewById(R.id.btn_qr_linea_bus);
            progressParaImageView = itemView.findViewById(R.id.progressParaImageView);
            progressLayout = itemView.findViewById(R.id.progress_layout);
        }

        void bind(LineaBus lineaBus, LineaBusViewHolder holder) {
            nombreTextView.setText(lineaBus.getNombre());
            recaudacionTextView.setText("S/. " + lineaBus.getRecaudacion());
            progressLayout.setVisibility(View.VISIBLE);
            progressParaImageView.setVisibility(View.VISIBLE);
            fotoImageView.setVisibility(View.GONE);

            cargarImagenDesdeStorage(lineaBus);

            btnEditar.setOnClickListener(l -> {
                Intent intent = new Intent(empresaActivity, EditActivity.class);
                intent.putExtra("lineaBus", lineaBus);
                empresaActivity.startActivity(intent);
            });

            btnQr.setOnClickListener(l -> {
                mostrarDialogQr(lineaBus);
            });
        }

        public void cargarImagenDesdeStorage(LineaBus lineaBus) {
            // Se carga solo la primera foto del carrusel:
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(lineaBus.getRutasCarrusel().get(0));
            GlideApp.with(empresaActivity)
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
        }

        public void mostrarDialogQr(LineaBus lineaBus){

            int tamanho = 700;
            String texto = lineaBus.getUid();

            try {
                // Creamos el QR:
                BitMatrix bitMatrix = new MultiFormatWriter().encode(
                        texto, BarcodeFormat.QR_CODE, tamanho, tamanho);
                Bitmap bitmap = Bitmap.createBitmap(tamanho, tamanho, Bitmap.Config.RGB_565);
                for (int x = 0; x < tamanho; x++) {
                    for (int y = 0; y < tamanho; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }

                // Creamos el imageview para el dialog:
                ImageView imageView = new ImageView(empresaActivity);
                imageView.setImageBitmap(bitmap);
                imageView.setPadding(50, 50, 50, 50);

                // Creamos el dialog:
                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(empresaActivity);
                dialogBuilder.setTitle("Código QR")
                        .setView(imageView)
                        .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                        .show();

            } catch (WriterException e) {
                e.printStackTrace();
                Toast.makeText(empresaActivity, "Error al generar el código QR", Toast.LENGTH_SHORT).show();
            }


        }
    }


}

