package ru.gazer.gazer.service;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.gazer.gazer.models.User;
import ru.gazer.gazer.repos.UserReposiroty;

/** Класс, предназначенный для тестирования методов класса UserService */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest extends TestCase {

    /** Имитация интерфейса userRepository */
    @Mock
    private UserReposiroty userRepository;

    /** Экземпляр энкодера BCryptPasswordEncoder*/
    @Spy
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** Экземпляр UserService. В него встраиваются зависимости BCryptPasswordEncoder и UserRepository */
    @InjectMocks
    UserService userService;

    /** Экземпляр класса User*/
    User user = new User();

    /**
     * Метод инициализирует Mock объекты и объект User.
     */
    @Before
    public void setUp() throws Exception {
        assertNotNull(userRepository);

        user.setId(1);
        user.setUsername("Test User");
        user.setEmail("test@user.com");
        user.setPassword(encoder.encode("password"));

        Mockito.when(userRepository.findUserByEmail("test@mail.com")).thenReturn(user);
    }

    /**
     * Тестирование метода saveUser(). Методу передается объект User, который затем сохраняется в базу данных.
     * Если все проходит хорошо, возвращается true. Если возникает какое-либо исключение, тогда false.
     */
    @Test
    public void testSaveUser() {
        boolean res = userService.saveUser(user);
        assertTrue(res);
    }

    /**
     * Тестирование метода checkUserPassword(). Методу передаются email и пароль. Из базы данных
     * берется объект с искомой почтой и сравнивается пароль из объекта и полученный пароль. Если она
     * равны, то возвращается true, иначе, false.
     */
    @Test
    public void testCheckUserPassword() {
        boolean res = userService.checkUserPassword("test@mail.com", "password");
        assertTrue(res);
    }

    /**
     * Тестирование метода loadUserByUsername(). Методу передается email. Из базы данных берется
     * объект с искомой почтой. Если объект не найден и вернулся null, выбрасывается исключение
     * UsernameNotFoundException, иначе, метод возвращает найденный объект User.
     */

    @Test
    public void testLoadUserByUsername() {
        User receivedUser = (User)userService.loadUserByUsername("test@mail.com");
        assertEquals(receivedUser, user);
    }

    /**
     * Тестирование метода updateAccount(). Метод принимает два объекта User, один оригинальный, а
     * второй - с обновленными данными. В оригинальным аккаунте обновляются значения полей и он сохраняется
     * в базу данных.
     */
    @Test
    public void testUpdateUser() {
        userService.updateAccount(user, user);
    }

    /**
     * Тестирование метода deleteUser(). Метод принимает объект User и удаляет связанную с ним запись
     * из базы данных.
     */
    @Test
    public void deleteUser() {
        userService.deleteUser(user);
    }

}