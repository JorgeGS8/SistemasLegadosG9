// TODO
// RF
//  1. Solicita la consulta del contador total de registros.
//  2. Gestiona la búsqueda de un programa por su nombre exacto.
//  3. Gestiona la consulta de una cinta concreta, ordenando los
// programas según su orden de grabación y aplicando la regla de
// cintas múltiples (por ejemplo, reconocer que un programa en "A-45"
// pertenece tanto a la cinta "A" como a la "45").


package SistemasLegados.service;

import SistemasLegados.model.Programa;
import SistemasLegados.wrapper.LegacyAppBridge;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CatalogService {

    private LegacyAppBridge bridge;

    public CatalogService() throws Exception {
        this.bridge = new LegacyAppBridge();
    }

    public List<Programa> ordenarProgramasPorAntiguedad(List<Programa> programas) {
        return programas.stream()
                .sorted(Comparator.comparing(Programa::getNumeroRegistro))
                .toList();
    }

    public int getTotalRegistros() {
        return bridge.obtenerNumeroRegistros();
    }

    public Programa buscarProgramaPorNombre(String nombre) {
        return bridge.buscarProgramaPorNombre(nombre);
    }

    public List<Programa> filtrarProgramasPorCinta(List<Programa> programas, String cintaBuscada) {
        if (cintaBuscada == null || cintaBuscada.isEmpty()) {
            throw new IllegalArgumentException("La cinta buscada no puede ser nula o vacía.");
        }

        String objetivo = cintaBuscada.toUpperCase();

        return programas.stream()
                .filter(p -> p.getCinta() != null && perteneceACinta(p.getCinta(), objetivo))
                .toList();
    }

    private boolean perteneceACinta(String cintaRegistro, String cintaBuscada) {
        // Si la cinta es "A-45", split("-") genera ["A", "45"]
        // Si la cinta es "18", split("-") genera ["18"]
        String[] cintas = cintaRegistro.split("-");

        return Arrays.stream(cintas)
                .map(String::trim)
                .anyMatch(c -> c.equalsIgnoreCase(cintaBuscada));
    }

    public List<Programa> buscarProgramasOrdenadosPorCinta(String cintaBuscada) {
        List<Programa> programas = bridge.leerTodosLosProgramas();
        programas = filtrarProgramasPorCinta(programas, cintaBuscada);
        return ordenarProgramasPorAntiguedad(programas);
    }

    public List<Programa> leerTodosLosProgramas(String cintaBuscada) {
        List<Programa> programas = bridge.leerTodosLosProgramas();
        return ordenarProgramasPorAntiguedad(programas);
    }
}