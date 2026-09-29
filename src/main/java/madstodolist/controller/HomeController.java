package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class HomeController {

    @Autowired
    ManagerUserSession managerUserSession;

    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/about")
    public String about(Model model) {
        cargarUsuarioSesion(model);
        return "about";
    }

    @GetMapping("/registrados")
    public String usuariosRegistrados(Model model) {
        cargarUsuarioSesion(model);
        model.addAttribute("usuarios", usuarioService.findAll());
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable Long id, Model model) {
        cargarUsuarioSesion(model);
        UsuarioData usuario = usuarioService.findById(id);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("usuarioDescrito", usuario);
        return "descripcionUsuario";
    }

    private void cargarUsuarioSesion(Model model) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId != null) {
            UsuarioData usuario = usuarioService.findById(usuarioId);
            model.addAttribute("usuario", usuario);
        }
    }

}