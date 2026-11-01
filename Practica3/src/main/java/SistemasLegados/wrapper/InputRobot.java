package main.java.SistemasLegados.wrapper;

// Traduce comandos de alto nivel (como "escribe 'F-15' y pulsa ENTER") a secuencias
// de eventos de teclado de bajo nivel (Robot)

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class InputRobot {
    
    private Robot robot;

    public InputRobot() throws Exception {
        this.robot = new Robot();
        // Tiempo de espera entre pulsaciones para que DOSBox no se pierda, por si acaso la vd, se puede mirar reducir o quitar
        this.robot.setAutoDelay(50);
    }
    
    // Pulsar y soltar
    public void pulsarEnter() {
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    public void pulsarEspacio() {
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.keyRelease(KeyEvent.VK_SPACE);
    }

    public void escribirTexto(String texto) {
        for (char c : texto.toCharArray()) {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            if (Character.isUpperCase(c) || c == '-' || c == ' ') {
                // Tener en cuenta el shift para las mayus y otros caracteres
                if(c == '-') {
                    keyCode = KeyEvent.VK_MINUS;
                } else if (c == ' ') {
                    keyCode = KeyEvent.VK_SPACE;
                }
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else {
                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);
            }
        }
    }

    // Para añadir delay para que no quede colgado
    public void esperar(int milisegundos) {
        robot.delay(milisegundos);
    }

}
