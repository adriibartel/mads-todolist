package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

    private void cargarUsuarioSesion(Model model) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId != null) {
            UsuarioData usuario = usuarioService.findById(usuarioId);
            model.addAttribute("usuario", usuario);
        }
    }

}