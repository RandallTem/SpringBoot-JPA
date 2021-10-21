package ru.gazer.gazer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.gazer.gazer.models.Client;
import ru.gazer.gazer.repos.ClientRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

/**
 * Класс реализует бизнес логику взаимодействия с таблицей clients базы данных
 */
@Service
public class ClientService {

    /** Интерфейс, обеспечивающий взаимодействие с базой данных */
    @Autowired
    private ClientRepository clientRepository;

    /**
     * Метод проверяет, существует ли в таблице clients запись с принятыми в качестве аргумента
     * паспортными данными. Если существует, то возвращается true, иначе, false.
     */
    public boolean isPassportUsed(String passportSeries, String passportNumber, Integer userId) {
        Pageable clientsPage = PageRequest.of(0, 15);
        List<Integer> userIds = Arrays.asList(0, userId);
        Slice<Client> clients = clientRepository.findClientByPassportSeriesAndPassportNumberAndUserIdIn(passportSeries, passportNumber, userIds, clientsPage);
        if (clients.getContent().size() > 0)
            return true;
        return false;
    }

    /**
     * Метод устанавливает значение userId и выполняет сохранения объекта Client в базу данных.
     */
    public boolean saveClient(Client client, MultipartFile file, Integer userId, String docsPath) {
        client.setUserId(userId);
        clientRepository.save(client);
        saveFile(file, client.getClientId(), docsPath);
        return true;

    }

    /**
     * Метод удаляет из таблицы clients запись с принятым значением clientId
     */
    public void deleteClient(Integer clientId) {
        clientRepository.deleteById(clientId);
    }

    /**
     * Метод выполняет сохранения файла на сервер.
     */
    private boolean saveFile(MultipartFile file, Integer clientId, String docsPath) {
        try {
            Path path = Paths.get(docsPath + clientId + ".pdf");
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            System.out.println("Exception " + e);
            return false;
        }
    }

    /**
     * Метод получает из базы данных одну страницу записей и возвращает ее.
     */
    public Slice<Client> getClientsPage(Integer page, Integer userId) {
        Pageable clientsPage = PageRequest.of(page, 15);
        List<Integer> userIds = Arrays.asList(0, userId);
        Slice<Client> clients = clientRepository.findAllByUserIdIn(userIds, clientsPage);
        return clients;
    }

    /**
     * Метод получает из базы данных одну страницу записей, которые содержат в полях
     * first_name и last_name искомые значения, и возвращает ее.
     */
    public Slice<Client> getByName(String firstName, String lastName, Integer userId) {
        Pageable clientsPage = PageRequest.of(0, 15);
        List<Integer> userIds = Arrays.asList(0, userId);
        Slice<Client> clients = clientRepository.findClientsByFirstNameAndLastNameAndUserIdIn(firstName, lastName, userIds, clientsPage);
        return clients;
    }

    /**
     * Метод получает из базы данных единственную запись на странице, которая содержит в полях
     * passport_series и passport_number искомые значения, и возвращает ее.
     */
    public Slice<Client> getByPassport(String passportSeries, String passportNumber, Integer userId) {
        Pageable clientsPage = PageRequest.of(0, 15);
        List<Integer> userIds = Arrays.asList(0, userId);
        Slice<Client> clients = clientRepository.findClientByPassportSeriesAndPassportNumberAndUserIdIn(passportSeries, passportNumber, userIds, clientsPage);
        return clients;
    }

    /**
     * Метод удаляет всех клиентов, добавленных пользователем. Вызывается при удалении пользователя.
     */
    @Transactional
    public void deleteUserClients(Integer userId) {
        clientRepository.deleteAllByUserId(userId);
    }

}
