// Controlador que expone las operaciones como una API web: recibe las peticiones de búsqueda
//desde la interfaz gráfica, se las pide a la capa de servicio y devuelve los resultados en
// formato legible (JSON) para que la página los muestre sin recargarse por completo.

package SistemasLegados.controller;

import jakarta.annotation.PostConstruct;
import SistemasLegados.model.Programa;
import SistemasLegados.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private CatalogService catalogService;

    @PostConstruct
    public void init() {
        try {
            this.catalogService = new CatalogService();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar CatalogService", e);
        }
    }

    /** RF1: número total de registros */
    @GetMapping("/registros")
    public ResponseEntity<Map<String, Object>> totalRegistros() {
        int total = catalogService.getTotalRegistros();
        if (total < 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo leer el número de registros (OCR falló)"));
        }
        return ResponseEntity.ok(Map.of("total", total));
    }

    /** RF2: búsqueda por nombre exacto */
    @GetMapping("/programas")
    public ResponseEntity<?> buscarPorNombre(@RequestParam("nombre") String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El nombre no puede estar vacío"));
        }

        Programa programa = catalogService.buscarProgramaPorNombre(nombre.trim().toUpperCase());
        if (programa == null || programa.getNumeroRegistro() == null || programa.getNumeroRegistro() <= 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Programa no encontrado: " + nombre));
        }
        return ResponseEntity.ok(programa);
    }

    /** RF3: listado por cinta, ordenado por antigüedad */
    @GetMapping("/cintas/{cinta}/programas")
    public ResponseEntity<?> buscarPorCinta(@PathVariable("cinta") String cinta) {
        if (cinta == null || cinta.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La cinta no puede estar vacía"));
        }

        List<Programa> programas = catalogService
                .buscarProgramasOrdenadosPorCinta(cinta.trim().toUpperCase());

        // Filtrar entradas basura que a veces produce el OCR
        List<Programa> validos = programas.stream()
                .filter(p -> p.getNumeroRegistro() != null && p.getNumeroRegistro() > 0)
                .filter(p -> p.getNombre() != null && !p.getNombre().isBlank())
                .toList();

        return ResponseEntity.ok(validos);
    }
}