package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        Equipo equipo = new Equipo(nombre);
        return modelMapper.map(equipoRepository.save(equipo), EquipoData.class);
    }

    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo " + id));
        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public List<EquipoData> findAllOrdenadoPorNombre() {
        return equipoRepository.findAll().stream()
                .sorted(Comparator.comparing(Equipo::getNombre))
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void añadirUsuarioAEquipo(Long idEquipo, Long idUsuario) {
        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo " + idEquipo));
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EquipoServiceException("No existe el usuario " + idUsuario));
        equipo.addUsuario(usuario);
    }

    @Transactional
    public void eliminarUsuarioDeEquipo(Long idEquipo, Long idUsuario) {
        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo " + idEquipo));
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EquipoServiceException("No existe el usuario " + idUsuario));
        equipo.removeUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo " + id));
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipoData> equiposUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EquipoServiceException("No existe el usuario " + idUsuario));
        return usuario.getEquipos().stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .sorted(Comparator.comparing(EquipoData::getNombre))
                .collect(Collectors.toList());
    }

    @Transactional
    public void modificarNombreEquipo(Long id, String nombre) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo " + id));
        equipo.setNombre(nombre);
    }

    @Transactional
    public void eliminarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo " + id));
        equipoRepository.delete(equipo);
    }
}
