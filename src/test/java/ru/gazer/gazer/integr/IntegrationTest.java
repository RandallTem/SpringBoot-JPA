package ru.gazer.gazer.integr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Класс, предназначенный для интеграционного тестирования. Тесты упорядочены, так как выполнение некоторых тестов
 * приводит к невыполнимости других.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest {

    /** Экземпляр класса TestRestTemplate. Он позволяет выполнять http запросы  */
    TestRestTemplate restTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);

    /**
     * Тестирование регистрации пользователя. Выполняется POST запрос, в котором передается
     * содержимое формы. После успешной регистрации выполняется редирект на главную страницу (302).
     * Если регистрация не удалась (например, такой аккаунт уже существует), то страница перезагружается (200).
     */
    @Test
    @Order(1)
    public void testRegister() {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/register", HttpMethod.POST, getRegisterRequest() , String.class );
        assertEquals(response.getStatusCode(), HttpStatus.FOUND);
        response = restTemplate.exchange("http://localhost:8080/register", HttpMethod.POST, getRegisterRequest(), String.class );
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Тестирование авторизации пользователя. В POST запросе передаются почта и пароль. Успешная авторизация
     * завершается редиректом на главную страницу (302).
     */
    @Test
    @Order(2)
    public void testAuthorize() {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/login", HttpMethod.POST, getLoginRequest() , String.class );
        assertEquals(response.getStatusCode(), HttpStatus.FOUND);
    }

    /**
     * Тестирование загрузки страницы с базой клиентов. Страница доступна только для авторизованных пользователей.
     * Неавторизованные пользователи перенаправляются на страницу авторизации.
     */
    @Test
    @Order(3)
    public void testGetClientsPage() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/clients", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotEquals(response.getBody().indexOf("Авторизация"), -1);
        headers.add("Cookie", getCookie());
        response = restTemplate.exchange(
                "http://localhost:8080/clients", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotEquals(response.getBody().indexOf("Поиск по имени и фамилии"), -1);
    }

    /**
     * Тестирование поиска клиентов по имени и фамилии. В Post запросе передаются серия и номер паспорта.
     * Если искомого клиента нет, то вместо таблицы выводится сообщение, что ничего не найдено.
     * Иначе, выводится таблица с пользователями с соответствующими именами и фамилиями.
     */
    @Test
    @Order(4)
    public void testFindByName() {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/findbyname", HttpMethod.POST, getClientNameRequest(true), String.class);
        assertNotEquals(response.getBody().indexOf("Ничего не найдено"), -1);
        response = restTemplate.exchange(
                "http://localhost:8080/findbyname", HttpMethod.POST, getClientNameRequest(false), String.class);
        assertNotEquals(response.getBody().indexOf("88005553535"), -1);
    }

    /**
     * Тестирование поиска клиентов по серии и номеру паспорта. В Post запросе передаются серия и номер паспорта.
     * Если искомого клиента нет, то вместо таблицы выводится сообщение, что ничего не найдено.
     * Иначе, выводятся данные пользователя с соответствующим паспортом.
     */
    @Test
    @Order(5)
    public void testFindByPassport() {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/findbypass", HttpMethod.POST, getClientPassportRequest(true), String.class);
        assertNotEquals(response.getBody().indexOf("Ничего не найдено"), -1);
        response = restTemplate.exchange(
                "http://localhost:8080/findbypass", HttpMethod.POST, getClientPassportRequest(false), String.class);
        assertNotEquals(response.getBody().indexOf("88005553535"), -1);
    }

    /**
     * Тестирование загрузки страницы управления аккаунтом. Страница доступна только для авторизованных
     * пользователей, остальные перенаправляются на страницу авторизации.
     */
    @Test
    @Order(6)
    public void testGetAccountPage() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/account", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotEquals(response.getBody().indexOf("Авторизация"), -1);
        headers.add("Cookie", getCookie());
        response = restTemplate.exchange(
                "http://localhost:8080/account", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotEquals(response.getBody().indexOf("ПРОФИЛЬ"), -1);
    }

    /**
     * Тестирование обновления данных пользователя. В Post запросе передаются новые данные профиля. Также
     * передается старый пароль. Он должен быть верным, иначе отобразится та же страница с ошибкой.
     * Если данные успешно обновлены, то выполнится редирект на главную страницу с выходом из аккаунта.
     */
    @Test
    @Order(7)
    public void testUpdateUser() {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/update", HttpMethod.POST, getUpdateUserRequest(true), String.class);
        assertNotEquals(response.getBody().indexOf("Введен неверный пароль"), -1);
        response = restTemplate.exchange(
                "http://localhost:8080/update", HttpMethod.POST, getUpdateUserRequest(false), String.class);
        assertEquals(response.getStatusCode(), HttpStatus.FOUND);
    }

    /**
     * Тестирование удаления клиента. Доступно только для авторизованных пользователей. После успешного удаления,
     * страница с базой пользователей перезагружается.
     */
    @Test
    @Order(8)
    public void deleteClient() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/delete?id=1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotEquals(response.getBody().indexOf("Авторизация"), -1);
        headers.add("Cookie", getCookie());
        response = restTemplate.exchange(
                "http://localhost:8080/delete?id=1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotEquals(response.getBody().indexOf("Ничего не найдено"), -1);
    }

    /**
     * Тестирование удаления пользователя. Доступно только для авторизованных пользователей. Пользователя выбрасывает
     * из аккаунта и перенаправляет на главную страницу. Аккаунт удаляется из базы данных.
     */
    @Test
    @Order(9)
    public void deleteUser() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8080/deleteuser", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotEquals(response.getBody().indexOf("Авторизация"), -1);
        headers.add("Cookie", getCookie());
        response = restTemplate.exchange(
                "http://localhost:8080/deleteuser", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertNotEquals(response.getBody().indexOf("GAZER"), -1);
    }

    /**
     * Метод предназначен для получения Cookie. Должен вызываться в каждом тесте, который хочет
     * получить доступ к страницам, доступным только для авторизованных пользователей.
     */
    public String getCookie() {
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8080/login", getLoginRequest() , String.class );
        String header = response.getHeaders().get("Set-Cookie").get(0);
        String cookie = header.substring(0, header.indexOf(';'));
        return cookie;
    }

    /**
     * Метод формирует Post запрос для регистрации пользователя.
     */
    public HttpEntity<MultiValueMap<String, String>> getRegisterRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("username", "TestUser");
        map.add("email", "test@test.com");
        map.add("password", "123456789");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return request;
    }

    /**
     * Метод формирует Post запрос для авторизации пользователя.
     */
    public HttpEntity<MultiValueMap<String, String>> getLoginRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("email", "test@test.com");
        map.add("password", "123456789");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return request;
    }

    /**
     * Метод формирует Post запрос для поиска клиентов по имени и фамилии. Может формировать правильный и
     * неправильный запросы в зависимости от принятого аргумента. Для неправильного запроса не будет
     * найдено клиентов.
     */
    public HttpEntity<MultiValueMap<String, String>> getClientNameRequest(boolean wrong) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Cookie", getCookie());
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("firstName", "Test");
        if (wrong)
            map.add("lastName", "User");
        else
            map.add("lastName", "Client");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return request;
    }

    /**
     * Метод формирует Post запрос для поиска клиентов по серии и номеру. Может формировать правильный и
     * неправильный запросы в зависимости от принятого аргумента. Для неправильного запроса не будет
     * найдено клиентов.
     */
    public HttpEntity<MultiValueMap<String, String>> getClientPassportRequest(boolean wrong) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Cookie", getCookie());
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("passportSeries", "1111");
        if (wrong)
            map.add("passportNumber", "222223");
        else
            map.add("passportNumber", "222222");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return request;
    }

    /**
     * Метод формирует Post запрос для обновления данных пользователя. Может формировать правильный и
     * неправильный запрос в зависимости от принятого аргумента. В неправильном запросе старый пароль
     * не соответствует истинному, что приведет к ошибке.
     */
    public HttpEntity<MultiValueMap<String, String>> getUpdateUserRequest(boolean wrong) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Cookie", getCookie());
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("username", "UpdatedUser");
        map.add("email", "test@test.com");
        map.add("password", "123456789");
        if (wrong)
            map.add("oldPassword", "123456780");
        else
            map.add("oldPassword", "123456789");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return request;
    }

}
