package com.isf.guatemala.controller;

import com.isf.guatemala.model.Usuario;
import com.isf.guatemala.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Usuario> usuario = usuarioRepository.findByUsername(request.getUsername());
        
        if (usuario.isPresent() && usuario.get().getPassword().equals(request.getPassword())) {
            LoginResponse response = new LoginResponse();
            response.setUsername(usuario.get().getUsername());
            response.setRol(usuario.get().getRol().toString());
            response.setSuccess(true);
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }
    
    static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    static class LoginResponse {
        private String username;
        private String rol;
        private boolean success;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRol() { return rol; }
        public void setRol(String rol) { this.rol = rol; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}