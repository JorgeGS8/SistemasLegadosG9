import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Mainframe3270 mainframe = new Mainframe3270();
            try {
                mainframe.iniciarConexion();
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainWindow(mainframe);
        });
    }
}