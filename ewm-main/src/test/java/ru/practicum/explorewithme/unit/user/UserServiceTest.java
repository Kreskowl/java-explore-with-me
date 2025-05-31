package ru.practicum.explorewithme.unit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.exception.custom.NotFoundException;
import ru.practicum.explorewithme.unit.AbstractServiceTest;
import ru.practicum.explorewithme.unit.TestDataFactory;
import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;
import ru.practicum.explorewithme.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest extends AbstractServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_shouldReturnDtoWithId() {
        NewUserRequest request = new NewUserRequest("NewUser", "newuser@example.com");

        UserDto result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    void getUsers_shouldReturnAllUsersIfIdsNull() {
        userRepository.save(TestDataFactory.createUser("User1", "u1@example.com"));
        userRepository.save(TestDataFactory.createUser("User2", "u2@example.com"));

        List<UserDto> users = userService.getUsers(null, 0, 10);

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void getUsers_shouldReturnFilteredByIds() {
        User u1 = userRepository.save(TestDataFactory.createUser("User1", "u1@example.com"));
        userRepository.save(TestDataFactory.createUser("User2", "u2@example.com"));

        List<UserDto> users = userService.getUsers(List.of(u1.getId()), 0, 10);

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo(u1.getId());
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        User user = userRepository.save(TestDataFactory.createUser("ToDelete", "del@example.com"));

        userService.deleteUser(user.getId());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void deleteUser_shouldThrowIfNotFound() {
        assertThatThrownBy(() -> userService.deleteUser(9999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("user with id 9999 not found");
    }
}
