// Pasa la imagen capturada por el motor OCR y procesa el bloque de texto crudo resultante
// mediante reglas y filtros de texto, limpiando errores típicos del reconocimiento y
// transformando líneas de texto en objetos estructurados.
package main.java.SistemasLegados.wrapper;

import net.sourceforge.tess4j.Tesseract;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.SistemasLegados.model.Programa;

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

    // Extrae los datos de un programa a partir del texto OCR, devolviendo un objeto Programa con los campos relevantes.
    public Programa extraerDatosPrograma(String textoOcr) {
        // El texto captura algo del estilo NUMERO DE REGISTRO GUION(- o K porque lo lee mal) NOMBRE(puede tener espacios) TIPO CINTA:(IDENTIFICADOR)
        //  y despues una linea con pregunta ES ESTE 7 ISZMI 7 K (ES ESTE? (S/N) ? _)
        // Ejemplo:  15 K COMBAT ZONE ARCADE CINTAEA (15 - COMBAT ZONE ARCADE CINTA:A)
        Pattern patron = Pattern.compile("(\\d+)\\s+[Kk-]\\s+([A-Z0-9 ]+)\\s+([A-Z]+)\\s+CINTA[E:]([A-Z0-9]+)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = patron.matcher(textoOcr);
        if (matcher.find()) {
            int numeroRegistro = Integer.parseInt(matcher.group(1));
            String nombre = matcher.group(2).trim();
            String tipo = matcher.group(3).trim();
            String identificadorCinta = matcher.group(4).trim();

            return new Programa(numeroRegistro, nombre, tipo, identificadorCinta);
        }
        return null;
    }

    // FIXME
    // Extrae los programas de la lista que estan en una cinta concreta, devolviendo una lista de objetos Programa.
    public List<Programa> extraerProgramasPorCinta(String textoOcr, String cintaBuscada) {
        List<Programa> programas = new ArrayList<>();

        // El texto a capturar es del estilo NUMERO NOMBRE TIPO CINTA NUMERODEREGISTRO
        // Ejemplo:  15 COMBAT ZONE ARCADE A 15
        // Hay que capturar todos los programas que tengan la cinta buscada, y devolverlos en una lista de objetos Programa.

        Pattern patron = Pattern.compile("\\d+\\s+([A-Z0-9-. ]+)\\s+([A-Z0-9-. ]+)\\s+([A-Z0-9-]+)\\s+(\\d+)\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = patron.matcher(textoOcr);
        while (matcher.find()) {
            String nombre = matcher.group(1).trim();
            String tipo = matcher.group(2).trim();
            String identificadorCinta = matcher.group(3).trim();
            int numeroRegistro = Integer.parseInt(matcher.group(4));

            // Verificar si el identificador de la cinta coincide con la cinta buscada
            if (identificadorCinta.equalsIgnoreCase(cintaBuscada)) {
                Programa programanuevo = new Programa(numeroRegistro, nombre, tipo, identificadorCinta);
                programas.add(programanuevo);

                //DEBUG FIXME
                System.out.println("OCR: Programa encontrado -> " + programanuevo);
            }
        }
        return programas;
    }

    public boolean esFinDeLista(String textoOcr) {
        // Verifica si el texto OCR contiene texto del menu principal
        // al acabar la lista sale directamente al menu principal
        return textoOcr.contains("GRABAR DATOS") || textoOcr.contains("ORDENAR DATOS");
    }
}