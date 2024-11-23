package com.example.subeteya.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.subeteya.DTOs.UsuarioDTO;
import com.example.subeteya.R;
import com.example.subeteya.databinding.ActivityLoginBinding;
import com.example.subeteya.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    ActivityRegisterBinding binding;
    FirebaseFirestore db;
    FirebaseAuth auth;
    AlertDialog progresoDialog;
    UsuarioDTO usuarioDTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase:
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Binding:
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Gestión de intent (correo electrónico):
        Intent intent = getIntent();
        if(intent.getStringExtra("email")!=null){
            binding.fieldCorreo.setText(intent.getStringExtra("email"));
        }

        // ------------
        //   Lógica:
        // ------------

        // -> Configuramos las validaciones de los campos uwu
        modificarBtnRegistrar(false);
        configurarValidaciones();

        // -> Registro de Usuario Operativo:
        binding.btnRegistrar.setOnClickListener(v -> {
           registrarUsuarioFirebase();
        });
    }

    // -------------------
    //      MÉTODOS:
    // -------------------

    // -> Registro:

    private void registrarUsuarioFirebase(){

        mostrarDialogoProgreso();

        // Verificación de correo existente:

        String correo = binding.fieldCorreo.getText().toString().trim();
        verificarCorreoUnico(correo,task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {

                // Seteo de UsuarioDTO:

                usuarioDTO = new UsuarioDTO();
                usuarioDTO.setNombre(binding.fieldNombre.getText().toString().trim());
                usuarioDTO.setApellido(binding.fieldApellido.getText().toString().trim());
                usuarioDTO.setCorreo(correo);
                usuarioDTO.setRol("Usuario Operativo");
                usuarioDTO.setSaldo(50.0);

                // Sign up en FirebaseAuth:

                String password = binding.fieldPassword.getText().toString().trim();

                auth.createUserWithEmailAndPassword(correo,password)
                        .addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()){

                                // Registro en Firestore:

                                db.collection("usuario")
                                        .document(auth.getCurrentUser().getUid())
                                        .set(usuarioDTO)
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                Intent intent = new Intent(RegisterActivity.this, UsuarioOperativoActivity.class);
                                                intent.putExtra("mensaje","Registro e inicio de sesión exitoso!");
                                                startActivity(intent);
                                                supportFinishAfterTransition();
                                            }else{
                                                cerrarDialogoProgreso();
                                                Snackbar.make(this, binding.getRoot(), "Ocurrió un error al registrar al usuario :(!", Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }else{
                                cerrarDialogoProgreso();
                                Snackbar.make(this, binding.getRoot(), "Ocurrió un error al registrar al usuario :c!", Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }else{
                cerrarDialogoProgreso();
                mostrarDialogCorreoUnico();
            }
        });
    }


    // -> Validación de campos:

    private void modificarBtnRegistrar(boolean validacion){
        binding.btnRegistrar.setEnabled(validacion);
        if(validacion){
            binding.btnRegistrar.setAlpha(1f);
        }else{
            binding.btnRegistrar.setAlpha(0.5f);
        }
    }

    private void verificarCorreoUnico(String correo, OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuario")
                .whereEqualTo("correo", correo)
                .get()
                .addOnCompleteListener(listener);
    }

    private void configurarValidaciones(){
        binding.fieldNombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().isEmpty()) {
                    binding.fieldNombreLayout.setError("El nombre no puede estar vacío");
                    modificarBtnRegistrar(false);
                } else {
                    binding.fieldNombreLayout.setError(null);
                    modificarBtnRegistrar(validarTodosLosCampos());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        binding.fieldApellido.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().isEmpty()) {
                    binding.fieldApellidoLayout.setError("El apellido no puede estar vacío");
                    modificarBtnRegistrar(false);
                } else {
                    binding.fieldApellidoLayout.setError(null);
                    modificarBtnRegistrar(validarTodosLosCampos());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        TextInputEditText emailEditText = binding.fieldCorreo;
        Pattern patron = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                // Verificamoss si el campo está vacío
                if (email.isEmpty()) {
                    binding.fieldCorreoLayout.setError("El campo de correo no puede estar vacío!");
                    modificarBtnRegistrar(false);
                }
                // verificamos el formato
                else if (!patron.matcher(email).matches()) {
                    binding.fieldCorreoLayout.setError("Ingresa un correo electrónico válido!");
                    modificarBtnRegistrar(false);

                } else {
                    // Aqui el correo es válido asi q borramos cualquier mensaje de error
                    binding.fieldCorreoLayout.setError(null);
                    modificarBtnRegistrar(validarTodosLosCampos());
                    Log.d("TAG", "validacioncampos: "+validarTodosLosCampos());
                }
            }
        });

        TextInputEditText passwordEditText = binding.fieldPassword;
        Pattern patron2 = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
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
                    modificarBtnRegistrar(false);
                }
                // Verificamos el formato (mínimo 8 caracteres, al menos una letra y un número)
                else if (!patron2.matcher(password).matches()) {
                    binding.fieldPasswordLayout.setError("La contraseña debe tener al menos 8 caracteres, incluyendo números y letras!");
                    modificarBtnRegistrar(false);
                } else {
                    if(password.equals(binding.fieldPassword2.getText().toString())){
                        // La contraseña es válida, eliminamos el mensaje de error
                        binding.fieldPasswordLayout.setError(null);
                        binding.fieldPassword2Layout.setError(null);
                        modificarBtnRegistrar(true);
                    }else{
                        binding.fieldPasswordLayout.setError("Las contraseñas no coinciden!");
                        binding.fieldPassword2Layout.setError("Las contraseñas no coinciden!");
                        modificarBtnRegistrar(false);
                    }

                }
            }
        });

        TextInputEditText password2EditText = binding.fieldPassword2;
        password2EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String password2 = s.toString().trim();
                // Verificamos si el campo está vacío
                if (password2.isEmpty()) {
                    binding.fieldPassword2Layout.setError("El campo de repetición de contraseña no puede estar vacío!");
                    modificarBtnRegistrar(false);
                }
                // Verificamos que ambas sean iguales
                else if (!password2.equals(binding.fieldPassword.getText().toString())) {
                    binding.fieldPassword2Layout.setError("Las contraseñas no coinciden!");
                    modificarBtnRegistrar(false);
                } else {
                    // La contraseña es válida, eliminamos el mensaje de error
                    binding.fieldPasswordLayout.setError(null);
                    binding.fieldPassword2Layout.setError(null);
                    if(validarTodosLosCampos()){
                        modificarBtnRegistrar(true);
                    }else{
                        modificarBtnRegistrar(false);
                    }

                }
            }
        });
    }

    private boolean validarTodosLosCampos(){
        return !binding.fieldNombre.getText().toString().isEmpty() &&
                !binding.fieldApellido.getText().toString().isEmpty() &&
                !binding.fieldCorreo.getText().toString().isEmpty() &&
                !binding.fieldPassword.getText().toString().isEmpty() &&
                !binding.fieldPassword2.getText().toString().isEmpty() &&
                validarCampoEmail() &&
                validarCamposPassword();
    }

    private boolean validarCampoEmail() {
        TextInputEditText emailEditText = binding.fieldCorreo;
        Pattern patron = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
        if (patron.matcher(emailEditText.getText().toString()).matches()) {
            return true;
        }else{
            return false;
        }
    }

    private boolean validarCamposPassword() {
        String password = binding.fieldPassword.getText().toString();
        Pattern patron = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
        if(patron.matcher(password).matches()){
            if(password.equals(binding.fieldPassword2.getText().toString())){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
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

    private void mostrarDialogCorreoUnico(){
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error de campo único!")
                .setMessage("El correo electrónico ingresado ya existe en la plataforma!")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}