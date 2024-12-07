# 🚌 SúbeteYA
![Android App](https://img.shields.io/badge/LABORATORIO-IOT-CEBDFF?style=flat-square&logo=android)

<div align="center">
  <img src="https://drive.google.com/uc?export=view&id=1QyZHblsS_049Zz3NzQI5tolzPp0OoeJI" alt="Logo de SúbeteYA" width="300"/>
</div>

---

## Descripción

**SúbeteYA** es una aplicación simple de Android desarrollada con el fin de facilitar la compra de tickets de buses mediante escaneo de códigos QR y el consumo de saldo de los usuarios. La app implementa funcionalidades específicas para dos roles: **Usuario Operativo** y **Empresa de Transporte**, aprovechando servicios del BaaS de **Firebase** para la autenticación, almacenamiento y base de datos.

Con **SúbeteYA**, los usuarios pueden consultar detalles de líneas de buses, suscribirse a servicios mensuales, gestionar su saldo, y obtener cashback por viajes cortos. Las empresas de transporte, por su parte, tienen herramientas para administrar sus líneas y ver los montos recaudados.

---

## Menú de la Aplicación:

<div align="center">
  <img src="https://drive.google.com/uc?export=view&id=1EEURe42Zj9XWuLWpSUpKnpzIfJcdHl0V" alt="Pantalla Menú" width="250"/>
</div>

---

## Funcionalidades principales

### 🚶 Rol Usuario Operativo
- **Inicio de sesión**:
  - Autenticación con **correo y contraseña** utilizando Firebase Authentication.
  - Registro de nuevos usuarios operativos con validación de datos personales, correo y contraseñas (mínimo 8 caracteres, números y letras).
- **Gestión de líneas de bus**:
  - Listado de líneas con imágenes y detalles como precio unitario del pasaje y suscripción mensual.
  - Carrusel de imágenes del bus en los detalles.
- **Suscripción a líneas de bus**:
  - Confirma la suscripción mensual y descuenta el saldo del usuario.
  - Mensaje de alerta si el saldo es insuficiente.
- **Escaneo de QR**:
  - Escanea el QR para ingresar al bus y descontar el saldo automáticamente (si no tiene suscripción activa).
  - Al salir del bus, escanea nuevamente para recibir un cashback:
    - **20%** para viajes menores a 15 minutos.
    - **5%** para viajes mayores a 15 minutos.
- **Historial de viajes**:
  - Visualiza la línea de bus utilizada, fecha, duración, precio original y el monto final tras cashback o suscripción.

### 🏢 Rol Empresa de Transporte
- **Gestión de líneas de bus**:
  - Listado de líneas con imágenes y el monto recaudado en el mes.
  - Botón de edición para gestionar las imágenes del carrusel.
  - Agregar y eliminar imágenes en el carrusel.
  - Subida de imágenes desde cámara o galería del dispositivo.

---

## Interfaz de la aplicación

<div align="center">
  <img src="https://drive.google.com/uc?export=view&id=1EH-nBuEyEJBiWlGeUoK0a2YiG1Kyv9Fc" alt="Pantalla 1" width="250"/>
  <img src="https://drive.google.com/uc?export=view&id=1ELyZcqjOs8XBJYb_InHq3Ri3z_FvodLd" alt="Pantalla 2" width="250"/>
  <img src="https://drive.google.com/uc?export=view&id=1EPS5OyCJVd-FaUeOVjhlmSLSoCzFfY5e" alt="Pantalla 3" width="250"/>
</div>

<div align="center">
  <img src="https://drive.google.com/uc?export=view&id=1ERraQjIKgcUi3lV9GZBwQkcg1cIQnaCq" alt="Pantalla 4" width="250"/>
  <img src="https://drive.google.com/uc?export=view&id=1EbGxpVj35c57M3WzaSSHQSb7vSkzha_a" alt="Pantalla 5" width="250"/>
  <img src="https://drive.google.com/uc?export=view&id=1Ef7q-AocVs1Xg9QdUsnb0PIiH46TuHeU" alt="Pantalla 6" width="250"/>
</div>

<div align="center">
  <img src="https://drive.google.com/uc?export=view&id=1EjwC2I9q_wkTJEI6XZzpX0xY0pj1kGcN" alt="Pantalla 7" width="250"/>
  <img src="https://drive.google.com/uc?export=view&id=1EnDqOXA0qlKABJ8XJ5BkEw_BPJH78v3y" alt="Pantalla 8" width="250"/>
  <img src="https://drive.google.com/uc?export=view&id=1Eo38exkNJFANTmiFsj799HaVlwye9OB4" alt="Pantalla 9" width="250"/>
</div>

<div align="center">
  <img src="https://drive.google.com/uc?export=view&id=1Ep9FKSIS1ExV9HgKHQJhOn1CVTf2F3iG" alt="Pantalla 10" width="250"/>
  <img src="https://drive.google.com/uc?export=view&id=1Et6TtV995fKkgBjJJ-keoI341HkP9DqI" alt="Pantalla 11" width="250"/>
  <img src="https://drive.google.com/uc?export=view&id=1EtgqXAPb-RUy8G7o4DP46MbiFERGh8ah" alt="Pantalla 12" width="250"/>
</div>

<div align="center">
  <img src="https://drive.google.com/uc?export=view&id=1EzORPVnMSirDNl6yO4Z7RBdCp_up1GzQ" alt="Pantalla 13" width="250"/>
  <img src="https://drive.google.com/uc?export=view&id=1EztRQzCDDkZxyjaq7KZcUEu6uYq4adl6" alt="Pantalla 14" width="250"/>
  <img src="https://drive.google.com/uc?export=view&id=1F2BAJZ2bzNe_XpXi0wX8Y_Uz2W3EsCZM" alt="Pantalla 15" width="250"/>
</div>


---

## 🛠️ Tecnologías utilizadas

- **Android Studio**: IDE de desarrollo.
- **Java**: Lenguaje de programación.
- **Firebase Authentication**: Para autenticación y registro de usuarios.
- **Firebase Firestore**: Base de datos en tiempo real para usuarios, líneas de buses y viajes.
- **Firebase Storage**: Almacenamiento de imágenes para los carruseles.
- **ZXing**: Librería de Android para escaneo de códigos QR.

---

## 📋 Instrucciones de instalación

1. Clona este repo en tu máquina local.
2. Abre el proyecto en Android Studio.
3. Firebase es configurado con el documento "google-services.json" contenido en este repositorio (no utilizar este archivo con malas intenciones :s).
4. Corre la aplicación en un celular Android o en el emulador del propio IDE.
5. En caso desee usar el inicio de sesión con Facebook, utilice la cuenta de prueba colocada en el archivo de texto "Repositorio_Lab6_20211755"
6. No busque bugs ni errores en la app, confíe en que todo lo solicitado en el enunciado del laboratorio funciona :D

---

## 🔍 Notas adicionales

- Los usuarios operativos comienzan con un saldo inicial de **50 soles**.
- El tiempo umbral para realizar un cashback de mayor porcentaje está definido en **1 minuto** para fines de pruebas.
- El monto recaudado para el rol de empresa se calcula automáticamente según los viajes realizados por cada usuario tanto al iniciar el viaje como al terminarlo (considerando el descuento por el cashback).
- Los QR de las líneas de buses se pueden visualizar desde el usuario con rol de Empresa de Transporte.


---
