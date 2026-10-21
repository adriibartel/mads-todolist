package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        model.addAttribute("equipos", equipoService.findAllOrdenadoPorNombre());
        cargarUsuarioSesion(model);
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId != null) {
            model.addAttribute("equiposUsuario", equipoService.equiposUsuario(usuarioId));
        }
        return "listaEquipos";
    }

    @GetMapping("/equipos/{id}")
    public String usuariosEquipo(@PathVariable Long id, Model model) {
        model.addAttribute("equipo", equipoService.recuperarEquipo(id));
        model.addAttribute("usuarios", equipoService.usuariosEquipo(id));
        cargarUsuarioSesion(model);
        return "listaUsuariosEquipo";
    }

    @PostMapping("/equipos")
    public String crearEquipo(@RequestParam String nombre) {
        if (managerUserSession.usuarioLogeado() == null) return "redirect:/login";
        equipoService.crearEquipo(nombre);
        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/usuarios")
    public String unirseEquipo(@PathVariable Long id) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) return "redirect:/login";
        equipoService.añadirUsuarioAEquipo(id, usuarioId);
        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/usuarios/salir")
    public String salirEquipo(@PathVariable Long id) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) return "redirect:/login";
        equipoService.eliminarUsuarioDeEquipo(id, usuarioId);
        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/nombre")
    public String modificarNombreEquipo(@PathVariable Long id, @RequestParam String nombre) {
        if (!comprobarAdministrador()) return "redirect:/login";
        equipoService.modificarNombreEquipo(id, nombre);
        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/eliminar")
    public String eliminarEquipo(@PathVariable Long id) {
        if (!comprobarAdministrador()) return "redirect:/login";
        equipoService.eliminarEquipo(id);
        return "redirect:/equipos";
    }

    private void cargarUsuarioSesion(Model model) {
        model.addAttribute("administrador", false);
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId != null) {
            UsuarioData usuario = usuarioService.findById(usuarioId);
            model.addAttribute("usuario", usuario);
            model.addAttribute("administrador", usuario != null && usuario.isAdministrador());
        }
    }

    private boolean comprobarAdministrador() {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) return false;
        UsuarioData usuario = usuarioService.findById(usuarioId);
        if (usuario == null || !usuario.isAdministrador()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return true;
    }
}
