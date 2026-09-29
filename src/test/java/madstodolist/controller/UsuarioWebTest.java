package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
        UsuarioData juan = new UsuarioData();
        juan.setId(2L);
        juan.setEmail("juan@example.com");
        when(usuarioService.findAll()).thenReturn(Arrays.asList(ana, juan));

        mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuarios registrados")))
                .andExpect(content().string(containsString("Identificador")))
                .andExpect(content().string(containsString("Correo electrónico")))
                .andExpect(content().string(containsString("ana@example.com")))
                .andExpect(content().string(containsString("juan@example.com")))
                .andExpect(content().string(containsString("/registrados/1")))
                .andExpect(content().string(containsString("/registrados/2")))
                .andExpect(content().string(containsString("Iniciar sesión")))
                .andExpect(content().string(containsString("Registrarse")));
    }

    @Test
    public void descripcionUsuarioMuestraTodosLosDatosSalvoLaContrasena() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(3L);
        usuario.setEmail("usuario@example.com");
        usuario.setNombre("Usuario de prueba");
        usuario.setPassword("secreto");
        usuario.setFechaNacimiento(new SimpleDateFormat("yyyy-MM-dd").parse("1990-05-15"));
        when(usuarioService.findById(3L)).thenReturn(usuario);

        mockMvc.perform(get("/registrados/3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Identificador")))
                .andExpect(content().string(containsString(">3</dd>")))
                .andExpect(content().string(containsString("usuario@example.com")))
                .andExpect(content().string(containsString("Usuario de prueba")))
                .andExpect(content().string(containsString("15/05/1990")))
                .andExpect(content().string(not(containsString("secreto"))));
    }

    @Test
    public void descripcionUsuarioInexistenteDevuelve404() throws Exception {
        when(usuarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/registrados/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void listaUsuariosRegistradosMantieneNavbarParaUsuarioAutenticado() throws Exception {
        UsuarioData ana = new UsuarioData();
        ana.setId(7L);
        ana.setEmail("ana@example.com");
        ana.setNombre("Ana García");
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
}
