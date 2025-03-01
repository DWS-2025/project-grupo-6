package es.web.xpressaly;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private User user;

    public UserService() {
        // Creamos un usuario único y lo guardamos en memoria.
        user = new User("Juan", "Pérez", "juan.perez@email.com", "password123", "Calle Ficticia 123", 2134, 25);
        user.setId(1L);  // Asignamos un ID único al usuario.
    }

    // Método para obtener el usuario único
    public User getUser() {
        return user;  // Retorna el usuario que está en memoria
    }
}

