# Sistema de Gestión de Inventario - Banco de Alimentos MJ²

## a. Datos del Proyecto
* **Título del Proyecto:** Banco de Alimentos MJ²
* **ODS (Objetivo de Desarrollo Sostenible):** ODS 2 - Hambre Cero.
* **Integrantes y Roles:**
  * **Marianna Lizarazo** – Coordinadora / Desarrolladora de Estructura.
  * **Juan Pablo Barraza** – Desarrollador de Estructura / Investigador y Documentador.
  * **Jackelyn Hernandez** – Desarrolladora de Aplicación (Interfaz Gráfica / GUI Swing).

---

## b. Descripción
Este prototipo resuelve la problemática del control ineficiente y la merma de alimentos en bancos de alimentos comunitarios.
Automatiza el control de existencias (actual, mínimo y máximo) mediante una estructura de datos en memoria optimizada (Árbol AVL) sincronizada en tiempo real 
con una base de datos relacional MySQL, incorporando además modelos de cálculo integral numérico y analítico para la auditoría de donaciones y predicción 
de consumo.

---

## c. Instrucciones de Ejecución

### Requisitos previos
* **Java Development Kit (JDK):** Versión 17 o superior.
* **Gestor de Base de Datos:** XAMPP (Servicios de Apache y MySQL activos).
* **Driver JDBC:** Conector de MySQL para Java (`mysql-connector-j-...jar`).

### Pasos para configurar la Base de Datos
1. Inicia **XAMPP** y asegúrate de que MySQL y Apache estén corriendo.
2. Ingresa a **phpMyAdmin** (`http://localhost/phpmyadmin/`).
3. Dirígete a la pestaña **SQL**, copia el script provisto en la carpeta `sql/` de este repositorio, pégalo en el recuadro y
    haz clic en **Continuar** para crear la base de datos `banco_alimentos_db` y cargar los 5 registros iniciales de prueba
    (comenzando con el arroz en kg y el atún en unidades).

### Configuración de la conexión y Ejecución
1. Clona o descarga este repositorio en tu equipo.
2. Abre el proyecto en tu entorno de desarrollo (ej. Apache NetBeans).
3. Añade el archivo JAR del conector de MySQL a las **Libraries** (Librerías) del proyecto.
4. Verifica que las credenciales de conexión en la clase principal coincidan con tu entorno local:
   * URL: `jdbc:mysql://localhost:3306/banco_alimentos_db?useSSL=false&serverTimezone=UTC`
   * Usuario: `root`
   * Contraseña: `` (vacía por defecto en XAMPP)
5. Ejecuta la clase principal (`ArbolAVLInventario.java`) para desplegar la interfaz gráfica de usuario.

---

## d. Estructuras Implementadas

| Estructura de Datos | Clase que la Implementa | Entidad / Tabla que Gestiona | Descripción de Uso |
| :--- | :--- | :--- | :--- |
| **Árbol AVL** | `ArbolAVLInventario.java` (Clase `NodoAVL`) | `alimentos` | Mantiene los productos ordenados alfabéticamente por su 
código con auto-balanceo para búsquedas y recorridos rápidos. |
| **Persistencia JDBC** | `DriverManager` / `PreparedStatement` | `alimentos` | Sincroniza en tiempo real las operaciones CRUD de la memoria RAM 
hacia la base de datos MySQL. |

---

## e. Estado Actual
* **Funcionalidades Operativas:** 
  * Operaciones completas (inserción, búsqueda, modificación, eliminación y recorrido In-Orden), contemplando manejo de estructuras vacías o elementos inexistentes.
  * Carga automática de registros desde MySQL al iniciar el programa.
  * Persistencia garantizada (las modificaciones reflejan cambios permanentes en la base de datos).
  * Interfaz gráfica en Swing con selección de categorías mediante `JComboBox` y alertas visuales de stock crítico.
* **Fallas Conocidas / Pendientes:** 
  * Ninguna reportada; prototipo completamente estable para su demostración en laboratorio y defensa en la Feria Innova.
