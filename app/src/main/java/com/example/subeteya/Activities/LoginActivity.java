package com.example.subeteya.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.subeteya.DTOs.UsuarioDTO;
import com.example.subeteya.R;
import com.example.subeteya.databinding.ActivityLoginBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    ActivityLoginBinding binding;
    FirebaseFirestore db;
    FirebaseAuth auth;
    AlertDialog progresoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase:
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Binding:
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ------------
        //   Lógica:
        // ------------

        // -> Redireccionamiento por si hay sesión activa:
        mostrarDialogoProgreso();
        if(auth.getCurrentUser() != null) {
            db.collection("usuario").whereEqualTo("correo", auth.getCurrentUser().getEmail()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    cerrarDialogoProgreso();
                    Intent intent = new Intent();
                    if(task.getResult().toObjects(UsuarioDTO.class).get(0).getRol().equals("Usuario Operativo")){
                        intent = new Intent(LoginActivity.this, UsuarioOperativoActivity.class);
                    }else{
                        // Empresa de transporte:
                        intent = new Intent(LoginActivity.this, EmpresaActivity.class);
                    }
                    intent.putExtra("mensaje", "Bienvenido de vuelta!");
                    startActivity(intent);
                    supportFinishAfterTransition();
                }
            });
        }else{
            cerrarDialogoProgreso();
        }

        // -> Gestión del intent:
        if(getIntent().getStringExtra("mensaje") != null) {
            Snackbar.make(this, binding.getRoot(), getIntent().getStringExtra("mensaje"), Snackbar.LENGTH_LONG).show();
        }

        // -> Configuración de validaciones (estas no son necesarias en esta vista, pero las coloco opcionalmente):
        modificarBtnIngresar(false);
        validarCampoEmail();
        validarCampoPassword();

        // -> Registro de Usuario Operativo:
        binding.textRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // -> Inicio de sesión con Firebase Authentication:
        binding.btnIngresar.setOnClickListener(v -> {
            binding.fieldEmailLayout.setError(null);
            login(binding.fieldEmail.getText().toString().trim(),binding.fieldPassword.getText().toString().trim());
        });

    }

    // -------------------
    //      MÉTODOS:
    // -------------------

    // -> Login:

    private void login(String email,String password) {
        mostrarDialogoProgreso();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Si la autenticación es exitosa, obtenemos al usuario en firestore:
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Obtenemos el uid del usuario:
                            db.collection("usuario").whereEqualTo("correo", email).get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful() && task1.getResult() != null && !task1.getResult().isEmpty()) {
                                    UsuarioDTO usuarioDTO = task1.getResult().getDocuments().get(0).toObject(UsuarioDTO.class);
                                    cerrarDialogoProgreso();

                                    Intent intent = new Intent();

                                    // Redirigimos según el rol:
                                    if(usuarioDTO.getRol().equals("Usuario Operativo")){
                                        intent = new Intent(LoginActivity.this, UsuarioOperativoActivity.class);
                                    }else{
                                        // Empresa de transporte:
                                        intent = new Intent(LoginActivity.this, EmpresaActivity.class);
                                    }

                                    intent.putExtra("mensaje", "Inició sesión con éxito!");
                                    startActivity(intent);
                                    supportFinishAfterTransition();

                                } else {
                                    // Si el usuario no existe en firestore (aunque, esto no puede pasar xd):
                                    cerrarDialogoProgreso();
                                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    supportFinishAfterTransition();
                                }
                            }).addOnFailureListener(e -> {
                                cerrarDialogoProgreso();
                                Snackbar.make(this, binding.getRoot(), "El usuario no existe en base de datos!", Snackbar.LENGTH_LONG).show();
                            });

                        }
                    } else {
                        cerrarDialogoProgreso();
                        mostrarDialogRegistro(this, email);
                    }
                });
    }

    // -> Validación de campos:

    private void modificarBtnIngresar(boolean validacion){
        binding.btnIngresar.setEnabled(validacion);
        if(validacion){
            binding.btnIngresar.setAlpha(1f);
        }else{
            binding.btnIngresar.setAlpha(0.5f);
        }
    }

    private void validarCampoEmail() {
        TextInputEditText emailEditText = binding.fieldEmail;
        Pattern patron = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                // Verificamos si el campo está vacío
                if (email.isEmpty()) {
                    binding.fieldEmailLayout.setError("El campo de correo no puede estar vacío!");
                    modificarBtnIngresar(false);
                }
                // verificamos el formato
                else if (!patron.matcher(email).matches()) {
                    binding.fieldEmailLayout.setError("Ingresa un correo electrónico válido!");
                    modificarBtnIngresar(false);
                } else {
                    // Aqui el correo es válido asi q borramos cualquier mensaje de error
                    binding.fieldEmailLayout.setError(null);
                    modificarBtnIngresar(true);

                }
            }
        });
    }

    private void validarCampoPassword() {
        TextInputEditText passwordEditText = binding.fieldPassword;
        Pattern patron = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString().trim();
                // Verificamos si el campo está vacío
                if (password.isEmpty()) {
                    binding.fieldPasswordLayout.setError("El campo de contraseña no puede estar vacío!");
                    modificarBtnIngresar(false);
                }
                // Verificamos el formato (mínimo 8 caracteres, al menos una letra y un número)
                else if (!patron.matcher(password).matches()) {
                    binding.fieldPasswordLayout.setError("La contraseña debe tener al menos 8 caracteres, incluyendo números y letras!");
                    modificarBtnIngresar(false);
                } else {
                    // La contraseña es válida, eliminamos el mensaje de error
                    binding.fieldPasswordLayout.setError(null);
                    modificarBtnIngresar(true);
                }
            }
        });
    }

    // -> Métodos para dialogs:

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

    public void mostrarDialogRegistro(Context context, String email){
        new MaterialAlertDialogBuilder(context)
                .setTitle("Correo electrónico no encontrado!")
                .setMessage("Desea proceder con el proceso de registro utilizando el correo '"+email+"' ?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                })
                .setNegativeButton("No",(dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }


}