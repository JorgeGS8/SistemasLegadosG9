package main.java.SistemasLegados.wrapper;

// Traduce comandos de alto nivel (como "escribe 'F-15' y pulsa ENTER") a secuencias
// de eventos de teclado de bajo nivel (Robot)

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class InputRobot {
    
    private Robot robot;

    public InputRobot() throws Exception {
        this.robot = new Robot();
    }
    
    // Por ej
    public void pulsarEnter() {
        robot.keyPress(KeyEvent.VK_ENTER);
    }
}
