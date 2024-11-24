package com.example.subeteya.Viewmodels;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.subeteya.Beans.LineaBus;
import com.example.subeteya.Beans.Usuario;
import com.example.subeteya.Beans.Viaje;
import com.example.subeteya.DTOs.LineaBusDTO;
import com.example.subeteya.DTOs.UsuarioDTO;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FirebaseViewModel extends ViewModel {

    // -------------------
    //      ATRIBUTOS:
    // -------------------
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final MutableLiveData<Usuario> usuarioActual = new MutableLiveData<>();
    private final MutableLiveData<List<LineaBus>> listaLineasBuses = new MutableLiveData<>();
    private final MutableLiveData<LineaBus> lineaBusActual = new MutableLiveData<>();
    private final MutableLiveData<List<LineaBus>> listaLineasBusesEmpresa = new MutableLiveData<>();
    private final MutableLiveData<List<Viaje>> listaViajes = new MutableLiveData<>();


    public FirebaseViewModel() {
        firestore = FirebaseFirestore.getInstance();
        storage=FirebaseStorage.getInstance();
    }

    // -------------------
    //      MÉTODOS:
    // -------------------

    // -> Getters de Livedata:

    public MutableLiveData<Usuario> getUsuarioActual() {
        return usuarioActual;
    }

    public MutableLiveData<List<LineaBus>> getlistaLineasBuses() {
        return listaLineasBuses;
    }

    public MutableLiveData<LineaBus> getLineaBusActual() {
        return lineaBusActual;
    }

    public MutableLiveData<List<LineaBus>> getListaLineasBusesEmpresa() {
        return listaLineasBusesEmpresa;
    }

    public MutableLiveData<List<Viaje>> getListaViajes() {
        return this.listaViajes;
    }


    // -> Métodos para usuario:

    public void obtenerUsuarioActual(String uid){
        firestore.collection("usuario").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UsuarioDTO usuarioDTO = documentSnapshot.toObject(UsuarioDTO.class);
                        mapearUsuarioDtoABean(usuarioDTO, documentSnapshot.getId()).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Usuario usuario = task.getResult();
                                usuarioActual.setValue(usuario);
                                Log.d("Firestore", "Usuario mapeado correctamente: " + usuario);
                            } else {
                                Log.d("Firestore", "Error al mapear usuario", task.getException());
                            }
                        });

                    }
                });
    }


    public void recargarUsuarioActual(String uid){
        firestore.collection("usuario").document(uid).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                return;
            }
            if (documentSnapshot.exists()) {
                UsuarioDTO usuarioDTO = documentSnapshot.toObject(UsuarioDTO.class);
                mapearUsuarioDtoABean(usuarioDTO, documentSnapshot.getId()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Usuario usuario = task.getResult();
                        usuarioActual.setValue(usuario);
                        Log.d("Firestore", "Usuario mapeado correctamente: " + usuario);
                    } else {
                        Log.d("Firestore", "Error al mapear usuario", task.getException());
                    }
                });
            }
        });
    }


    // -> Métodos para línea de bus:

    public void obtenerLineaBusActual(String uid){
        firestore.collection("lineaBus").document(uid).get()
                .addOnCompleteListener(documentSnapshot -> {
                    if (documentSnapshot.isSuccessful() && documentSnapshot.getResult().exists()) {
                        LineaBusDTO lineaBusDTO = documentSnapshot.getResult().toObject(LineaBusDTO.class);
                        mapearLineaBusDtoABean(lineaBusDTO, documentSnapshot.getResult().getId()).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                LineaBus lineaBus = task.getResult();
                                lineaBusActual.setValue(lineaBus);
                                Log.d("Firestore", "LineaBus mapeado correctamente");
                            } else {
                                Log.e("Firestore", "Error al mapear LineaBus", task.getException());
                            }
                        });
                    }
                });
    }

    public void recargarLineaBusActual(String uid){
        firestore.collection("lineaBus").document(uid).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                return;
            }
            if (documentSnapshot.exists()) {
                LineaBusDTO lineaBusDTO = documentSnapshot.toObject(LineaBusDTO.class);
                mapearLineaBusDtoABean(lineaBusDTO, documentSnapshot.getId()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        LineaBus lineaBus = task.getResult();
                        lineaBusActual.setValue(lineaBus);
                        Log.d("Firestore", "LineaBus mapeado correctamente");
                    } else {
                        Log.e("Firestore", "Error al mapear LineaBus", task.getException());
                    }
                });
            }
        });
    }

    public Task<Boolean> validarDocumentoPorUid(String uid) {
        DocumentReference documentRef = firestore.collection("lineaBus").document(uid);
        return documentRef.get().continueWith(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                return document != null && document.exists();
            } else {
                return false;
            }
        });
    }

    // -> Métodos para lista de líneas de buses que pertenecen a una empresa:

    public void obtenerListaLineasBusesEmpresa(String empresaUid) {
        firestore.collection("lineaBus")
                .whereEqualTo("refEmpresa",firestore.collection("usuario").document(empresaUid))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<Task<LineaBus>> tasks = new ArrayList<>(); // Lista para rastrear las tareas
                        List<LineaBus> listaLineasBuses = new ArrayList<>(); // Lista temporal para las LineaBus

                        for (DocumentSnapshot ds : querySnapshot.getDocuments()) {
                            LineaBusDTO lineaBusDTO = ds.toObject(LineaBusDTO.class);
                            Task<LineaBus> mapearTask = mapearLineaBusDtoABean(lineaBusDTO, ds.getId());

                            // Añadir tarea a la lista y manejar el resultado
                            tasks.add(mapearTask.continueWith(task -> {
                                if (task.isSuccessful()) {
                                    LineaBus lineaBus = task.getResult();
                                    listaLineasBuses.add(lineaBus); // Agregar el resultado a la lista temporal
                                } else {
                                    Log.e("Firestore", "Error al mapear LineaBus", task.getException());
                                }
                                return null; // No necesitamos devolver nada útil aquí
                            }));
                        }

                        // Esperar a que todas las tareas terminen antes de actualizar la lista principal
                        Tasks.whenAll(tasks).addOnCompleteListener(allTasks -> {
                            if (allTasks.isSuccessful()) {
                                this.listaLineasBusesEmpresa.setValue(listaLineasBuses);
                                Log.d("Firestore", "Lista de LineaBus obtenida correctamente");
                            } else {
                                Log.e("Firestore", "Error al completar tareas de mapeo", allTasks.getException());
                            }
                        });
                    } else {
                        Log.d("Firestore", "No se encontraron documentos en lineaBus");
                        this.listaLineasBusesEmpresa.setValue(new ArrayList<>()); // Lista vacía si no hay documentos
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener la colección lineaBus", e);
                    this.listaLineasBusesEmpresa.setValue(new ArrayList<>()); // Lista vacía en caso de error
                });
    }

    public void recargarListaLineasBusesEmpresa(String empresaUid) {
        firestore.collection("lineaBus").addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Errorcito al escuchar cambios de la lista de buss", e);
                return;
            }

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                List<Task<LineaBus>> tasks = new ArrayList<>(); // Lista para rastrear tareas
                List<LineaBus> listaLineasBuses = new ArrayList<>(); // Lista temporal para las LineaBus

                for (DocumentSnapshot ds : querySnapshot.getDocuments()) {
                    LineaBusDTO lineaBusDTO = ds.toObject(LineaBusDTO.class);

                    if(lineaBusDTO.getRefEmpresa().equals(firestore.collection("usuario").document(empresaUid))){
                        Task<LineaBus> mapearTask = mapearLineaBusDtoABean(lineaBusDTO, ds.getId());

                        // Añadimos la tarea a la lista y manejamos el resultado
                        tasks.add(mapearTask.continueWith(task -> {
                            if (task.isSuccessful()) {
                                LineaBus lineaBus = task.getResult();
                                listaLineasBuses.add(lineaBus);
                            } else {
                                Log.e("Firestore", "Error al mapear LineaBus", task.getException());
                            }
                            return null;
                        }));
                    }
                }

                Tasks.whenAll(tasks).addOnCompleteListener(allTasks -> {
                    if (allTasks.isSuccessful()) {
                        this.listaLineasBusesEmpresa.setValue(listaLineasBuses);
                        Log.d("Firestore", "Lista de LineaBus actualizada correctamente");
                    } else {
                        Log.e("Firestore", "Error al completar tareas de mapeo", allTasks.getException());
                    }
                });
            } else {
                Log.d("Firestore", "No se encontraron documentos en lineaBus");
                this.listaLineasBusesEmpresa.setValue(new ArrayList<>());
            }
        });
    }



    // -> Métodos para lista de líneas de buses:

    public void obtenerListaLineasBuses() {
        firestore.collection("lineaBus")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<Task<LineaBus>> tasks = new ArrayList<>(); // Lista para rastrear las tareas
                        List<LineaBus> listaLineasBuses = new ArrayList<>(); // Lista temporal para las LineaBus

                        for (DocumentSnapshot ds : querySnapshot.getDocuments()) {
                            LineaBusDTO lineaBusDTO = ds.toObject(LineaBusDTO.class);
                            Task<LineaBus> mapearTask = mapearLineaBusDtoABean(lineaBusDTO, ds.getId());

                            // Añadir tarea a la lista y manejar el resultado
                            tasks.add(mapearTask.continueWith(task -> {
                                if (task.isSuccessful()) {
                                    LineaBus lineaBus = task.getResult();
                                    listaLineasBuses.add(lineaBus); // Agregar el resultado a la lista temporal
                                } else {
                                    Log.e("Firestore", "Error al mapear LineaBus", task.getException());
                                }
                                return null; // No necesitamos devolver nada útil aquí
                            }));
                        }

                        // Esperar a que todas las tareas terminen antes de actualizar la lista principal
                        Tasks.whenAll(tasks).addOnCompleteListener(allTasks -> {
                            if (allTasks.isSuccessful()) {
                                this.listaLineasBuses.setValue(listaLineasBuses);
                                Log.d("Firestore", "Lista de LineaBus obtenida correctamente");
                            } else {
                                Log.e("Firestore", "Error al completar tareas de mapeo", allTasks.getException());
                            }
                        });
                    } else {
                        Log.d("Firestore", "No se encontraron documentos en lineaBus");
                        this.listaLineasBuses.setValue(new ArrayList<>()); // Lista vacía si no hay documentos
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener la colección lineaBus", e);
                    this.listaLineasBuses.setValue(new ArrayList<>()); // Lista vacía en caso de error
                });
    }

    public void recargarListaLineasBuses() {
        firestore.collection("lineaBus").addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore", "Errorcito al escuchar cambios de la lista de buss", e);
                return;
            }

            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                List<Task<LineaBus>> tasks = new ArrayList<>(); // Lista para rastrear tareas
                List<LineaBus> listaLineasBuses = new ArrayList<>(); // Lista temporal para las LineaBus

                for (DocumentSnapshot ds : querySnapshot.getDocuments()) {
                    LineaBusDTO lineaBusDTO = ds.toObject(LineaBusDTO.class);
                    Task<LineaBus> mapearTask = mapearLineaBusDtoABean(lineaBusDTO, ds.getId());

                    // Añadimos la tarea a la lista y manejamos el resultado
                    tasks.add(mapearTask.continueWith(task -> {
                        if (task.isSuccessful()) {
                            LineaBus lineaBus = task.getResult();
                            listaLineasBuses.add(lineaBus);
                        } else {
                            Log.e("Firestore", "Error al mapear LineaBus", task.getException());
                        }
                        return null;
                    }));
                }

                Tasks.whenAll(tasks).addOnCompleteListener(allTasks -> {
                    if (allTasks.isSuccessful()) {
                        this.listaLineasBuses.setValue(listaLineasBuses);
                        Log.d("Firestore", "Lista de LineaBus actualizada correctamente");
                    } else {
                        Log.e("Firestore", "Error al completar tareas de mapeo", allTasks.getException());
                    }
                });
            } else {
                Log.d("Firestore", "No se encontraron documentos en lineaBus");
                this.listaLineasBuses.setValue(new ArrayList<>());
            }
        });
    }

    // -> Métodos para mapeo DTO a Bean:

    public Task<Usuario> mapearUsuarioDtoABean(UsuarioDTO usuarioDTO, String uid) {
        Usuario usuario = new Usuario();

        // Atributos normales:
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setCorreo(usuarioDTO.getCorreo());
        usuario.setRol(usuarioDTO.getRol());
        usuario.setSaldo(usuarioDTO.getSaldo());
        usuario.setFechaInicio(usuarioDTO.getFechaInicio());
        usuario.setFechaFin(usuarioDTO.getFechaFin());
        usuario.setUid(uid);
        usuario.setNombreEmpresa(usuarioDTO.getNombreEmpresa());
        usuario.setRecaudacion(usuarioDTO.getRecaudacion());

        List<Task<?>> allTasks = new ArrayList<>(); // Todas las tareas asíncronas

        // Mapeo del bus de viaje actual:

        Task<LineaBus> lineaBusTask = Tasks.forResult(null);
         // Inicializamos un Task vacío
        if (usuarioDTO.getRefLineaBus() != null) {
            lineaBusTask = usuarioDTO.getRefLineaBus().get()
                    .continueWithTask(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            LineaBusDTO lineaBusDTO = task.getResult().toObject(LineaBusDTO.class);
                            return mapearLineaBusDtoABean(lineaBusDTO, task.getResult().getId());
                        } else {
                            return Tasks.forResult(null);
                        }
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            usuario.setLineaBus(task.getResult());
                        } else {
                            usuario.setLineaBus(null);
                        }
                    });
            allTasks.add(lineaBusTask);
        } else {
            usuario.setLineaBus(null);
        }

        // Mapeo de buses de suscripción:
        Task<List<LineaBus>> lineaBusSuscripcionTask = Tasks.forResult(new ArrayList<>());
        if (usuarioDTO.getRefLineaBusSuscripcion() != null) {
            List<Task<LineaBus>> suscripcionTasks = new ArrayList<>();
            for (DocumentReference refLineaBusDTO : usuarioDTO.getRefLineaBusSuscripcion()) {
                Task<LineaBus> lineaBusTaskAlt = refLineaBusDTO.get()
                        .continueWithTask(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                LineaBusDTO lineaBusDTO = task.getResult().toObject(LineaBusDTO.class);
                                return mapearLineaBusDtoABean(lineaBusDTO, task.getResult().getId());
                            } else {
                                return Tasks.forResult(null);
                            }
                        });
                suscripcionTasks.add(lineaBusTaskAlt);
            }

            // Cuando todas las tareas de suscripción terminen:
            lineaBusSuscripcionTask = Tasks.whenAllComplete(suscripcionTasks).continueWith(task -> {
                List<LineaBus> listaLineasBusesSuscripcion = new ArrayList<>();
                for (Task<LineaBus> lineaBusTaskAlt2 : suscripcionTasks) {
                    if (lineaBusTaskAlt2.isSuccessful() && lineaBusTaskAlt2.getResult() != null) {
                        listaLineasBusesSuscripcion.add(lineaBusTaskAlt2.getResult());
                    }
                }
                return listaLineasBusesSuscripcion;
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    usuario.setLineaBusSuscripcion(task.getResult());
                } else {
                    usuario.setLineaBusSuscripcion(new ArrayList<>());
                }
            });
            allTasks.add(lineaBusSuscripcionTask);
        } else {
            usuario.setLineaBusSuscripcion(new ArrayList<>());
        }

        // Esperamos a que todas las tareas se completen:
        return Tasks.whenAllComplete(allTasks).continueWith(task -> {
            // Retornamos el usuario completamente mapeado.
            return usuario;
        });
    }



    public Task<LineaBus> mapearLineaBusDtoABean(LineaBusDTO lineaBusDTO, String uid) {
        LineaBus lineaBus = new LineaBus();

        // Atributos normales:
        lineaBus.setNombre(lineaBusDTO.getNombre());
        lineaBus.setPrecioUnitario(lineaBusDTO.getPrecioUnitario());
        lineaBus.setPrecioSuscripcion(lineaBusDTO.getPrecioSuscripcion());
        lineaBus.setRecaudacion(lineaBusDTO.getRecaudacion());
        lineaBus.setRutasCarrusel(lineaBusDTO.getRutasCarrusel());
        lineaBus.setUid(uid);

        // Creamos una tarea para obtener la empresa de transporte:
        Task<DocumentSnapshot> empresaTask = lineaBusDTO.getRefEmpresa().get();

        return empresaTask.continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                UsuarioDTO empresaDTO = task.getResult().toObject(UsuarioDTO.class);

                Usuario empresa = new Usuario();
                empresa.setNombreEmpresa(empresaDTO.getNombreEmpresa());
                empresa.setCorreo(empresaDTO.getCorreo());
                empresa.setRol(empresaDTO.getRol());
                empresa.setUid(task.getResult().getId());
                empresa.setRecaudacion(empresaDTO.getRecaudacion());

                lineaBus.setEmpresa(empresa);
            } else {
                lineaBus.setEmpresa(null);
            }

            return lineaBus;
        });
    }

    // ----------
    //   CRUDS:
    // ----------

    // Métodos para viajes (Actual e historial):

    public void registrarViaje(Viaje viaje){
        firestore.collection("viaje")
                .add(viaje)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d("TAG", "Se pudo registrar el viaje ");

                    }else{
                        Log.d("TAG", "No se pudo registrar el viaje ");
                    }
                });
    }

    public Task<QuerySnapshot> obtenerViaje(String usuarioUid, Timestamp fechaInicio){
        return firestore.collection("viaje")
                .whereEqualTo("usuarioUid",usuarioUid)
                .whereEqualTo("fechaInicio",fechaInicio)
                .get();
    }

    public void actualizarViaje(String viajeUid, HashMap<String,Object> campos){
        firestore.collection("viaje")
                .document(viajeUid)
                .update(campos)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d("TAG", "Se pudo actualizar el viaje ");
                    }else{
                        Log.d("TAG", "No se pudo actualizar el viaje ");
                    }
                });
    }

    public void recargarListaViajes(String usuarioUid){
        firestore.collection("viaje")
                .whereEqualTo("usuarioUid",usuarioUid)
                .orderBy("fechaInicio", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot,e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Errorcito al escuchar cambios de los viajes", e);
                        return;
                    }

                    List<Viaje> listaViajes = new ArrayList<>();

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for(DocumentSnapshot ds: querySnapshot.getDocuments()){
                            Viaje viaje = ds.toObject(Viaje.class);
                            listaViajes.add(viaje);
                        }
                        this.listaViajes.setValue(listaViajes);
                    }else{

                    }
                });
    }

    public void obtenerListaViaje(String usuarioUid){
        firestore.collection("viaje")
                .whereEqualTo("usuarioUid",usuarioUid)
                .orderBy("fechaInicio", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(querySnapshot -> {
                    if(querySnapshot.isSuccessful()){
                        List<Viaje> listaViajes = new ArrayList<>();
                        if (querySnapshot.getResult() != null && !querySnapshot.getResult().isEmpty()) {
                            for(DocumentSnapshot ds: querySnapshot.getResult().getDocuments()){
                                Viaje viaje = ds.toObject(Viaje.class);
                                listaViajes.add(viaje);
                            }
                            this.listaViajes.setValue(listaViajes);
                        }else{

                        }
                    }
                });
    }





}
