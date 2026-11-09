// Gestiona el proceso del sistema operativo: lanza database.bat, comprueba si el proceso sigue vivo
// y gestiona que la ventana de DOSBox se mantenga en primer plano cuando sea necesario interactuar con ella.

package SistemasLegados.wrapper;

import java.io.File;

public class LegacyProcessManager {

    private Process procesoDosBox;

    public void iniciarAplicacionLegada() throws Exception {
        File carpetaBase = new File("Database-MSDOS");

        // Ruta al DOSBox.exe y al archivo .bat
        File exeDosBox = new File(carpetaBase, "DOSBox-0.74\\DOSBox.exe");
        File archivoBat = new File(carpetaBase, "Database\\gwbasic.bat");

        if (!exeDosBox.exists()) {
            throw new RuntimeException("No se encuentra DOSBox.exe en: " + exeDosBox.getAbsolutePath());
        }

        System.out.println("Lanzando DOSBox directamente...");

        // Lanzar el DOSBox
        ProcessBuilder pb = new ProcessBuilder(exeDosBox.getAbsolutePath(), archivoBat.getAbsolutePath(), "-noconsole");
        pb.directory(carpetaBase);

        procesoDosBox = pb.start();
    }

    public boolean estaActivo() {
        return procesoDosBox != null && procesoDosBox.isAlive();
    }

    public void cerrarAplicacionLegada() {
        if (procesoDosBox != null && procesoDosBox.isAlive()) {
            procesoDosBox.destroy();
        }
    }
}