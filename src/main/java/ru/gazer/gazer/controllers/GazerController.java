package ru.gazer.gazer.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.gazer.gazer.models.Client;
import ru.gazer.gazer.service.ClientService;
import ru.gazer.gazer.service.UserService;
import ru.gazer.gazer.models.User;
import ru.gazer.gazer.models.Role;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;

/**
 * Класс - контроллер. Обрабатывает входящие Get и Post запросы.
 * Взаимодействует с UserService и ClientService для их исполнения
 */
@Controller
@RequestMapping("/")
public class GazerController {

    /** Экземпляр класса UserService */
    @Autowired
    private UserService userService;

    /** Экземпляр класса ClientService */
    @Autowired
    private ClientService clientService;

    /** Пусть к папке, в которой лежат файлы с информацией о пользователях.
     * Путь задается в application.properties
     */
    @Value("${document.folder}")
    String docsPath;


    /**
     * Метод возвращает страницу для авторизации, если пользователь еще не авторизован.
     * Если авторизован, то его перенаправляет на главную страницу
     */
    @GetMapping("/login")
    public String getLoginPage(Model model, @AuthenticationPrincipal User user) {
        if (user != null)
            return "redirect:/";
        else
            return "login";
    }

    /**
     * Метод возвращает главную страницу сайта
     */
    @GetMapping("/")
    public String getHomePage(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("user", user);
        return "home";
    }

    /**
     * Метод возвращает страницу с формой добавления нового клиента
     */
    @GetMapping("/addclient")
    public String getClientForm(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("user", user);
        model.addAttribute("client", new Client());
        return "addclient";
    }

    /**
     * Метод возвращает страницу с формой для регистрации нового пользователя если пользователь
     * не авторизован. Если авторизован, то его перенаправляет на главную страницу
     */
    @GetMapping("/register")
    public String getRegisterPage(Model model, @AuthenticationPrincipal User user) {
        if (user != null)
            return "redirect:/";
        model.addAttribute("user", new User());
        model.addAttribute("role", new Role(1, "USER"));
        return "register";
    }

    /**
     * Метод загружает страницу с клиентами. Принимает request параметр с номером страницы.
     * Если параметр не указан, то загружает первую страницу
     */
    @GetMapping("/clients")
        public String getClientsPage(@RequestParam(value = "page", required = false) String pageStr,
                                     Model model,
                                     @AuthenticationPrincipal User user) {
            Integer pageNum;
            try {
                pageNum = Integer.parseInt(pageStr);
            } catch (Exception e) {
                pageNum = 0;
            }
            Slice<Client> clientsPage = clientService.getClientsPage(pageNum, user.getId());
            model.addAttribute("clients", clientsPage.getContent());
            if (clientsPage.hasNext())
                model.addAttribute("hasNext", "");
            if (clientsPage.hasPrevious())
                model.addAttribute("hasPrevious", "");
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("user", user);
            return "clients";
        }

    /**
     * Метод возвращает файл клиента для загрузки
     */
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadClientFile(Model model, @RequestParam("id") Integer id) throws Exception {
        File file = new File(docsPath + id + ".pdf");
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(MediaType.parseMediaType("application/pdf"))
                .contentLength(file.length())
                .body(resource);
    }

    /**
     * Метод удаляет клиента из базы данных
     */
    @GetMapping("/delete")
    public String deleteClient(@RequestParam("id") Integer id) {
        clientService.deleteClient(id);
        return "redirect:/clients";
    }

    /**
     * Метод возвращает страницу управления аккаунтом
     */
    @GetMapping("/account")
    public String getAccountPage(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("user", user);
        return "account";
    }

    /**
     * Метод удаляет из базы данных пользователя и всех добавленных им клиентов
     */
    @GetMapping("/deleteuser")
    public String deleteUser(@AuthenticationPrincipal User user) {
        clientService.deleteUserClients(user.getId());
        userService.deleteUser(user);
        return "redirect:/logout";
    }

    /**
     * Метод добавляет в базу данных нового клиента и перенаправляет пользователя
     * на страницу clients
     */
    @PostMapping("/addclient")
    public String addClient(@Valid Client client, BindingResult errors, Model model,
                            @RequestParam("file") MultipartFile file,
                            @AuthenticationPrincipal User user) {
        System.out.println(file.getContentType());
        boolean isFileValid = file.getContentType().equals("application/pdf") ? true : false;
        boolean isPassValid = !clientService.isPassportUsed(client.getPassportSeries(), client.getPassportNumber(), user.getId());
        if (!isPassValid)
            model.addAttribute("error2", "badpass");
        if (!isFileValid)
            model.addAttribute("error1", "badfile");
        if (errors.hasErrors() || !isFileValid || !isPassValid) {
            model.addAttribute("user", user);
            return "addclient";
        } else {
            clientService.saveClient(client, file, user.getId(), docsPath);
            return "redirect:/clients";
        }
    }

    /**
     * Метод добавляет нового пользователя в базу данных и перенаправляет посетителя
     * на главную страницу сайта
     */
    @PostMapping("/register")
    public String registerUser(@Valid User user, BindingResult errors, Model model) {
        if (errors.hasErrors()) {
            return "register";
        } else {
            if (userService.saveUser(user)) {
                return "redirect:/";
            } else {
                model.addAttribute("error", "wp");
                return "register";
            }
        }
    }

    /**
     * Метод ищет пользователей с определенными именем и фамилией и возвращает их странице clients
     * для отображения
     */
    @PostMapping("/findbyname")
    public String findByName(@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
                             @AuthenticationPrincipal User user, Model model) {
        Slice<Client> clientsPage = clientService.getByName(firstName, lastName, user.getId());
        model.addAttribute("back", "");
        model.addAttribute("clients", clientsPage.getContent());
        model.addAttribute("user", user);
        return "clients";
    }

    /**
     * Метод ищет пользователей с определенными паспортными данными и возвращает их странице clients
     * для отображения
     */
    @PostMapping("/findbypass")
    public String findByPass(@RequestParam("passportSeries") String passportSeries,
                             @RequestParam("passportNumber") String passportNumber,
                             @AuthenticationPrincipal User user, Model model) {
        Slice<Client> clientsPage = clientService.getByPassport(passportSeries, passportNumber, user.getId());
        model.addAttribute("back", "");
        model.addAttribute("clients", clientsPage.getContent());
        model.addAttribute("user", user);
        return "clients";
    }

    /**
     * Метод обновляет информацию о пользователе в базе данных
     */
    @PostMapping("/update")
    public String updateAccount(@Valid User updatedUser, BindingResult errors, Model model,
                                @RequestParam("oldPassword") String oldPassword,
                                @AuthenticationPrincipal User user) {
        if (errors.hasErrors()) {
            if (errors.hasFieldErrors("username"))
                model.addAttribute("usernameError", "");
            if (errors.hasFieldErrors("email"))
                model.addAttribute("emailError", "");
            if (errors.hasFieldErrors("password"))
                model.addAttribute("passwordError", "");
            return "account";
        } else {
            if (userService.checkUserPassword(user.getEmail(), oldPassword)) {
                userService.updateAccount(user, updatedUser);
                return "redirect:/logout";
            } else {
                model.addAttribute("passError", "");
                model.addAttribute("updatedUser", updatedUser);
                return "account";
            }
        }
    }
}
