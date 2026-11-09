package SistemasLegados;

import SistemasLegados.model.Programa;
import SistemasLegados.service.CatalogService;

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
            List<Programa> programas = servicio.buscarProgramasOrdenadosPorCinta("1");
            System.out.println("Programas encontrados en cinta A:");
            for (Programa programa : programas) {
                System.out.println(programa + "\n");
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}