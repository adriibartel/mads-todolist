# Documentación técnica de la práctica 2

Esta documentación describe la ampliación de la aplicación ToDoList para administrar cuentas. La solución conserva la separación entre entidades JPA, DTO, servicios, controladores y vistas Thymeleaf. Las funcionalidades opcionales se implementan sobre la entidad `Usuario` existente, por lo que no se introduce una entidad nueva ni se expone directamente el modelo de persistencia a las plantillas.

## Modelo y transferencia de datos

La entidad `Usuario` incorpora dos propiedades booleanas: `administrador` identifica la única cuenta con permisos de administración y `bloqueado` indica si la cuenta puede autenticarse. Ambas propiedades cuentan con sus accesores y se persisten junto con el resto de los atributos. `UsuarioData` transporta esos indicadores entre los servicios, los controladores y las vistas; `RegistroData` añade únicamente el campo necesario para enlazar el checkbox del formulario. El `ModelMapper` ya configurado realiza la conversión entre entidad y DTO.

## Alta del administrador

`UsuarioRepository.existsByAdministradorTrue()` proporciona la consulta derivada para comprobar si ya existe un administrador. `UsuarioService.existeAdministrador()` reutiliza esa consulta, y `registrar()` aplica la regla de que una solicitud de alta administrativa sólo se concede cuando todavía no existe esa cuenta. Así, la validación de la regla de negocio no depende exclusivamente de que el navegador oculte el checkbox.

En `LoginController`, `registroForm()` comunica a `formRegistro.html` si debe mostrarse el checkbox. El controlador conserva esa información también cuando hay errores de validación o de correo duplicado. Al procesar el login, consulta el DTO del usuario y envía a administradores a `/registrados`; las cuentas normales continúan dirigiéndose a sus tareas. Esta decisión está en la capa web, mientras que la asignación del rol se restringe en el servicio.

## Autorización del listado y el detalle

`HomeController` atiende las rutas `/registrados` y `/registrados/{id}`. Para ambas, primero conserva el comportamiento existente de redirigir a `/login` si no hay sesión. Después, el método privado `usuarioAdministrador()` carga el DTO de la sesión y rechaza con HTTP 403 a quien no sea administrador o ya no corresponda con una cuenta existente. La excepción incluye el motivo “No tiene permisos suficientes”. El método común evita que el listado, la descripción y las acciones administrativas apliquen reglas distintas.

## Bloqueo y autenticación

El listado `listaUsuarios.html` añade una acción POST por usuario. El texto del botón depende de `bloqueado`: permite bloquear una cuenta activa o habilitar una bloqueada. La ruta `/registrados/{id}/bloqueo` valida al administrador antes de modificar el objetivo y redirige al listado después de guardar el cambio.

`UsuarioService.alternarBloqueo()` recupera la entidad dentro de una transacción, invierte su estado y devuelve el DTO actualizado; un identificador inexistente se traduce a HTTP 404 en el controlador. En `login()`, la contraseña se comprueba antes del estado de bloqueo. Sólo unas credenciales correctas para una cuenta bloqueada producen `USER_BLOCKED`, que `LoginController` presenta en `formLogin.html` como un error explícito. Las credenciales incorrectas siguen generando el mensaje de contraseña incorrecta.

```java
} else if (usuario.get().isBloqueado()) {
    return LoginStatus.USER_BLOCKED;
}
```

La comprobación se realiza en el servicio de autenticación, no sólo al renderizar la pantalla. Por tanto, no se depende de que un usuario evite o active controles de la interfaz para aplicar el bloqueo.

## Vistas y pruebas

Las plantillas afectadas son `formRegistro.html`, `listaUsuarios.html` y `formLogin.html`; no se añaden vistas específicas para los nuevos estados. El listado existente conserva los enlaces al detalle y muestra acciones con etiquetas distintas para facilitar reconocer el estado de cada cuenta.

`UsuarioServiceTest` cubre el registro del primer administrador, la restricción del segundo, el bloqueo, el rechazo de login con cuenta bloqueada, el rechazo de contraseña incorrecta y la rehabilitación posterior. `UsuarioWebTest` comprueba la visibilidad del checkbox, las redirecciones de login, las respuestas 403 para usuarios normales, el acceso autorizado a las vistas, los botones del listado, la protección de la acción POST y el mensaje de cuenta bloqueada. Las pruebas web aíslan los controladores mediante mocks del servicio; las pruebas de servicio verifican los cambios persistidos en H2. El conjunto completo se valida con `./mvnw test`.
