import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

//Nuevo comentario para q me deje el commit xd
public class MainWindow extends JFrame {

    private JTable tablaTareas;
    private DefaultTableModel modeloTabla;
    private Mainframe3270 mainframe;
    private boolean operacionEnCurso = false;

    private JButton btnNueva;
    private JButton btnRefrescar;
    private JButton btnSalir;

    public MainWindow(Mainframe3270 mf) {
        this.mainframe = mf;

        setTitle("Tareas");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        modeloTabla = new DefaultTableModel(
            new String[]{"Tipo", "Fecha", "Nombre", "Descripción"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaTareas = new JTable(modeloTabla);
        add(new JScrollPane(tablaTareas), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        btnNueva = new JButton("Nueva");
        btnRefrescar = new JButton("Refrescar");
        btnSalir = new JButton("Salir");
        panelBotones.add(btnNueva);
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnSalir);
        add(panelBotones, BorderLayout.SOUTH);

        btnRefrescar.addActionListener(e -> refrescarTareas());
        btnNueva.addActionListener(e -> nuevaTarea());
        btnSalir.addActionListener(e -> {
            try { mainframe.salir(); } catch (Exception ex) { ex.printStackTrace(); }
            dispose();
        });

        setVisible(true);
        refrescarTareas();
    }

    /** Bloquea los botones mientras hay una operación MUY IMPORTANTE PORQ ANTES SIN ESTO AUNQ NO HUBIERA TERMINADO 
     * y hubiera pasado un tiempo podian explotar los campos al poner nuevas tareas
     */
    private void setBotonesHabilitados(boolean habilitados) {
        btnNueva.setEnabled(habilitados);
        btnRefrescar.setEnabled(habilitados);
        btnSalir.setEnabled(habilitados);
    }

    private void refrescarTareas() {
        if (operacionEnCurso) return;
        operacionEnCurso = true;
        setBotonesHabilitados(false);
        try {
            List<String[]> tareas = mainframe.listarTodasTareas();
            modeloTabla.setRowCount(0);
            for (String[] t : tareas) {
                modeloTabla.addRow(t);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al listar tareas: " + ex.getMessage());
        } finally {
            operacionEnCurso = false;
            setBotonesHabilitados(true);
        }
    }

    private void nuevaTarea() {
        if (operacionEnCurso) return;

        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"General", "Específica"});
        JTextField fecha = new JTextField();
        JTextField nombre = new JTextField();
        JTextField descripcion = new JTextField();

        Object[] mensaje = {
            "Tipo:", cmbTipo,
            "Fecha (DDMM):", fecha,
            "Nombre (solo específicas):", nombre,
            "Descripción:", descripcion
        };

        int opcion = JOptionPane.showConfirmDialog(
            this, mensaje, "Nueva tarea", JOptionPane.OK_CANCEL_OPTION
        );

        if (opcion != JOptionPane.OK_OPTION) return;

        int tipo = cmbTipo.getSelectedIndex() == 0
                ? Mainframe3270.TIPO_GENERAL
                : Mainframe3270.TIPO_ESPECIFICA;

        operacionEnCurso = true;
        setBotonesHabilitados(false);
        try {
            mainframe.crearTarea(
                tipo,
                fecha.getText(),
                nombre.getText(),
                descripcion.getText()
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear tarea: " + ex.getMessage());
        } finally {
            operacionEnCurso = false;
            setBotonesHabilitados(true);
        }

        refrescarTareas();
    }
}