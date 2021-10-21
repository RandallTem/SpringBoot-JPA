package ru.gazer.gazer.service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.gazer.gazer.models.Role;
import ru.gazer.gazer.repos.UserReposiroty;
import ru.gazer.gazer.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Класс реализует бизнес логику взаимодействия с таблицей users базы данных
 */
@Service
public class UserService implements UserDetailsService {

    /** Интерфейс, обеспечивающий взаимодействие с базой данных */
    @Autowired
    private UserReposiroty userRepository;
    /** Экземпляр BCryptPasswordEncoder для кодирования пароля */
    @Autowired
    BCryptPasswordEncoder encoder;

    /**
     * Метод выполняет сохранения объекта User в базу данных, выдавая ему роль USER
     */
    public boolean saveUser(User user) {
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            user.setRole(new Role(1, "USER"));
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Метод получает из базы данных запись для полученного email и сверяет значения
     * полученного пароля и пароля в записи. Если пароли совпали, возвращается true,
     * иначе, false
     */
    public boolean checkUserPassword(String email, String password) {
        User user = userRepository.findUserByEmail(email);
        String pass = user.getPassword();
        if (encoder.matches(password, user.getPassword())) {
            return true;
        }
        return false;
    }

    /**
     * Метод обновляет значения полей для записи user в таблице users
     */
    public void updateAccount(User user, User updatedUser) {
        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setPassword(encoder.encode(updatedUser.getPassword()));
        userRepository.save(user);
    }

    /**
     * Метод удаляет запись для user из таблицы users
     */
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    /**
     * Метод необходим для реализации интерфейса UserDetailsService. Метод получает из базы данных запись
     * с искомым значением email и возвращает в виде объекта, реализующего интерфейс UserDetails.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }



}
