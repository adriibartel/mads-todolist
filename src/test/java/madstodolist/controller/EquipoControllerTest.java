package madstodolist.controller;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EquipoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipoService equipoService;

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
}
