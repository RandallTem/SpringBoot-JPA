package ru.gazer.gazer.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.gazer.gazer.service.UserService;

/**
 * Конфигурация Spring Security
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    /** Экземпляр класса UserService */
    @Autowired
    UserService userService;
    /** Экземпляр класса BCryptPasswordEncoder */
    @Autowired
    BCryptPasswordEncoder encoder;

    /**
     * Метод устанавливает разграничение доступа к страницам веб-приложения,
     * а также конфигурирует процесс авторизации и выхода из аккаунта
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
                .authorizeRequests()
                .antMatchers("/addclient", "/clients", "/account",
                        "/download", "/update", "/findbypass", "/findbyname",
                        "/deleteuser", "/delete", "/createPresent20").hasAuthority("USER")
                .antMatchers("/home", "/register").permitAll()
           .and()
            .formLogin()
               .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll()
                .passwordParameter("password")
                .usernameParameter("email")
            .and()
            .rememberMe()
                .tokenValiditySeconds(60*60*24)
                .rememberMeParameter("remember-me")
            .and()
            .logout()
                .permitAll()
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .logoutSuccessUrl("/");
    }

    /**
     * В методе конфигурируется хранилище пользователей для авторизации.
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userService)
            .passwordEncoder(encoder);
    }
}
