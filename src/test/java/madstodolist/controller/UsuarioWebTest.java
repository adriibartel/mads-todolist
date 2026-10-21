package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
//
// A diferencia de los tests web de tarea, donde usábamos los datos
// de prueba de la base de datos, aquí vamos a practicar otro enfoque:
// moquear el usuarioService.
public class UsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Moqueamos el usuarioService.
    // En los tests deberemos proporcionar el valor devuelto por las llamadas
    // a los métodos de usuarioService que se van a ejecutar cuando se realicen
    // las peticiones a los endpoint.
    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void listaUsuariosRegistrados() throws Exception {
        UsuarioData ana = new UsuarioData();
        ana.setId(1L);
        ana.setEmail("ana@example.com");
        ana.setAdministrador(true);
        UsuarioData juan = new UsuarioData();
        juan.setId(2L);
        juan.setEmail("juan@example.com");
        juan.setBloqueado(true);
        when(usuarioService.findById(1L)).thenReturn(ana);
        when(usuarioService.findAll()).thenReturn(Arrays.asList(ana, juan));

        mockMvc.perform(get("/registrados").sessionAttr("idUsuarioLogeado", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuarios registrados")))
                .andExpect(content().string(containsString("Identificador")))
                .andExpect(content().string(containsString("Correo electrónico")))
                .andExpect(content().string(containsString("ana@example.com")))
                .andExpect(content().string(containsString("juan@example.com")))
                .andExpect(content().string(containsString("/registrados/1")))
                .andExpect(content().string(containsString("/registrados/2")))
                .andExpect(content().string(containsString("Bloquear")))
                .andExpect(content().string(containsString("Habilitar")));
    }

    @Test
    public void listaUsuariosRegistradosRedirigeSiNoHayUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/registrados"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void listaUsuariosRegistradosDevuelve403SiElUsuarioNoEsAdministrador() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setAdministrador(false);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(get("/registrados").sessionAttr("idUsuarioLogeado", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    public void descripcionUsuarioMuestraTodosLosDatosSalvoLaContrasena() throws Exception {
        UsuarioData usuarioSesion = new UsuarioData();
        usuarioSesion.setId(7L);
        usuarioSesion.setAdministrador(true);
        UsuarioData usuario = new UsuarioData();
        usuario.setId(3L);
        usuario.setEmail("usuario@example.com");
        usuario.setNombre("Usuario de prueba");
        usuario.setPassword("secreto");
        usuario.setFechaNacimiento(new SimpleDateFormat("yyyy-MM-dd").parse("1990-05-15"));
        when(usuarioService.findById(7L)).thenReturn(usuarioSesion);
        when(usuarioService.findById(3L)).thenReturn(usuario);

        mockMvc.perform(get("/registrados/3").sessionAttr("idUsuarioLogeado", 7L))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Identificador")))
                .andExpect(content().string(containsString(">3</dd>")))
                .andExpect(content().string(containsString("usuario@example.com")))
                .andExpect(content().string(containsString("Usuario de prueba")))
                .andExpect(content().string(containsString("15/05/1990")))
                .andExpect(content().string(not(containsString("secreto"))));
    }

    @Test
    public void descripcionUsuarioRedirigeSiNoHayUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/registrados/3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void descripcionUsuarioDevuelve403SiElUsuarioNoEsAdministrador() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(7L);
        usuario.setAdministrador(false);
        when(usuarioService.findById(7L)).thenReturn(usuario);

        mockMvc.perform(get("/registrados/3").sessionAttr("idUsuarioLogeado", 7L))
                .andExpect(status().isForbidden());
    }

    @Test
    public void administradorPuedeAlternarBloqueoDeUsuario() throws Exception {
        UsuarioData administrador = new UsuarioData();
        administrador.setId(1L);
        administrador.setAdministrador(true);
        when(usuarioService.findById(1L)).thenReturn(administrador);
        when(usuarioService.alternarBloqueo(2L)).thenReturn(new UsuarioData());

        mockMvc.perform(post("/registrados/2/bloqueo").sessionAttr("idUsuarioLogeado", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));

        verify(usuarioService).alternarBloqueo(2L);
    }

    @Test
    public void usuarioNoAdministradorNoPuedeAlternarBloqueo() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setAdministrador(false);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(post("/registrados/2/bloqueo").sessionAttr("idUsuarioLogeado", 1L))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).alternarBloqueo(2L);
    }

    @Test
    public void descripcionUsuarioInexistenteDevuelve404() throws Exception {
        UsuarioData usuarioSesion = new UsuarioData();
        usuarioSesion.setId(7L);
        usuarioSesion.setAdministrador(true);
        when(usuarioService.findById(7L)).thenReturn(usuarioSesion);
        when(usuarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/registrados/99").sessionAttr("idUsuarioLogeado", 7L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void listaUsuariosRegistradosMantieneNavbarParaUsuarioAutenticado() throws Exception {
        UsuarioData ana = new UsuarioData();
        ana.setId(7L);
        ana.setEmail("ana@example.com");
        ana.setNombre("Ana García");
        ana.setAdministrador(true);
        when(usuarioService.findById(7L)).thenReturn(ana);
        when(usuarioService.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/registrados").sessionAttr("idUsuarioLogeado", 7L))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana García")))
                .andExpect(content().string(containsString("/usuarios/7/tareas")))
                .andExpect(content().string(containsString("Cerrar sesión")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Iniciar sesión"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Registrarse"))));
    }

    @Test
    public void servicioLoginUsuarioOK() throws Exception {
        // GIVEN
        // Moqueamos la llamada a usuarioService.login para que
        // devuelva un LOGIN_OK y la llamada a usuarioServicie.findByEmail
        // para que devuelva un usuario determinado.

        UsuarioData anaGarcia = new UsuarioData();
        anaGarcia.setNombre("Ana García");
        anaGarcia.setId(1L);
        anaGarcia.setAdministrador(false);

        when(usuarioService.login("ana.garcia@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("ana.garcia@gmail.com"))
                .thenReturn(anaGarcia);

        // WHEN, THEN
        // Realizamos una petición POST al login pasando los datos
        // esperados en el mock, la petición devolverá una redirección a la
        // URL con las tareas del usuario

        this.mockMvc.perform(post("/login")
                        .param("eMail", "ana.garcia@gmail.com")
                        .param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/tareas"));
    }

    @Test
    public void servicioLoginAdministradorRedirigeAlListadoDeUsuarios() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setAdministrador(true);
        when(usuarioService.login("admin@example.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("admin@example.com")).thenReturn(admin);

        mockMvc.perform(post("/login")
                        .param("eMail", "admin@example.com")
                        .param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));
    }

    @Test
    public void formularioRegistroMuestraCheckboxAdministradorCuandoAunNoExisteAdmin() throws Exception {
        when(usuarioService.existeAdministrador()).thenReturn(false);

        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrarse como administrador")));
    }

    @Test
    public void formularioRegistroOcultaCheckboxAdministradorSiYaExisteAdmin() throws Exception {
        when(usuarioService.existeAdministrador()).thenReturn(true);

        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Registrarse como administrador"))));
    }

    @Test
    public void formularioRegistroMantieneCheckboxAlMostrarErroresDeValidacion() throws Exception {
        when(usuarioService.existeAdministrador()).thenReturn(false);

        mockMvc.perform(post("/registro")
                        .param("email", "correo-invalido")
                        .param("password", "123")
                        .param("administrador", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("formRegistro"))
                .andExpect(content().string(containsString("Registrarse como administrador")));
    }

    @Test
    public void registroEnvioCheckboxAdministrador() throws Exception {
        when(usuarioService.existeAdministrador()).thenReturn(false);

        mockMvc.perform(post("/registro")
                        .param("email", "admin@example.com")
                        .param("password", "123")
                        .param("administrador", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        ArgumentCaptor<UsuarioData> usuarioCaptor = ArgumentCaptor.forClass(UsuarioData.class);
        verify(usuarioService).registrar(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().isAdministrador()).isTrue();
    }

    @Test
    public void servicioLoginUsuarioNotFound() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // USER_NOT_FOUND
        when(usuarioService.login("pepito.perez@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_NOT_FOUND);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "No existe usuario"
        this.mockMvc.perform(post("/login")
                        .param("eMail","pepito.perez@gmail.com")
                        .param("password","12345678"))
                .andExpect(content().string(containsString("No existe usuario")));
    }

    @Test
    public void servicioLoginUsuarioErrorPassword() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // ERROR_PASSWORD
        when(usuarioService.login("ana.garcia@gmail.com", "000"))
                .thenReturn(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "Contraseña incorrecta"
        this.mockMvc.perform(post("/login")
                        .param("eMail","ana.garcia@gmail.com")
                        .param("password","000"))
                .andExpect(content().string(containsString("Contraseña incorrecta")));
    }

    @Test
    public void servicioLoginUsuarioBloqueadoMuestraError() throws Exception {
        when(usuarioService.login("bloqueado@example.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_BLOCKED);

        mockMvc.perform(post("/login")
                        .param("eMail", "bloqueado@example.com")
                        .param("password", "12345678"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El usuario está bloqueado")));
    }
}
