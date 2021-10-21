package ru.gazer.gazer.repos;

import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.gazer.gazer.models.Client;

import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Интерфейс для взаимодействия с таблицей clients базы данных
 */
public interface ClientRepository extends JpaRepository<Client, Integer> {

    /**
     * Метод получает записи с искомыми значениями полей passport_series, passport_number и user_id
     */
    Slice<Client> findClientByPassportSeriesAndPassportNumberAndUserIdIn(String passportSeries, String passportNumber, List<Integer> ids, Pageable pageable);

    /**
     * Метод получает записи с искомыми значениями полей first_name, last_name и user_id
     */
    Slice<Client> findClientsByFirstNameAndLastNameAndUserIdIn(String firstName, String lastName, List<Integer> ids, Pageable pageable);

    /**
     * Метод получает записи с искомым значением поля user_id
     */
    Slice<Client> findAllByUserIdIn(List<Integer> userIds, Pageable pageable);

    /**
     * Метод удаляет все записи с userId
     */
    void deleteAllByUserId(Integer userId);
}