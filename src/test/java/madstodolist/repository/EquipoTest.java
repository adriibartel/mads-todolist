package madstodolist.repository;

import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoTest {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    public void crearEquipo() {
        Equipo equipo = new Equipo("Proyecto P1");
        assertThat(equipo.getNombre()).isEqualTo("Proyecto P1");
    }

    @Test
    @Transactional
    public void grabarYBuscarEquipo() {
        Equipo equipo = new Equipo("Proyecto P1");
        equipoRepository.save(equipo);

        Long equipoId = equipo.getId();
        assertThat(equipoId).isNotNull();

        Equipo equipoDB = equipoRepository.findById(equipoId).orElse(null);
        assertThat(equipoDB).isNotNull();
        assertThat(equipoDB.getNombre()).isEqualTo("Proyecto P1");
    }

    @Test
    public void comprobarIgualdadEquipos() {
        Equipo equipo1 = new Equipo("Proyecto P1");
        Equipo equipo2 = new Equipo("Proyecto P2");
        Equipo equipo3 = new Equipo("Proyecto P2");

        assertThat(equipo1).isNotEqualTo(equipo2);
        assertThat(equipo2).isEqualTo(equipo3);
        assertThat(equipo2.hashCode()).isEqualTo(equipo3.hashCode());

        equipo1.setId(1L);
        equipo2.setId(1L);
        equipo3.setId(2L);

        assertThat(equipo1).isEqualTo(equipo2);
        assertThat(equipo2).isNotEqualTo(equipo3);
    }

    @Test
    @Transactional
    public void comprobarRelacionBaseDatos() {
        Equipo equipo = new Equipo("Proyecto 1");
        equipoRepository.save(equipo);
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        equipo.addUsuario(usuario);

        Equipo equipoBD = equipoRepository.findById(equipo.getId()).orElse(null);
        Usuario usuarioBD = usuarioRepository.findById(usuario.getId()).orElse(null);
        assertThat(equipoBD).isNotNull();
        assertThat(usuarioBD).isNotNull();
        assertThat(equipo.getUsuarios()).hasSize(1).contains(usuario);
        assertThat(usuario.getEquipos()).hasSize(1).contains(equipo);
    }

    @Test
    @Transactional
    public void comprobarFindAll() {
        equipoRepository.save(new Equipo("Proyecto 2"));
        equipoRepository.save(new Equipo("Proyecto 3"));

        List<Equipo> equipos = equipoRepository.findAll();

        assertThat(equipos).hasSize(2);
    }
}
