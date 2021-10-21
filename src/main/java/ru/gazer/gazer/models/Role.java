package ru.gazer.gazer.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Set;

/**
 * Класс - сущность, на основе которого создается таблица user_roles в базе данных.
 */
@Getter
@Setter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Entity
@Table (name = "user_roles")
public class Role implements GrantedAuthority {
    @Id
    private final Integer id;
    private final String roleName;
    @OneToMany(mappedBy = "role")
    @JsonBackReference
    private Set<User> users;

    @Override
    public String getAuthority() {
        return getRoleName();
    }
}
