// Pasa la imagen capturada por el motor OCR y procesa el bloque de texto crudo resultante
// mediante reglas y filtros de texto, limpiando errores típicos del reconocimiento y
// transformando líneas de texto en objetos estructurados.
package main.java.SistemasLegados.wrapper;

import net.sourceforge.tess4j.Tesseract;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrParserService {

    public String extraerTexto(String rutaImagen) {
        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("Tess4J-3.4.8-src\\Tess4J\\tessdata");
            tesseract.setLanguage("spa");

            // Bloque grande de texto
            tesseract.setPageSegMode(6);

            return tesseract.doOCR(new File(rutaImagen));
        } catch (Exception e) {
            System.err.println("Error en el OCR: " + e.getMessage());
            return "";
        }
    }

    public int extraerNumeroRegistros(String textoOcr) {
        Pattern patron = Pattern.compile("(\\d+)\\s+ARCHIV", Pattern.CASE_INSENSITIVE);
        Matcher matcher = patron.matcher(textoOcr);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }
}