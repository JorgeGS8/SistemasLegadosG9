import java.io.*;
import java.util.*;

public class Mainframe3270 {

    private enum Estado {
        DESCONECTADO,
        PANTALLA_HERCULES,
        PANTALLA_LOGIN_USUARIO,
        PANTALLA_LOGIN_PASSWORD,
        PANTALLA_LOGIN_USUARIO_EN_USO,
        AUTENTICADO,
        EN_TAREAS
    }   

    private Estado estado = Estado.DESCONECTADO;

    public static final int TIPO_GENERAL    = 1;
    public static final int TIPO_ESPECIFICA = 2;
    public static final int MAX_DESCRIPCION_GENERAL = 12;
    public static final int MAX_NOMBRE_ESPECIFICA = 6;
    public static final int MAX_DESCRIPCION_ESPECIFICA = 10;

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

    private String prepararTexto(String texto) {

        if (texto == null) {
            return "";
        }

        // Escapar caracteres que puedan interferir
        texto = texto.replace("\\", "\\\\");
        texto = texto.replace("\"", "\\\"");

        // Eliminamos saltos de linea
        texto = texto.replace("\n", " ");
        texto = texto.replace("\r", " ");

        // Remplazar espacios por guiones bajos para evitar problemas con el terminal
        texto = texto.replace(" ", "_");

        return texto;
    }
    
    private String sincronizar() throws IOException {
        enviarComando("Wait(60, InputField)");
        pausa(800);

        String previa = "";
        String actual = "";
        int estables = 0;
        StringBuilder paginas = new StringBuilder();

        for (int i = 0; i < 15; i++) {
            actual = leerPantalla();

            if (tieneMoreAlFinal(actual)) {
                System.out.println("[INFO] 'More...' detectado, paso de página");
                paginas.append(actual).append("\n");
                enviarComando("Enter()");
                enviarComando("Wait(60, InputField)");
                pausa(1000);
                actual = leerPantalla();
                previa = actual;
                estables = 0;
                continue;
            }

            if (actual.equals(previa)) {
                estables++;
                if (estables >= 2) {
                    paginas.append(actual);
                    return paginas.toString();
                }
            } else {
                estables = 0;
            }
            previa = actual;
            pausa(500);
        }
        paginas.append(actual);
        return paginas.toString();
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
    public void iniciarConexion() throws IOException {
        proceso = new ProcessBuilder(
            "C:\\Program Files\\wc3270\\ws3270.exe"
        ).start();
        entrada = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
        errores = new BufferedReader(new InputStreamReader(proceso.getErrorStream()));
        salida  = new PrintWriter(new OutputStreamWriter(proceso.getOutputStream()), true);

        enviarComando("Connect(155.210.152.51:3270)");
        pausa(2000);

        estado = Estado.PANTALLA_HERCULES;

    }

    public void iniciarSesion(String usuario, String password) throws IOException, LoginException {
        prepararPantallaLogin();
        enviarUsuario(usuario);
        enviarPassword(password);
        completarInicioSesion();
    }

    // ---------------------------------------------------------------
    // Iniciar sesion, hay que dividir porque la pantalla cambia 
    // dependiendo de la fase de login y si falla en una parte continuar
    // desde ahí.
    // ---------------------------------------------------------------
    public void prepararPantallaLogin() throws IOException {
        // Pantalla Hércules
        enviarComando("Enter()");
        sincronizar();

        estado = Estado.PANTALLA_LOGIN_USUARIO;
    }

    public void enviarUsuario(String usuario) throws IOException, LoginException {
        String pantalla = leerPantalla();
        System.out.println("=== PANTALLA LOGIN ===");
        System.out.println(pantalla);

        // Usuario
        enviarComando("String(" + prepararTexto(usuario) + ")");
        enviarComando("Enter()");
        sincronizar();

        pantalla = leerPantalla();
        if (pantalla.contains("Userid is not authorized")) {
            throw new LoginException(
                LoginException.Tipo.USUARIO_NO_AUTORIZADO,
                "Usuario no autorizado"
            );
        } else if (pantalla.contains("Userid is incorrect")) {
            throw new LoginException(
                LoginException.Tipo.USUARIO_INCORRECTO,
                "Usuario es incorrecto"
            );
        }

        System.out.println("=== PANTALLA PASSWORD ===");
        System.out.println(pantalla);

        estado = Estado.PANTALLA_LOGIN_PASSWORD;
    }

    public void enviarPassword(String password) throws IOException, LoginException {

        // Password
        enviarComando("String(" + prepararTexto(password) + ")");
        enviarComando("Enter()");
        sincronizar();

        String pantalla = leerPantalla();

        if (pantalla.contains("Password incorrect")) {
            throw new LoginException(
                LoginException.Tipo.CONTRASENA_INCORRECTA,
                "Contraseña incorrecta"
            );
        } else if (pantalla.contains("Userid is in use")) {
            throw new LoginException(
                LoginException.Tipo.USUARIO_EN_USO,
                "Usuario en uso"
            );
        }

        System.out.println("=== PANTALLA POST-LOGIN ===");
        System.out.println(pantalla);
        if (pantalla.contains("Press ENTER")) {
            enviarComando("Enter()");
            sincronizar();
        }

        estado = Estado.AUTENTICADO;
    }

    public void completarInicioSesion() throws IOException {

        String pantalla = leerPantalla();
        System.out.println("=== PANTALLA MENU MUSIC ===");
        System.out.println(pantalla);

        // Lanzar tareas.c por lo q sea no es tasks.c
        enviarComando("String(tareas.c)");
        enviarComando("Enter()");
        sincronizar();

        pantalla = leerPantalla();
        System.out.println("=== MENÚ PRINCIPAL TAREAS ===");
        System.out.println(pantalla);

        estado = Estado.EN_TAREAS;
    }

    public void volverAlHercules() throws IOException {
        switch (estado) {
            case PANTALLA_LOGIN_USUARIO:
                // F3
                enviarComando("PF(3)");
                estado = Estado.PANTALLA_HERCULES;
                break;
            case PANTALLA_LOGIN_PASSWORD:
                // F3
                enviarComando("PF(3)");
                estado = Estado.PANTALLA_HERCULES;
                break;
            case PANTALLA_LOGIN_USUARIO_EN_USO:
                // F3
                enviarComando("PF(3)");
                estado = Estado.PANTALLA_HERCULES;
                break;
            default:
                // No hacer nada
        }
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
        // FIXME
        String temp3 = sincronizar();
        System.out.println("=== SALIDA A MAIN MENU ===");
        System.out.println(temp3);

        return todas;
    }



    // ---------------------------------------------------------------
    // Crear tareica
    // ---------------------------------------------------------------
    public void crearTarea(int tipo, String fecha, String nombre, String descripcion) throws IOException {
        // Validar datos antes de enviar comandos
        validarDatosTarea(tipo, fecha, nombre, descripcion);

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
            if (nombre != null && !nombre.isEmpty()) enviarComando("String(\"" + prepararTexto(nombre) + "\")");
            enviarComando("Enter()");
            sincronizar();
            pausa(800);
        }

        if (descripcion != null && !descripcion.isEmpty()) enviarComando("String(\"" + prepararTexto(descripcion) + "\")");
        enviarComando("Enter()");
        sincronizar();
        pausa(1200);

        enviarComando("String(3)");
        enviarComando("Enter()");
        sincronizar();
    }

    private void validarDatosTarea(int tipo, String fecha, String nombre, String descripcion) {
        if (tipo != TIPO_GENERAL && tipo != TIPO_ESPECIFICA) {
            throw new IllegalArgumentException("Tipo de tarea no válido.");
        }

        if (fecha == null || fecha.isEmpty()) {
            throw new IllegalArgumentException("La fecha no puede estar vacía.");
        }

        if (descripcion == null) {
            throw new IllegalArgumentException("La descripción no puede ser nula.");
        }

        int limiteDescripcion = tipo == TIPO_GENERAL
                ? MAX_DESCRIPCION_GENERAL
                : MAX_DESCRIPCION_ESPECIFICA;
        if (descripcion.length() > limiteDescripcion) {
            throw new IllegalArgumentException(
                "La descripción no puede superar los " + limiteDescripcion + " caracteres."
            );
        }

        if (tipo == TIPO_ESPECIFICA
                && nombre != null
                && nombre.length() > MAX_NOMBRE_ESPECIFICA) {
            throw new IllegalArgumentException(
                "El nombre no puede superar los " + MAX_NOMBRE_ESPECIFICA + " caracteres."
            );
        }
    }

    
    public void salir() throws IOException {

    if (proceso == null || !proceso.isAlive()) {
        return;
    }

    try {
        enviarComando("String(0)");
        enviarComando("Enter()");

        // Damos tiempo al mainframe a procesar la salida
        pausa(1000);

    } finally {
        proceso.destroy();
    }
}


    // ---------------------------------------------------------------
    // Parseo del listado de tareas, quedándose con el tipo pedido.
    // ---------------------------------------------------------------
    private List<String[]> parsearTareas(String pantalla, int tipoFiltro) {
        List<String[]> tareas = new ArrayList<>();
        String[] lineas = pantalla.split("\n");

        // TOTAL TASK marca el final del listado. Las páginas anteriores se
        // encuentran antes de ese marcador en la pantalla acumulada.
        int finBloque = -1;
        for (int i = lineas.length - 1; i >= 0; i--) {
            if (lineas[i].contains("TOTAL TASK")) {
                finBloque = i;
                break;
            }
        }
        if (finBloque == -1) return tareas;

        // Buscar la primera tarea del listado acumulado. No se busca hacia
        // atrás desde TOTAL TASK porque las páginas están separadas por More...
        int iniBloque = -1;
        for (int i = 0; i < finBloque; i++) {
            if (lineas[i].trim().startsWith("TASK ")) {
                iniBloque = i;
                break;
            }
        }
        if (iniBloque == -1) return tareas;

        // Recorrer todas las páginas hasta el TOTAL TASK final.
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

            // El sistema recibe los espacios como "_"; se muestran como espacios en la aplicación.
            taskStr = taskStr.replace('_', ' ');

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