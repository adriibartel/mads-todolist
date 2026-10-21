package madstodolist.controller;

import madstodolist.service.EquipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @GetMapping("/equipos")
    public String listadoEquipos(Model model) {
        model.addAttribute("equipos", equipoService.findAllOrdenadoPorNombre());
        return "listaEquipos";
    }

    @GetMapping("/equipos/{id}")
    public String usuariosEquipo(@PathVariable Long id, Model model) {
        model.addAttribute("equipo", equipoService.recuperarEquipo(id));
        model.addAttribute("usuarios", equipoService.usuariosEquipo(id));
        return "listaUsuariosEquipo";
    }
}
