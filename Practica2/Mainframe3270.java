import java.io.*;


public class Mainframe3270 {

    private Process proceso;

    private BufferedReader entrada;

    private PrintWriter salida;

    private BufferedReader errores;

    public void iniciar() throws IOException {

        proceso = Runtime.getRuntime().exec("\"C:\\Program Files\\wc3270\\ws3270.exe\"");

        entrada = new BufferedReader(new InputStreamReader(proceso.getInputStream()));

        errores = new BufferedReader(new InputStreamReader(proceso.getErrorStream()));

        salida = new PrintWriter(new OutputStreamWriter(proceso.getOutputStream()), true);

        salida.println("Connect(155.210.152.51:3270)");

        boolean conectado = false;

       // System.out.println("Conectado al mainframe.");

        String linea;

        while ((linea = entrada.readLine()) != null) {
            System.out.println(linea);

            if (linea.contains("ok")) {
                conectado = true;
                break;
            }

            // Verificar si la conexión fue exitosa
            // (BORRAR) alguna palabra clave tiene q haber por ahi como failed o succes o algo
        }
    
        if (conectado) {
            System.out.println("Conectado al mainframe.");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            salida.println("ReadBuffer(Ascii)");

            while ((linea = entrada.readLine()) != null) {
                System.out.println(linea);

                if (linea.contains("ok")) {
                    break;
                }
            }

            salida.println("String(grupo_o9)");

            System.out.println("Usuario enviado.");

            salida.println("Enter()");

            System.out.println("Enter enviado.");

            salida.println("ReadBuffer(Ascii)");

            while ((linea = entrada.readLine()) != null) {
                System.out.println(linea);

                if (linea.contains("ok")) {
                    break;
                }
            }
            
        } else {
            System.out.println("No se pudo conectar al mainframe.");
            // Aqui un return o retry o algo no se
        }


    }
}