// Controlador que atiende la navegación del usuario. Sirve la página web principal y sus vistas HTML.

package SistemasLegados.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegacyWebController {

    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }
}