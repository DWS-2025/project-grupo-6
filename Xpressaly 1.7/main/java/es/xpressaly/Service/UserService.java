package es.xpressaly.Service;

import org.springframework.stereotype.Service;

import es.xpressaly.Model.User;

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

    // Método para actualizar el usuario
    public void updateUser(User updatedUser) {
        // Actualizamos los datos del usuario en memoria
        this.user.setFirstName(updatedUser.getFirstName());
        this.user.setLastName(updatedUser.getLastName());
        this.user.setEmail(updatedUser.getEmail());
        this.user.setAddress(updatedUser.getAddress());
    }
}

