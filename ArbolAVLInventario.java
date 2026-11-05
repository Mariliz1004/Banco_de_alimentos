/**
 *
 * @author MSI THIN
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ArbolAVLInventario extends JFrame {

    // --- CLASE PRODUCTO / ALIMENTO ---
    static class Producto {
        String codigo, nombre, categoria;
        int stockActual, stockMinimo, stockMaximo;

        Producto(String codigo, String nombre, int stockActual, int stockMinimo, int stockMaximo, String categoria) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.stockActual = stockActual;
            this.stockMinimo = stockMinimo;
            this.stockMaximo = stockMaximo;
            this.categoria = categoria;
        }

        @Override
        public String toString() {
            String info = String.format("[%s] %s (%s) | Act: %d | Min: %d | Max: %d", 
                    codigo, nombre, categoria, stockActual, stockMinimo, stockMaximo);
            
            if (stockActual <= stockMinimo) {
                info += " --> [⚠️ ALERTA: Stock bajo, se requieren donaciones!]";
            } else if (stockActual >= stockMaximo) {
                info += " --> [🛑 AVISO: Capacidad de bodega al límite!]";
            }
            return info;
        }
    }

    // --- NODO AVL ---
    static class NodoAVL {
        Producto producto;
        NodoAVL izquierdo, derecho;
        int altura;

        NodoAVL(Producto p) {
            producto = p;
            altura = 1;
        }
    }

    private NodoAVL raiz;
    
    // Credenciales de conexión a MySQL
    private final String url = "jdbc:mysql://localhost:3306/banco_alimentos_db?useSSL=false&serverTimezone=UTC";
    private final String user = "root";
    private final String password = "";

    // Componentes de la GUI
    private JTable tablaInventario;
    private DefaultTableModel modeloTabla;
    private JTextField txtCodigo, txtNombre, txtStockActual, txtStockMinimo, txtStockMaximo;
    private JComboBox<String> cmbCategoria;

    // --- CONSTRUCTOR ---
    public ArbolAVLInventario() {
        setTitle("Sistema Banco de Alimentos - Árbol AVL + MySQL + Cálculo Integral");
        setSize(950, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Panel Superior (Formulario)
        JPanel panelFormulario = new JPanel(new GridLayout(3, 4, 8, 8));
        panelFormulario.setBorder(BorderFactory.createTitledBorder(" Gestión de Alimentos en Bodega "));

        panelFormulario.add(new JLabel(" Código (ej. ALI-010):"));
        txtCodigo = new JTextField();
        panelFormulario.add(txtCodigo);

        panelFormulario.add(new JLabel(" Nombre / Unidad (ej. Arroz kg):"));
        txtNombre = new JTextField();
        panelFormulario.add(txtNombre);

        panelFormulario.add(new JLabel(" Stock Actual:"));
        txtStockActual = new JTextField();
        panelFormulario.add(txtStockActual);

        panelFormulario.add(new JLabel(" Stock Mínimo:"));
        txtStockMinimo = new JTextField();
        panelFormulario.add(txtStockMinimo);

        panelFormulario.add(new JLabel(" Stock Máximo:"));
        txtStockMaximo = new JTextField();
        panelFormulario.add(txtStockMaximo);

        panelFormulario.add(new JLabel(" Categoría:"));
        String[] categorias = {
            "Granos y Cereales (kg)", 
            "Proteínas y Conservas (und)", 
            "Abarrotes", 
            "Lácteos", 
            "Frutas y Verduras"
        };
        cmbCategoria = new JComboBox<>(categorias);
        panelFormulario.add(cmbCategoria);

        JButton btnRegistrar = new JButton("Registrar / Guardar");
        panelFormulario.add(btnRegistrar);

        add(panelFormulario, BorderLayout.NORTH);

        // 2. Tabla Central
        modeloTabla = new DefaultTableModel(new Object[]{"Código", "Nombre", "Categoría", "Actual", "Mínimo", "Máximo", "Estado"}, 0);
        tablaInventario = new JTable(modeloTabla);
        add(new JScrollPane(tablaInventario), BorderLayout.CENTER);

        // 3. Panel Inferior (Botones de Acción y Cálculo)
        JPanel panelBotones = new JPanel();
        JButton btnModificar = new JButton("Modificar Stock");
        JButton btnEliminar = new JButton("Eliminar Alimento");
        JButton btnBuscar = new JButton("Buscar Código");
        
        // Botones para las aplicaciones de Cálculo Integral
        JButton btnTrapecio = new JButton("🧮 Simular Donación (Prob 4.2)");
        JButton btnChurn = new JButton("📉 Predicción Consumo (Prob 6.6)");

        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnBuscar);
        panelBotones.add(btnTrapecio);
        panelBotones.add(btnChurn);

        add(panelBotones, BorderLayout.SOUTH);

        // Eventos
        btnRegistrar.addActionListener(e -> registrarAlimento());
        btnModificar.addActionListener(e -> modificarStock());
        btnEliminar.addActionListener(e -> eliminarAlimento());
        btnBuscar.addActionListener(e -> buscarAlimento());
        
        btnTrapecio.addActionListener(e -> calcularDonacionTrapecio());
        btnChurn.addActionListener(e -> calcularDepreciacionExponencial());

        inicializarBaseDeDatosYArbol();
    }

    // --- LÓGICA ÁRBOL AVL ---
    private int altura(NodoAVL n) { return n == null ? 0 : n.altura; }
    private int balance(NodoAVL n) { return n == null ? 0 : altura(n.izquierdo) - altura(n.derecho); }
    private void actualizarAltura(NodoAVL n) { n.altura = 1 + Math.max(altura(n.izquierdo), altura(n.derecho)); }

    private NodoAVL rotarDerecha(NodoAVL y) {
        NodoAVL x = y.izquierdo, t2 = x.derecho;
        x.derecho = y; y.izquierdo = t2;
        actualizarAltura(y); actualizarAltura(x);
        return x;
    }

    private NodoAVL rotarIzquierda(NodoAVL x) {
        NodoAVL y = x.derecho, t2 = y.izquierdo;
        y.izquierdo = x; x.derecho = t2;
        actualizarAltura(x); actualizarAltura(y);
        return y;
    }

    public void insertar(Producto p) { raiz = insertar(raiz, p); }

    private NodoAVL insertar(NodoAVL n, Producto p) {
        if (n == null) return new NodoAVL(p);
        int cmp = p.codigo.compareTo(n.producto.codigo);
        if (cmp < 0) n.izquierdo = insertar(n.izquierdo, p);
        else if (cmp > 0) n.derecho = insertar(n.derecho, p);
        else { n.producto = p; return n; }

        actualizarAltura(n);
        int b = balance(n);

        if (b > 1 && p.codigo.compareTo(n.izquierdo.producto.codigo) < 0) return rotarDerecha(n);
        if (b < -1 && p.codigo.compareTo(n.derecho.producto.codigo) > 0) return rotarIzquierda(n);
        if (b > 1 && p.codigo.compareTo(n.izquierdo.producto.codigo) > 0) {
            n.izquierdo = rotarIzquierda(n.izquierdo); return rotarDerecha(n);
        }
        if (b < -1 && p.codigo.compareTo(n.derecho.producto.codigo) < 0) {
            n.derecho = rotarDerecha(n.derecho); return rotarIzquierda(n);
        }
        return n;
    }

    public Producto buscar(String codigo) {
        NodoAVL actual = raiz;
        while (actual != null) {
            int cmp = codigo.compareTo(actual.producto.codigo);
            if (cmp == 0) return actual.producto;
            actual = cmp < 0 ? actual.izquierdo : actual.derecho;
        }
        return null;
    }

    public void eliminar(String codigo) { raiz = eliminar(raiz, codigo); }

    private NodoAVL eliminar(NodoAVL raiz, String codigo) {
        if (raiz == null) return null;
        int cmp = codigo.compareTo(raiz.producto.codigo);
        if (cmp < 0) raiz.izquierdo = eliminar(raiz.izquierdo, codigo);
        else if (cmp > 0) raiz.derecho = eliminar(raiz.derecho, codigo);
        else {
            if ((raiz.izquierdo == null) || (raiz.derecho == null)) {
                NodoAVL temp = raiz.izquierdo != null ? raiz.izquierdo : raiz.derecho;
                raiz = temp;
            } else {
                NodoAVL temp = obtenerMinimoNodo(raiz.derecho);
                raiz.producto = temp.producto;
                raiz.derecho = eliminar(raiz.derecho, temp.producto.codigo);
            }
        }
        if (raiz == null) return null;
        actualizarAltura(raiz);
        int b = balance(raiz);

        if (b > 1 && balance(raiz.izquierdo) >= 0) return rotarDerecha(raiz);
        if (b > 1 && balance(raiz.izquierdo) < 0) {
            raiz.izquierdo = rotarIzquierda(raiz.izquierdo); return rotarDerecha(raiz);
        }
        if (b < -1 && balance(raiz.derecho) <= 0) return rotarIzquierda(raiz);
        if (b < -1 && balance(raiz.derecho) > 0) {
            raiz.derecho = rotarDerecha(raiz.derecho); return rotarIzquierda(raiz);
        }
        return raiz;
    }

    private NodoAVL obtenerMinimoNodo(NodoAVL nodo) {
        NodoAVL actual = nodo;
        while (actual.izquierdo != null) actual = actual.izquierdo;
        return actual;
    }

    private void llenarTablaInOrden(NodoAVL n) {
        if (n != null) {
            llenarTablaInOrden(n.izquierdo);
            String estado = "✅ Óptimo";
            if (n.producto.stockActual <= n.producto.stockMinimo) estado = "⚠️ CRÍTICO";
            else if (n.producto.stockActual >= n.producto.stockMaximo) estado = "🛑 LÍMITE BODEGA";

            modeloTabla.addRow(new Object[]{
                n.producto.codigo,
                n.producto.nombre,
                n.producto.categoria,
                n.producto.stockActual,
                n.producto.stockMinimo,
                n.producto.stockMaximo,
                estado
            });
            llenarTablaInOrden(n.derecho);
        }
    }

    public void actualizarTablaGrafica() {
        modeloTabla.setRowCount(0);
        llenarTablaInOrden(raiz);
    }

    // --- APLICACIONES DE CÁLCULO INTEGRAL ---
    private void calcularDonacionTrapecio() {
        double[] tasasLectura = {10.0, 25.0, 45.0, 30.0, 15.0, 5.0, 2.0};
        int n = tasasLectura.length - 1;
        double h = 1.0; 

        double sumaInner = 0;
        for (int i = 1; i < n; i++) {
            sumaInner += tasasLectura[i];
        }
        double totalAproximado = (h / 2.0) * (tasasLectura[0] + 2 * sumaInner + tasasLectura[n]);

        JOptionPane.showMessageDialog(this, 
            String.format("📊 AUDITORÍA NUMÉRICA DE DONACIÓN (Regla del Trapecio - Prob 4.2)\n\n" +
                          "Muestreo de recepción en 6 horas:\n" +
                          "• Hora 0: %.1f | Hora 1: %.1f | Hora 2: %.1f\n" +
                          "• Hora 3: %.1f | Hora 4: %.1f | Hora 5: %.1f | Hora 6: %.1f\n\n" +
                          "Total estimado recibido mediante la Integral Numérica: %.2f kg/unidades",
                          tasasLectura[0], tasasLectura[1], tasasLectura[2], tasasLectura[3],
                          tasasLectura[4], tasasLectura[5], tasasLectura[6], totalAproximado),
            "Cálculo Integral - Regla del Trapecio", JOptionPane.INFORMATION_MESSAGE);
    }

    private void calcularDepreciacionExponencial() {
        String codigo = JOptionPane.showInputDialog(this, "Ingrese el código del producto a analizar:");
        if (codigo == null || codigo.trim().isEmpty()) return;

        Producto p = buscar(codigo.trim());
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Producto no encontrado.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double R0 = p.stockActual;
        double k = 0.15;
        double t = 4.0;

        double consumoAcumulado = (R0 / k) * (1.0 - Math.exp(-k * t));
        double remanenteFuturo = R0 * Math.exp(-k * t);

        JOptionPane.showMessageDialog(this,
            String.format("📉 MODELO DE CONSUMO EXPONENCIAL (CLV Adaptado - Prob 6.6)\n\n" +
                          "Producto: %s (Stock Actual: %d)\n" +
                          "Tasa de Despacho/Consumo mensual (k): %.2f\n" +
                          "Proyección a: %.0f meses\n\n" +
                          "• Stock estimado remanente en bodega: %.1f\n" +
                          "• Consumo acumulado por las familias: %.1f",
                          p.nombre, p.stockActual, k, t, remanenteFuturo, consumoAcumulado),
            "Cálculo Integral - Modelo Exponencial", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- CARGA DE DATOS ---
    private void inicializarBaseDeDatosYArbol() {
        Producto[] listaInicial = {
            new Producto("010", "Arroz Premium (kg)", 40, 15, 100, "Granos y Cereales (kg)"),
            new Producto("020", "Frijol Negro (kg)", 25, 10, 80, "Granos y Cereales (kg)"),
            new Producto("030", "Lentejas (kg)", 8, 12, 60, "Granos y Cereales (kg)"),
            new Producto("040", "Aceite Vegetal (L)", 50, 20, 120, "Abarrotes"),
            new Producto("050", "Atún en Lata (und)", 3, 25, 150, "Proteínas y Conservas (und)")
        };

        for (Producto p : listaInicial) {
            insertar(p);
        }
        actualizarTablaGrafica();
    }

    private void registrarAlimento() {
        try {
            String codigo = txtCodigo.getText().trim();
            String nombre = txtNombre.getText().trim();
            String categoria = (String) cmbCategoria.getSelectedItem();

            if (codigo.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos obligatorios.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (buscar(codigo) != null) {
                JOptionPane.showMessageDialog(this, "El código ya existe.", "Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int stockActual = Integer.parseInt(txtStockActual.getText().trim());
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());
            int stockMaximo = Integer.parseInt(txtStockMaximo.getText().trim());

            Producto nuevo = new Producto(codigo, nombre, stockActual, stockMinimo, stockMaximo, categoria);
            insertar(nuevo);
            actualizarTablaGrafica();
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "¡Alimento registrado exitosamente en la estructura AVL!");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese números enteros válidos en los stocks.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarStock() {
        String codigo = JOptionPane.showInputDialog(this, "Ingrese el código del alimento:");
        if (codigo == null || codigo.trim().isEmpty()) return;

        Producto p = buscar(codigo.trim());
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Alimento no encontrado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int nuevoAct = Integer.parseInt(JOptionPane.showInputDialog(this, "Nuevo Stock Actual:", p.stockActual));
            int nuevoMin = Integer.parseInt(JOptionPane.showInputDialog(this, "Nuevo Stock Mínimo:", p.stockMinimo));
            int nuevoMax = Integer.parseInt(JOptionPane.showInputDialog(this, "Nuevo Stock Máximo:", p.stockMaximo));

            p.stockActual = nuevoAct;
            p.stockMinimo = nuevoMin;
            p.stockMaximo = nuevoMax;

            insertar(p);
            actualizarTablaGrafica();
            JOptionPane.showMessageDialog(this, "¡Stock actualizado!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valores numéricos inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarAlimento() {
        String codigo = JOptionPane.showInputDialog(this, "Ingrese el código a eliminar:");
        if (codigo == null || codigo.trim().isEmpty()) return;

        if (buscar(codigo.trim()) == null) {
            JOptionPane.showMessageDialog(this, "El código no existe.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        eliminar(codigo.trim());
        actualizarTablaGrafica();
        JOptionPane.showMessageDialog(this, "¡Elemento eliminado del Árbol AVL!");
    }

    private void buscarAlimento() {
        String codigo = JOptionPane.showInputDialog(this, "Ingrese el código a buscar:");
        if (codigo == null || codigo.trim().isEmpty()) return;

        Producto p = buscar(codigo.trim());
        if (p != null) {
            JOptionPane.showMessageDialog(this, "📦 Producto Encontrado:\n\n" + p.toString(), "Resultado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "❌ No existe el producto.", "Sin resultados", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtStockActual.setText("");
        txtStockMinimo.setText("");
        txtStockMaximo.setText("");
        cmbCategoria.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new ArbolAVLInventario().setVisible(true));
    }
}
