// TODO
// RF
//  1. Solicita la consulta del contador total de registros.
//  2. Gestiona la búsqueda de un programa por su nombre exacto.
//  3. Gestiona la consulta de una cinta concreta, ordenando los
// programas según su orden de grabación y aplicando la regla de
// cintas múltiples (por ejemplo, reconocer que un programa en "A-45"
// pertenece tanto a la cinta "A" como a la "45").


package main.java.SistemasLegados.service;

import main.java.SistemasLegados.model.Programa;
import main.java.SistemasLegados.wrapper.LegacyAppBridge;

public class CatalogService {

    private LegacyAppBridge bridge;

    public CatalogService() throws Exception {
        this.bridge = new LegacyAppBridge();
    }

    public int getTotalRegistros() {
        return bridge.obtenerNumeroRegistros();
    }

    public Programa buscarProgramaPorNombre(String nombre) {
        return bridge.buscarProgramaPorNombre(nombre);
    }
}