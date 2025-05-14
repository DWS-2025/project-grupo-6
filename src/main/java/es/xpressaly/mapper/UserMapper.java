package es.xpressaly.mapper;

import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.dto.UserDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UserMapper {
    
    public UserDTO toDTO(User user) {
        return new UserDTO(
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getAddress(),
            user.getPhoneNumber(),
            user.getAge(),
            user.getRole() == UserRole.ADMIN,
            null, // orders would be assigned elsewhere if needed
            null  // reviews would be assigned elsewhere if needed
        );
    }
    
    public User toEntity(UserDTO dto) {
        User user = new User();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email());
        user.setAddress(dto.address());
        user.setPhoneNumber(dto.phoneNumber());
        user.setAge(dto.age());
        return user;
    }
}