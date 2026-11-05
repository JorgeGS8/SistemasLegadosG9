package main.java.SistemasLegados;

import main.java.SistemasLegados.model.Programa;
import main.java.SistemasLegados.service.CatalogService;

public class Main {
    public static void main(String[] args) {
        try {
            //Debug para ver como funciona
            System.out.println("Iniciar pruebecilla");
            CatalogService servicio = new CatalogService();
            //int total = servicio.getTotalRegistros();
            //System.out.println("Número de registros: " + total);
            //Programa programa = servicio.buscarProgramaPorNombre("COMBAT ZONE");
            //System.out.println("Programa encontrado: " + programa);
            servicio.buscarProgramasOrdenadosPorCinta("A");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}