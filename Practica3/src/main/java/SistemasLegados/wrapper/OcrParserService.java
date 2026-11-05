// Pasa la imagen capturada por el motor OCR y procesa el bloque de texto crudo resultante
// mediante reglas y filtros de texto, limpiando errores típicos del reconocimiento y
// transformando líneas de texto en objetos estructurados.
package main.java.SistemasLegados.wrapper;

import net.sourceforge.tess4j.Tesseract;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.SistemasLegados.model.Programa;

import javax.imageio.ImageIO;

public class OcrParserService {

    private final Tesseract tesseract;

    public OcrParserService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("Tess4J-3.4.8-src\\Tess4J\\tessdata");
        tesseract.setLanguage("spa");

        tesseract.setTessVariable("load_system_dawg", "0");
        tesseract.setTessVariable("load_freq_dawg", "0");
        tesseract.setTessVariable("load_punc_dawg", "0");
        tesseract.setTessVariable("load_number_dawg", "0");
        tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 -./");

        tesseract.setOcrEngineMode(0);
        // Bloque grande de texto
        tesseract.setPageSegMode(6);
    }

    public String extraerTexto(String rutaImagen) {
        try {
            // Por si acaso esta configurado de otra manera
            tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 -./");

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

    public List<Programa> leerPagina(String rutaImagen) {
        try {

            BufferedImage imagen = ImageIO.read(new File(rutaImagen));
            List<Programa> listaProgramas = new ArrayList<>();

            // ---- 1. Definir verticales (excluir encabezado y pie de pagina) ----
            int inicioY = 100;
            int altoTotal = 395 - inicioY;
            double altoFila = (double) altoTotal / 18.0; // 18 filas por pagina

            // Anchos y posiciones X de cada celda
            int xNum = 30, wNum = 50;
            int xNombre = 85, wNombre = 260;
            int xTipo = 350, wTipo = 140;
            int xCinta = 500, wCinta = 70;
            int xNumReg = 570, wNumReg = 70;

            // ---- 2. Bucle de 18 filas ----
            for (int i = 0; i < 18; i++) {
                int yFila = inicioY + (int) Math.round(i * altoFila);
                int hFila = (int) Math.round(altoFila);

                // Recortar cada celda individual de la fila
                BufferedImage imgNombre = imagen.getSubimage(xNombre, yFila, wNombre, hFila);
                BufferedImage imgTipo = imagen.getSubimage(xTipo, yFila, wTipo, hFila);
                BufferedImage imgCinta = imagen.getSubimage(xCinta, yFila, wCinta, hFila);
                BufferedImage imgNumReg = imagen.getSubimage(xNumReg, yFila, wNumReg, hFila);

                // ---- 3. Extraer texto de cada celda con configuracion estricta ----

                // Procesar nombre - texto alfanumerico
                tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 -./");
                String nombre = tesseract.doOCR(imgNombre).trim();

                if (nombre.isEmpty()) {
                    continue;
                }

                // Procesar tipo - letras mayusculas, espacios, guion y punto
                tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ -_.");
                String tipoOg = tesseract.doOCR(imgTipo).trim();
                String tipo;
                if (tipoOg.isEmpty() || tipoOg.contains("-") || tipoOg.length() < 3) {
                    tipo = corregirTipo("----");
                } else {
                    tipo = corregirTipo(tipoOg);
                }

                // Procesar cinta - letras, numeros y guion
                tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-");
                String cintaOg = tesseract.doOCR(imgCinta).trim();
                String cinta = corregirCinta(cintaOg);

                // Procesar numero de registro - solo digitos
                tesseract.setTessVariable("tessedit_char_whitelist", "0123456789");
                String numRegStr = tesseract.doOCR(imgNumReg).trim();
                int numeroRegistro = numRegStr.isEmpty() ? -1 : Integer.parseInt(numRegStr);

                // ---- 4. Combinar celdas en objetos Programa ----
                Programa programa = new Programa(numeroRegistro, nombre, tipo, cinta);

                //FIXME DEBUG IMPRIMIR PROGRAMA LEIDO
                //System.out.println("OCR leerPagina: Programa leido -> " + nombre + " | " + tipoOg + " | " + cintaOg + " | " + numeroRegistro);

                //System.out.println("OCR leerPagina: Programa insertado -> " + programa);
                listaProgramas.add(programa);
            }

            return listaProgramas;

        } catch (Exception e) {
            throw new RuntimeException("Error al extraer programas por columnas: ", e);
        }
    }

    public String corregirTipo(String tipo) {
        String t = tipo.toUpperCase().trim();
        t = t.replace(" ", "");
        if (t.contains("UTIL") || t.contains("ILID") || t.contains("IDAD")) return "UTILIDAD";
        if (t.contains("ARC") || t.contains("ADE")) return "ARCADE";
        if (t.contains("CONV") || t.contains("ERSA") || t.contains("SACI")) return "CONVERSACIONAL";
        if (t.contains("VID") || t.contains("EAV") || t.contains("ENT")) return "VIDEOAVENTURA";
        if (t.contains("SIM") || t.contains("ULA") || t.contains("DOR")) return "SIMULADOR";
        if (t.contains("JUE") || t.contains("GO") || t.contains("MESA")) return "JUEGO DE MESA";
        if (t.contains("DEP") || t.contains("ORT") || t.contains("IVO")) return "S. DEPORTIVO";
        if (t.contains("EST") || t.contains("RAT") || t.contains("EGI")) return "ESTRATEGIA";
        return "----";
    }

    public String corregirCinta(String cinta) {
        String c = cinta.toUpperCase().trim();
        // Hay un error que se leen - como K, por ejemplo 13-45 se lee 13K45
        // Pero K tambien es un posible identificador de cinta, por ejemplo K-12, que se lee KK12

        if (c.matches("^KK[A-Z0-9]+$")) {
            System.out.println("OCR corregirCinta: Corrigiendo KK a K- en " + c);
            return c.replaceFirst("KK", "K-");
        } else if (c.matches("^[A-Z0-9]+K[A-Z0-9]+$")) {
            System.out.println("OCR corregirCinta: Corrigiendo [X]K a [X]- en " + c);
            return c.replaceFirst("K", "-");
        }
        return c;
    }

    public boolean esFinDeLista(String textoOcr) {
        // Verifica si el texto OCR contiene texto del menu principal
        // al acabar la lista sale directamente al menu principal

        return textoOcr.contains("GRABAR DATOS") || textoOcr.contains("ORDENAR DATOS");
    }
}