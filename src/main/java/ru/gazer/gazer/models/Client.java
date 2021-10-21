package ru.gazer.gazer.models;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


/**
 * Класс - сущность, на основе которого создается таблица clients в базе данных.
 */
@Getter
@Setter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Entity
@Table(name = "clients")
@Validated
public class Client {
    @Id
    @SequenceGenerator(
            name = "client_id_sequence",
            sequenceName = "client_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE, generator = "client_id_sequence"
    )
    private Integer clientId;
    @NonNull
    @Size(min = 3, max = 20, message = "Некорректная длина имени")
    private String firstName;
    @NonNull
    @Size(min = 3, max = 20, message = "Некорректная длина фамилии")
    private String lastName;
    @NonNull
    private String sex;
    @NonNull
    @Pattern(regexp = "\\b\\d{2}\\b", message = "Некорректный возраст")
    private String age;
    @NonNull
    @Pattern(regexp = "\\b\\d{4}\\b", message = "Неверная серия паспорта")
    private  String passportSeries;
    @NonNull
    @Pattern(regexp = "\\b\\d{6}\\b", message = "Неверный номер паспорта")
    private  String passportNumber;
    @NonNull
    @Pattern(regexp = "\\b\\d{11}\\b", message = "Неверный номер телефона")
    private  String phone;
    @NonNull
    private  Integer userId;

}
