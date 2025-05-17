package es.xpressaly.mapper;

import es.xpressaly.Model.User;
import es.xpressaly.dto.UserWebDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserWebMapper {
    
    UserWebDTO toDTO(User user);
    
    List<UserWebDTO> toDTOs(Collection<User> users);
    
    @Mapping(target = "roles", ignore = true)
    User toDomain(UserWebDTO userWebDTO);
} 