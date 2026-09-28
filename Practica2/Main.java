public class Main {
    public static void main(String[] args) {

        try {

            Mainframe3270 mainframe = new Mainframe3270();

            mainframe.iniciar();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}