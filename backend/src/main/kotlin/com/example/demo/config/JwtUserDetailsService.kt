package com.example.demo.config

import com.example.demo.repositories.UsuarioRepository
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class JwtUserDetailsService(
    private val usuarioRepository: UsuarioRepository
) : UserDetailsService {

    override fun loadUserByUsername(rut: String): UserDetails {
        val usuario = usuarioRepository.findByRut(rut)
            .orElseThrow { UsernameNotFoundException("Usuario no encontrado: $rut") }

        return User(
            usuario.rut,
            usuario.contrasena,
            AuthorityUtils.createAuthorityList("ROLE_${usuario.rol}")
        )
    }
}