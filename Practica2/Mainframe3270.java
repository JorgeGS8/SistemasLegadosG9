import java.io.*;
import java.util.*;

public class Mainframe3270 {

    public static final int TIPO_GENERAL    = 1;
    public static final int TIPO_ESPECIFICA = 2;

    private Process proceso;
    private BufferedReader entrada;
    private PrintWriter salida;
    private BufferedReader errores;

    // ---------------------------------------------------------------
    // Comunicación con ws3270
    // ---------------------------------------------------------------
    private void enviarComando(String cmd) throws IOException {
        salida.println(cmd);
        String linea;
        while ((linea = entrada.readLine()) != null) {
            System.out.println("[DEBUG] " + linea);
            if (linea.equals("ok")) break;
            if (linea.startsWith("error")) {
                System.err.println("Error en comando: " + cmd);
                break;
            }
        }
    }

    
    private String sincronizar() throws IOException {
        enviarComando("Wait(60, InputField)");
        pausa(800);

        String previa = "";
        String actual = "";
        int estables = 0;

        for (int i = 0; i < 15; i++) {
            actual = leerPantalla();

            if (tieneMoreAlFinal(actual)) {
                System.out.println("[INFO] 'More...' detectado, paso de página");
                enviarComando("Enter()");
                enviarComando("Wait(60, InputField)");
                pausa(1000);
                previa = "";
                estables = 0;
                continue;
            }

            if (actual.equals(previa)) {
                estables++;
                if (estables >= 2) return actual;
            } else {
                estables = 0;
            }
            previa = actual;
            pausa(500);
        }
        return actual;
    }


    /** Comprueba las últimas 10 líneas del buffer (el more ese). */
    private boolean tieneMoreAlFinal(String pantalla) {
        String[] lineas = pantalla.split("\n");
        int desde = Math.max(0, lineas.length - 10);
        for (int i = desde; i < lineas.length; i++) {
            if (lineas[i].contains("More...")) return true;
        }
        return false;
    }

    private String leerPantalla() throws IOException {
        salida.println("Ascii()");
        StringBuilder sb = new StringBuilder();
        String linea;
        while ((linea = entrada.readLine()) != null) {
            if (linea.equals("ok")) break;
            if (linea.startsWith("U F ")) continue;
            if (linea.startsWith("data: ")) {
                sb.append(linea.substring(6)).append("\n");
            } else {
                sb.append(linea).append("\n");
            }
        }
        return sb.toString();
    }

    private void pausa(int milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }



    // ---------------------------------------------------------------
    // Iniciar lanzando el sistema legaddo con el grupo y el secreto6
    // ---------------------------------------------------------------
    public void iniciar() throws IOException {
        proceso = new ProcessBuilder(
            "C:\\Program Files\\wc3270\\ws3270.exe"
        ).start();
        entrada = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
        errores = new BufferedReader(new InputStreamReader(proceso.getErrorStream()));
        salida  = new PrintWriter(new OutputStreamWriter(proceso.getOutputStream()), true);

        enviarComando("Connect(155.210.152.51:3270)");
        pausa(2000);

        // Pantalla Hércules
        enviarComando("Enter()");
        sincronizar();

        String pantalla = leerPantalla();
        System.out.println("=== PANTALLA LOGIN ===");
        System.out.println(pantalla);

        // Usuario
        enviarComando("String(grupo_09)");
        enviarComando("Enter()");
        sincronizar();

        pantalla = leerPantalla();
        System.out.println("=== PANTALLA PASSWORD ===");
        System.out.println(pantalla);

        // Password
        enviarComando("String(secreto6)");
        enviarComando("Enter()");
        sincronizar();

        pantalla = leerPantalla();
        System.out.println("=== PANTALLA POST-LOGIN ===");
        System.out.println(pantalla);

        if (pantalla.contains("Press ENTER")) {
            enviarComando("Enter()");
            sincronizar();
        }

        pantalla = leerPantalla();
        System.out.println("=== PANTALLA MENU MUSIC ===");
        System.out.println(pantalla);

        // Lanzar tareas.c por lo q sea no es tasks.c
        enviarComando("String(tareas.c)");
        enviarComando("Enter()");
        sincronizar();

        pantalla = leerPantalla();
        System.out.println("=== MENÚ PRINCIPAL TAREAS ===");
        System.out.println(pantalla);
    }


    // ---------------------------------------------------------------
    // Listar las tareicas
    // ---------------------------------------------------------------
    public List<String[]> listarTodasTareas() throws IOException {
        List<String[]> todas = new ArrayList<>();

        // Ir a VIEW TASKS 
        enviarComando("String(2)");
        enviarComando("Enter()");
        sincronizar();

        // Listar GENERALES
        enviarComando("String(1)");
        enviarComando("Enter()");
        String p1 = sincronizar();
        System.out.println("=== LISTA GENERALES ===");
        System.out.println(p1);
        todas.addAll(parsearTareas(p1, TIPO_GENERAL));

        // Listar ESPECÍFICAS
        enviarComando("String(2)");
        enviarComando("Enter()");
        String p2 = sincronizar();
        System.out.println("=== LISTA ESPECÍFICAS ===");
        System.out.println(p2);
        todas.addAll(parsearTareas(p2, TIPO_ESPECIFICA));

        // Salir a MAIN MENU
        enviarComando("String(3)");
        enviarComando("Enter()");
        sincronizar();

        return todas;
    }



    // ---------------------------------------------------------------
    // Crear tareica
    // ---------------------------------------------------------------
    public void crearTarea(int tipo, String fecha, String nombre, String descripcion) throws IOException {
        enviarComando("String(1)");
        enviarComando("Enter()");
        sincronizar();
        pausa(800);

        enviarComando("String(" + tipo + ")");
        enviarComando("Enter()");
        sincronizar();
        pausa(800);

        if (fecha != null && !fecha.isEmpty()) enviarComando("String(" + fecha + ")");
        enviarComando("Enter()");
        sincronizar();
        pausa(800);

        if (tipo == TIPO_ESPECIFICA) {
            if (nombre != null && !nombre.isEmpty()) enviarComando("String(\"" + nombre + "\")");
            enviarComando("Enter()");
            sincronizar();
            pausa(800);
        }

        if (descripcion != null && !descripcion.isEmpty()) enviarComando("String(\"" + descripcion + "\")");
        enviarComando("Enter()");
        sincronizar();
        pausa(1200);

        enviarComando("String(3)");
        enviarComando("Enter()");
        sincronizar();
    }

    
    public void salir() throws IOException {
        enviarComando("String(0)");
        enviarComando("Enter()");
        proceso.destroy();
    }


    // ---------------------------------------------------------------
    // Parseo, convierte la pantalla cruda del terminal en una lista de tareas, aislando solo el último listado y quedándose con el tipo pedido.
    // ---------------------------------------------------------------
    private List<String[]> parsearTareas(String pantalla, int tipoFiltro) {
        List<String[]> tareas = new ArrayList<>();
        String[] lineas = pantalla.split("\n");

        // Localizar el FINAL del último bloque de tareas
        int finBloque = -1;
        for (int i = lineas.length - 1; i >= 0; i--) {
            if (lineas[i].contains("TOTAL TASK")) {
                finBloque = i;
                break;
            }
        }
        if (finBloque == -1) return tareas;

        // Va  pa atrás buscando el INICIO del bloque
        int iniBloque = finBloque;
        for (int i = finBloque - 1; i >= 0; i--) {
            String l = lineas[i].trim();
            if (l.startsWith("TASK ")) {
                iniBloque = i;
            } else if (l.isEmpty()) {
                continue;
            } else {
                break;
            }
        }

        // Parsear SOLO ese bloque y filtrar por tipo
        for (int i = iniBloque; i < finBloque; i++) {
            String linea = lineas[i].trim();
            if (!linea.startsWith("TASK ")) continue;

            // Quitar kaka
            int posDosPuntos = linea.indexOf(":");
            if (posDosPuntos == -1) continue;

            String cuerpo = linea.substring(posDosPuntos + 1);

            //Encontrar el PRIMER marcador (GENERAL o SPECIFIC)
            int gen1  = cuerpo.indexOf("GENERAL ");
            int spec1 = cuerpo.indexOf("SPECIFIC ");
            int startMarker;
            if (gen1 >= 0 && spec1 >= 0) startMarker = Math.min(gen1, spec1);
            else if (gen1 >= 0)          startMarker = gen1;
            else if (spec1 >= 0)         startMarker = spec1;
            else                         continue;

            // Buscar un SEGUNDO marcador 
            int gen2  = cuerpo.indexOf("GENERAL ",  startMarker + 1);
            int spec2 = cuerpo.indexOf("SPECIFIC ", startMarker + 1);
            int endMarker = -1;
            if (gen2 >= 0 && spec2 >= 0) endMarker = Math.min(gen2, spec2);
            else if (gen2 >= 0)          endMarker = gen2;
            else if (spec2 >= 0)         endMarker = spec2;

            // Quedarnos SOLO con el trozo importante
            String taskStr = (endMarker > 0)
                    ? cuerpo.substring(startMarker, endMarker).trim()
                    : cuerpo.substring(startMarker).trim();

            // Parsear la tarea
            String[] partes = taskStr.split("\\s+");
            if (partes.length < 2) continue;

            String tipo  = partes[0];
            String fecha = partes[1];
            String nombre;
            String desc;

            if ("GENERAL".equalsIgnoreCase(tipo)) {
                nombre = "";
                StringBuilder sb = new StringBuilder();
                for (int k = 2; k < partes.length; k++) {
                    if (partes[k].equals("-----")) continue;
                    sb.append(partes[k]).append(" ");
                }
                desc = sb.toString().trim();
            } else {
                nombre = partes.length > 2 ? partes[2] : "";
                StringBuilder sb = new StringBuilder();
                for (int k = 3; k < partes.length; k++) {
                    sb.append(partes[k]).append(" ");
                }
                desc = sb.toString().trim();
            }

            // Filtrar por tipo
            if (tipoFiltro == TIPO_GENERAL    && !"GENERAL".equalsIgnoreCase(tipo))  continue;
            if (tipoFiltro == TIPO_ESPECIFICA && !"SPECIFIC".equalsIgnoreCase(tipo)) continue;

            tareas.add(new String[]{tipo, fecha, nombre, desc});


        }
        return tareas;
    }

}