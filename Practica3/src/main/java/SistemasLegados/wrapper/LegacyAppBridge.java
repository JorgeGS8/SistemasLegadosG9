// Fachada que coordina todo el ciclo: recibe una orden de negocio, adquiere el cerrojo de sincronización
// (para que dos usuarios en la web no envíen teclas a la vez), pulsa las teclas necesarias,
// captura la pantalla, parsea el texto y devuelve la información lista a la capa de servicio.

package main.java.SistemasLegados.wrapper;

import main.java.SistemasLegados.model.Programa;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.concurrent.locks.ReentrantLock;

public class LegacyAppBridge {

    private LegacyProcessManager processManager;
    private InputRobot robot;
    private ScreenCaptureService capturer;
    private OcrParserService ocrParser;

    // Cerrojo para que no colapsen varias teclas a la vez
    private static final ReentrantLock cerrojo = new ReentrantLock();

    public LegacyAppBridge() throws Exception {
        this.processManager = new LegacyProcessManager();
        this.robot = new InputRobot();
        this.capturer = new ScreenCaptureService();
        this.ocrParser = new OcrParserService();
    }

    public int obtenerNumeroRegistros() {
        // Bloquear para que nadie toque DOSBox
        cerrojo.lock();
        try {
            System.out.println("Bridge: Solicitando número de registros...");

            // Arrancar
            processManager.iniciarAplicacionLegada();
            Thread.sleep(6000); // Un poco de delay (no lo he probado sin porq funciona pero se podria probar)

            // Pantalla completa con alt y enter para q lo lea perfecto todo
            Robot r = new Robot();
            r.keyPress(KeyEvent.VK_ALT);
            r.keyPress(KeyEvent.VK_ENTER);
            r.keyRelease(KeyEvent.VK_ENTER);
            r.keyRelease(KeyEvent.VK_ALT);
            Thread.sleep(3000);

            r.keyPress(KeyEvent.VK_ESCAPE);
            r.keyRelease(KeyEvent.VK_ESCAPE);
            Thread.sleep(1000);

            // Elegir la opción 4
            robot.escribirTexto("4");
            robot.esperar(3000);

            // Capturar y hacer OCR
            capturer.capturarPantallaLegada("captura_bridge.png");
            String texto = ocrParser.extraerTexto("captura_bridge.png");

            // Salir de la opción 4 con un Enter
            robot.pulsarEnter();
            robot.esperar(1000);

            // Cerrar la aplicación legada
            processManager.cerrarAplicacionLegada();

            int numRegistros = ocrParser.extraerNumeroRegistros(texto);
            System.out.println("Bridge: Registros encontrados -> " + numRegistros);
            return numRegistros;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            cerrojo.unlock(); // Liberar SIEMPRE SI NO BOMBOCLAT
        }
    }

    public Programa buscarProgramaPorNombre(
        String programaBuscado
    ) {
        // Bloquear para que nadie toque DOSBox
        cerrojo.lock();
        try {
            System.out.println("Bridge: Solicitando datos del programa...");

            // Arrancar
            processManager.iniciarAplicacionLegada();
            Thread.sleep(6000); // Un poco de delay (no lo he probado sin porq funciona pero se podria probar)

            // Pantalla completa con alt y enter para q lo lea perfecto todo
            Robot r = new Robot();
            r.keyPress(KeyEvent.VK_ALT);
            r.keyPress(KeyEvent.VK_ENTER);
            r.keyRelease(KeyEvent.VK_ENTER);
            r.keyRelease(KeyEvent.VK_ALT);
            Thread.sleep(3000);

            r.keyPress(KeyEvent.VK_ESCAPE);
            r.keyRelease(KeyEvent.VK_ESCAPE);
            Thread.sleep(1000);

            // Elegir la opción 7
            robot.escribirTexto("7");
            robot.esperar(3000);

            // Elegir el programa con su nombre
            // Saltar peticion de nº registro
            robot.escribirTexto("N");
            robot.pulsarEnter();
            robot.esperar(1000);
            // Elegir el programa con su nombre
            robot.escribirTexto(programaBuscado);
            robot.pulsarEnter();
            robot.esperar(2000);

            // Capturar y hacer OCR
            capturer.capturarPantallaLegada("captura_bridge.png");
            String texto = ocrParser.extraerTexto("captura_bridge.png");

            // Salir de la opción 4 con 3 Enter
            robot.pulsarEnter();
            robot.pulsarEnter();
            robot.pulsarEnter();
            robot.esperar(1000);

            Programa programa = ocrParser.extraerDatosPrograma(texto);


            //FIXME BORRAR LUEGO
            System.out.println("Bridge: Texto OCR -> " + texto);


            System.out.println("Bridge: Programa encontrado -> " + programa);
            return programa;

        } catch (Exception e) {
            throw new RuntimeException("Error al buscar programa por nombre: ", e);
        } finally {
            cerrojo.unlock(); // Liberar SIEMPRE SI NO BOMBOCLAT
        }
    }
}