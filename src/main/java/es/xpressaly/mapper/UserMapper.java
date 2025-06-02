package es.xpressaly.mapper;

import es.xpressaly.Model.User;
import es.xpressaly.dto.UserDTO;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Collection;

@Mapper(componentModel = "spring", uses = {OrderMapper.class, ReviewMapper.class})
public interface UserMapper {
    
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    UserDTO toDTO(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "currentOrder", ignore = true)
    User toDomain(UserDTO userDTO);

    List<UserDTO> toDTOs(Collection<User> users);
}