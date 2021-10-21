package ru.gazer.gazer.repos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gazer.gazer.models.Role;

/**
 * Интерфейс для взаимодействия с таблицей user_roles базы данных
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}
