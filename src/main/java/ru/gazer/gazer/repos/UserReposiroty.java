package ru.gazer.gazer.repos;
import org.springframework.stereotype.Repository;
import ru.gazer.gazer.models.User;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Интерфейс для взаимодействия с таблицей users базы данных
 */
@Repository
public interface UserReposiroty extends JpaRepository<User, Integer> {

    /**
     * Метод получает записи с искомым значением поля email
     */
    User findUserByEmail(String email);
}
