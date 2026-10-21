package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EquipoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipoService equipoService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void listadoEquiposRedirigeSiNoHaySesion() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/equipos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void listadoEquiposMuestraEquiposOrdenadosYEnlacesAMiembros() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("Ana");
        EquipoData equipoA = new EquipoData();
        equipoA.setId(2L);
        equipoA.setNombre("Alpha");
        EquipoData equipoB = new EquipoData();
        equipoB.setId(1L);
        equipoB.setNombre("Beta");
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Arrays.asList(equipoA, equipoB));
        when(equipoService.equiposUsuario(1L)).thenReturn(Collections.singletonList(equipoB));

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaEquipos"))
                .andExpect(content().string(containsString("Alpha")))
                .andExpect(content().string(containsString("Beta")))
                .andExpect(content().string(containsString("/equipos/2/usuarios")))
                .andExpect(content().string(containsString("/equipos/1/usuarios")))
                .andExpect(content().string(stringContainsInOrder("Alpha", "Beta")))
                .andExpect(content().string(containsString("Abandonar")))
                .andExpect(content().string(containsString("Unirme")))
                .andExpect(content().string(containsString("Crear equipo")));
    }

    @Test
    public void crearEquipoRegistraNombreYRedirigeAlListado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(5L);

        mockMvc.perform(post("/equipos").param("nombre", "Proyecto nuevo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).crearEquipo("Proyecto nuevo");
    }

    @Test
    public void administradorPuedeCambiarNombreDeEquipo() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);
        when(usuarioService.findById(7L)).thenReturn(usuario(7L, true));

        mockMvc.perform(post("/equipos/4/nombre").param("nombre", "Proyecto renovado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).cambiarNombreEquipo(4L, "Proyecto renovado");
    }

    @Test
    public void administradorPuedeEliminarEquipo() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);
        when(usuarioService.findById(7L)).thenReturn(usuario(7L, true));

        mockMvc.perform(post("/equipos/4/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).eliminarEquipo(4L);
    }

    @Test
    public void usuarioNoAdministradorNoPuedeGestionarEquipos() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(8L);
        when(usuarioService.findById(8L)).thenReturn(usuario(8L, false));

        mockMvc.perform(post("/equipos/4/nombre").param("nombre", "No permitido"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/equipos/4/eliminar"))
                .andExpect(status().isForbidden());

        verify(equipoService, never()).cambiarNombreEquipo(4L, "No permitido");
        verify(equipoService, never()).eliminarEquipo(4L);
    }

    @Test
    public void accionesDeGestionRedirigenALoginSiNoHaySesion() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(post("/equipos/4/nombre").param("nombre", "Proyecto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(post("/equipos/4/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void administracionDeEquipoDevuelve404SiNoExiste() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);
        when(usuarioService.findById(7L)).thenReturn(usuario(7L, true));
        doThrow(new EquipoServiceException("No existe el equipo 99"))
                .when(equipoService).cambiarNombreEquipo(99L, "Proyecto");
        doThrow(new EquipoServiceException("No existe el equipo 99"))
                .when(equipoService).eliminarEquipo(99L);

        mockMvc.perform(post("/equipos/99/nombre").param("nombre", "Proyecto"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/equipos/99/eliminar"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void listaEquiposMuestraAccionesDeGestionSoloAlAdministrador() throws Exception {
        EquipoData equipo = new EquipoData();
        equipo.setId(4L);
        equipo.setNombre("Alpha");
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);
        when(usuarioService.findById(7L)).thenReturn(usuario(7L, true));
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));
        when(equipoService.equiposUsuario(7L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/equipos/4/nombre")))
                .andExpect(content().string(containsString("/equipos/4/eliminar")));
    }

    @Test
    public void usuarioPuedeUnirseAlEquipoPropioDeLaSesion() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(5L);

        mockMvc.perform(post("/equipos/2/miembros"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).añadirUsuarioAEquipo(2L, 5L);
    }

    @Test
    public void usuarioPuedeAbandonarElEquipoPropioDeLaSesion() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(5L);

        mockMvc.perform(post("/equipos/2/miembros/salir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).quitarUsuarioDeEquipo(2L, 5L);
    }

    @Test
    public void accionesDePertenenciaRedirigenALoginSiNoHaySesion() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(post("/equipos/2/miembros"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(post("/equipos/2/miembros/salir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(post("/equipos").param("nombre", "Proyecto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void miembrosEquipoRedirigeSiNoHaySesion() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/equipos/1/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void miembrosEquipoDevuelve404SiNoExisteEquipo() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(equipoService.recuperarEquipo(99L))
                .thenThrow(new EquipoServiceException("No existe el equipo 99"));

        mockMvc.perform(get("/equipos/99/usuarios"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void miembrosEquipoMuestraParticipantes() throws Exception {
        UsuarioData usuarioSesion = new UsuarioData();
        usuarioSesion.setId(1L);
        usuarioSesion.setNombre("Ana");
        UsuarioData miembro = new UsuarioData();
        miembro.setId(3L);
        miembro.setEmail("miembro@example.com");
        miembro.setNombre("Miembro");
        EquipoData equipo = new EquipoData();
        equipo.setId(2L);
        equipo.setNombre("Alpha");
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuarioSesion);
        when(equipoService.recuperarEquipo(2L)).thenReturn(equipo);
        when(equipoService.usuariosEquipo(2L)).thenReturn(Collections.singletonList(miembro));

        mockMvc.perform(get("/equipos/2/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuariosEquipo"))
                .andExpect(content().string(containsString("Miembros de Alpha")))
                .andExpect(content().string(containsString("miembro@example.com")))
                .andExpect(content().string(containsString("Miembro")));
    }

    private UsuarioData usuario(Long id, boolean administrador) {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(id);
        usuario.setAdministrador(administrador);
        usuario.setNombre("Usuario");
        return usuario;
    }
}
