package ru.gazer.gazer.service;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;
import ru.gazer.gazer.models.Client;
import ru.gazer.gazer.repos.ClientRepository;

import java.util.Arrays;

/** Класс, предназначенный для тестирования методов класса ClientService */
@RunWith(MockitoJUnitRunner.class)
public class ClientServiceTest extends TestCase {

    /** Имитация реализации интерфейса clientRepository */
    @Mock
    private ClientRepository clientRepository;

    /** Экземпляр UserService. В него встраивается зависимость ClientRepository */
    @InjectMocks
    ClientService clientService;

    /** Экземпляр класса Client*/
    Client client = new Client();

    /** Имитация реализации интерфейса Slice */
    @Mock
    Slice<Client> clients;

    /** Экземпляр класса PageRequest */
    Pageable clientsPage = PageRequest.of(0, 15);

    /**
     * Метод инициализирует Mock объекты и объект Client.
     */
    @Before
    public void setUp() throws Exception {
        assertNotNull(clientRepository);
        assertNotNull(clients);

        client.setClientId(1);
        client.setFirstName("Test");
        client.setLastName("Client");
        client.setPassportSeries("1111");
        client.setPassportNumber("222222");
        client.setUserId(0);

        Mockito.when(clientRepository
                .findClientByPassportSeriesAndPassportNumberAndUserIdIn(
                        "1111", "222222", Arrays.asList(0, 0), clientsPage))
                .thenReturn(clients);
        Mockito.when(clientRepository.findAllByUserIdIn(Arrays.asList(0, 0), clientsPage))
                .thenReturn(clients);
        Mockito.when(clientRepository.findClientsByFirstNameAndLastNameAndUserIdIn(
                "Test", "Client", Arrays.asList(0, 0), clientsPage))
                .thenReturn(clients);
        Mockito.when(clientRepository.findClientByPassportSeriesAndPassportNumberAndUserIdIn(
                "1111", "222222", Arrays.asList(0, 0), clientsPage))
                .thenReturn(clients);
        Mockito.when(clients.getContent()).thenReturn(Arrays.asList(client));

    }

    /**
     * Тестирование метода isPassportUser(). Методу передаются серия и номера паспорта. Он проверяет,
     * есть ли уже в базе данных объект с такими паспортными данными. Если есть, то возвращается true,
     * иначе false.
     */
    @Test
    public void testIsPassportUsed() {
        boolean res = clientService.isPassportUsed("1111", "222222", 0);
        assertTrue(res);
    }

    /**
     * Тестирование метода deleteClient(). На вход принимает ID клиента, который должен быть удален
     * из базы данных.
     */
    @Test
    public void testDeleteClient() {
        clientService.deleteClient(1);
    }

    /**
     * Тестирование метода getClientsPage(). Методу передаются номер желаемой страницы и id пользователя,
     * которому должны принадлежать клиенты. Возвращается список всех найденных записей в виде объекта,
     * реализующего интерфейс Slice.
     */
    @Test
    public void testGetClientsPage() {
        Slice<Client> page = clientService.getClientsPage(0, 0);
        assertTrue(page.getContent().size() > 0);
    }

    /**
     * Тестирование метода getByName(). Методу передаются имя и фамилия клиента, а также id пользователя,
     * которому должны принадлежать клиенты. Возвращается список всех найденных записей в виде объекта,
     * реализующего интерфейс Slice.
     */
    @Test
    public void testGetByName() {
        Slice<Client> page = clientService.getByName("Test", "Client", 0);
        assertTrue(page.getContent().size() > 0);
    }

    /**
     * Тестирование метода getByName(). Методу передаются серия и номер паспорта клиента, а также id пользователя,
     * которому должны принадлежать клиенты. Возвращается список всех найденных записей в виде объекта,
     * реализующего интерфейс Slice.
     */
    @Test
    public void testGetByPassport() {
        Slice<Client> page = clientService.getByPassport("1111", "222222", 0);
        assertTrue(page.getContent().size() > 0);
    }
}
