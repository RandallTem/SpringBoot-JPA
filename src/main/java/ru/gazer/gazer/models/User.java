package ru.gazer.gazer.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс - сущность, на основе которого создается таблица users в базе данных.
 */
@Getter
@Setter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Entity
@Table(name = "users",
        uniqueConstraints = {
        @UniqueConstraint(name = "unique_email", columnNames = "email")
        })
@Validated
public class User implements UserDetails {

    @Id
    @SequenceGenerator(
            name = "user_id_sequence",
            sequenceName = "user_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue (
            strategy = GenerationType.SEQUENCE, generator = "user_id_sequence"
    )
    private Integer id;
    @NonNull
    @Size(min = 3, max = 20, message = "Некорректная длина имени")
    private String username;
    @NonNull
    @Email(message = "Некорректный формат почты")
    private String email;
    @NonNull
    @Size(min = 8, message = "Слишком короткий пароль")
    private String password;
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonManagedReference
    @NonNull
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> role = new HashSet<>();
        role.add(getRole());
        return role;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
