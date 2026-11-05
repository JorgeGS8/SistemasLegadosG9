package main.java.SistemasLegados;

import main.java.SistemasLegados.model.Programa;
import main.java.SistemasLegados.service.CatalogService;

import java.util.List;

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
            //servicio.buscarProgramasOrdenadosPorCinta("A");
            List<Programa> programas = servicio.buscarProgramasOrdenadosPorCinta("A");
            System.out.println("Programas encontrados en cinta A:");
            for (Programa programa : programas) {
                System.out.println(programa + "\n");
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}