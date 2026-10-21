package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/equipos")
    public String listadoEquipos(Model model) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) {
            return "redirect:/login";
        }
        UsuarioData usuario = usuarioService.findById(usuarioId);
        model.addAttribute("usuario", usuario);
        model.addAttribute("equipos", equipoService.findAllOrdenadoPorNombre());
        return "listaEquipos";
    }

    @GetMapping("/equipos/{id}/usuarios")
    public String usuariosEquipo(@PathVariable Long id, Model model) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) {
            return "redirect:/login";
        }
        EquipoData equipo;
        try {
            equipo = equipoService.recuperarEquipo(id);
        } catch (EquipoServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        model.addAttribute("usuario", usuarioService.findById(usuarioId));
        model.addAttribute("equipo", equipo);
        model.addAttribute("usuarios", equipoService.usuariosEquipo(id));
        return "usuariosEquipo";
    }
}
