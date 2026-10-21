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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);
        model.addAttribute("equiposUsuario", equipoService.equiposUsuario(usuarioId));
        return "listaEquipos";
    }

    @PostMapping("/equipos")
    public String crearEquipo(@RequestParam String nombre) {
        if (managerUserSession.usuarioLogeado() == null) {
            return "redirect:/login";
        }
        equipoService.crearEquipo(nombre);
        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/miembros")
    public String unirseAEquipo(@PathVariable Long id) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) {
            return "redirect:/login";
        }
        cambiarPertenencia(id, usuarioId, true);
        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/miembros/salir")
    public String salirDeEquipo(@PathVariable Long id) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) {
            return "redirect:/login";
        }
        cambiarPertenencia(id, usuarioId, false);
        return "redirect:/equipos";
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

    private void cambiarPertenencia(Long equipoId, Long usuarioId, boolean unirse) {
        try {
            if (unirse) {
                equipoService.añadirUsuarioAEquipo(equipoId, usuarioId);
            } else {
                equipoService.quitarUsuarioDeEquipo(equipoId, usuarioId);
            }
        } catch (EquipoServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}
