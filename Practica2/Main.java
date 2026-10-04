import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            Mainframe3270 mainframe = new Mainframe3270();
            mainframe.iniciar();
            SwingUtilities.invokeLater(() -> new MainWindow(mainframe));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}