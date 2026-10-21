package madstodolist.controller;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.authentication.ManagerUserSession;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    public void listadoEquiposMuestraNombreYEnlace() throws Exception {
        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Proyecto 1");
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaEquipos"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Proyecto 1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/equipos/1")));
    }

    @Test
    public void listadoEquiposOfreceCrearUnirseYSalirConSesion() throws Exception {
        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Proyecto 1");
        UsuarioData usuario = new UsuarioData();
        usuario.setId(7L);
        usuario.setNombre("Ana");
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));
        when(equipoService.equiposUsuario(7L)).thenReturn(Collections.singletonList(equipo));
        when(usuarioService.findById(7L)).thenReturn(usuario);

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ana")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Crear equipo")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Abandonar")));
    }

    @Test
    public void miembrosEquipoMuestraUsuarios() throws Exception {
        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Proyecto 1");
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        when(equipoService.recuperarEquipo(1L)).thenReturn(equipo);
        when(equipoService.usuariosEquipo(1L)).thenReturn(Arrays.asList(usuario));

        mockMvc.perform(get("/equipos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("listaUsuariosEquipo"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Proyecto 1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user@ua")));
    }

    @Test
    public void crearEquipoRedirigeAlListado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);

        mockMvc.perform(post("/equipos").param("nombre", "Proyecto 2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).crearEquipo("Proyecto 2");
    }

    @Test
    public void unirseAEquipoUsaElUsuarioAutenticado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);

        mockMvc.perform(post("/equipos/1/usuarios"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).añadirUsuarioAEquipo(1L, 7L);
    }

    @Test
    public void salirDeEquipoUsaElUsuarioAutenticado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);

        mockMvc.perform(post("/equipos/1/usuarios/salir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).eliminarUsuarioDeEquipo(1L, 7L);
    }

    @Test
    public void crearEquipoRedirigeAlLoginSiNoHaySesion() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(post("/equipos").param("nombre", "Proyecto 2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(equipoService);
    }

    @Test
    public void administradorPuedeCambiarNombreEquipo() throws Exception {
        UsuarioData administrador = new UsuarioData();
        administrador.setId(7L);
        administrador.setAdministrador(true);
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);
        when(usuarioService.findById(7L)).thenReturn(administrador);

        mockMvc.perform(post("/equipos/1/nombre").param("nombre", "Proyecto renovado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).modificarNombreEquipo(1L, "Proyecto renovado");
    }

    @Test
    public void administradorPuedeEliminarEquipo() throws Exception {
        UsuarioData administrador = new UsuarioData();
        administrador.setAdministrador(true);
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);
        when(usuarioService.findById(7L)).thenReturn(administrador);

        mockMvc.perform(post("/equipos/1/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).eliminarEquipo(1L);
    }

    @Test
    public void usuarioSinPermisosNoPuedeEliminarEquipo() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setAdministrador(false);
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);
        when(usuarioService.findById(7L)).thenReturn(usuario);

        mockMvc.perform(post("/equipos/1/eliminar"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(equipoService);
    }

    @Test
    public void administradorVeControlesDeGestionEnListado() throws Exception {
        UsuarioData administrador = new UsuarioData();
        administrador.setId(7L);
        administrador.setAdministrador(true);
        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Proyecto 1");
        when(managerUserSession.usuarioLogeado()).thenReturn(7L);
        when(usuarioService.findById(7L)).thenReturn(administrador);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));
        when(equipoService.equiposUsuario(7L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cambiar nombre")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Eliminar equipo")));
    }
}
