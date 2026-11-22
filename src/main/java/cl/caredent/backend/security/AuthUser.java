package cl.caredent.backend.security;

import cl.caredent.backend.model.Rol;

/** Usuario autenticado que viaja en el SecurityContext durante cada request. */
public record AuthUser(Long id, String usuario, String nombre, Rol rol) {}
