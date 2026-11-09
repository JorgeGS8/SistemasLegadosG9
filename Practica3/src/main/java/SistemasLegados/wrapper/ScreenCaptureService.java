// Calcula la posición de la ventana de la aplicación legada y toma capturas gráficas de
// la región donde se muestran las tablas de datos, recortando bordes innecesarios para optimizar el OCR.

package SistemasLegados.wrapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;

public class ScreenCaptureService {

    public void capturarPantallaLegada(String rutaGuardado) throws Exception {
        Robot robot = new Robot();

        // Capturar TODA la pantalla
        Rectangle pantallaCompleta = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage captura = robot.createScreenCapture(pantallaCompleta);

        // INVERTIMOS LOS COLORES
        // Muy importante pues Tesseract es medio medio y solo lee en negro y fondo blanco
        BufferedImage imagenGris = new BufferedImage(captura.getWidth(), captura.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = imagenGris.getGraphics();
        g.drawImage(captura, 0, 0, null);
        g.dispose();
        RescaleOp op = new RescaleOp(-1.0f, 255f, null);
        BufferedImage imagenInvertida = op.filter(imagenGris, null);

        ImageIO.write(imagenInvertida, "png", new File(rutaGuardado));
    }
}