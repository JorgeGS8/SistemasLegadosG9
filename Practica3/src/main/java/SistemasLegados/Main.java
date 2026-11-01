package main.java.SistemasLegados;

import main.java.SistemasLegados.service.CatalogService;

public class Main {
    public static void main(String[] args) {
        try {
            //Debug para ver como funciona
            System.out.println("Iniciar pruebecilla");
            CatalogService servicio = new CatalogService();
            int total = servicio.getTotalRegistros();
            System.out.println("úmero de registros: " + total);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}