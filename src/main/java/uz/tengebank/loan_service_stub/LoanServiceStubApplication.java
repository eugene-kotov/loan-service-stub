package uz.tengebank.loan_service_stub;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@SpringBootApplication
@EnableSwagger2
public class LoanServiceStubApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoanServiceStubApplication.class, args);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}

@RequestMapping("/api/v1/loan-service")
@RestController
class LoanServiceApiV1 {

    private final ValidationService validationService;

    public LoanServiceApiV1(ValidationService validationService) {
        this.validationService = validationService;
    }

    @CrossOrigin
    @ResponseBody
    @PostMapping(path = "/loan-request", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> sendLoanRequest(@RequestBody LoanRq rq, HttpServletRequest request) throws ServiceException {
        validationService.validate(rq);
        return ResponseEntity.ok().body("OK");
    }
}

interface ValidationService {
    void validate(LoanRq rq) throws ServiceException;
}

@Slf4j
@Service
class ValidationServiceImpl implements ValidationService{
    public void validate(LoanRq rq) throws ServiceException{
        if (!rq.getAgreement()) {
            log.error("Agreement not accepted");
            throw new ServiceException("Agreement not accepted");
        }
        if (!Arrays.asList("32", "34").contains(rq.getProductType())) {
           log.error("Invalid product type");
            throw new ServiceException("Invalid product type");
        }
        if (!Arrays.asList(0, 1, 2, 3).contains(rq.getPayMethod())) {
            log.error("Invalid pay method");
            throw new ServiceException("Invalid pay method");
        }
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class LoanRq {
    private String productType; // 32 - микрозайм, 34 - автокредит
    private String mobilePhone;
    private Double loanAmount; // до 20 000 000
    private Integer docType; // Воинское удовстверение-2; Вид на жительство-5; Биометрический паспорт гражданина РУз-6; Другое-9
    private String docNumber;
    private String docScan;
    private Integer loanTerm; // Автокредит до 48 месяцев; Микрозайм до 36 месяцев; Ипотека до 180 месяцев
    private Integer payMethod; // Уменьшение платежей-0; Аннуитет реальный с измен.на раб.дни-1; Аннуитет 360/30-2; Аннуитет реальный без измен.на раб.дни-3
    private Boolean agreement;
}

@Configuration
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers(HttpMethod.POST, "/api/**").hasAnyRole(Role.ADMIN.name(), Role.USER.name())
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic();
    }

    @Override
    protected UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.builder()
                        .username("admin")
                        .password(passwordEncoder().encode("admin"))
                        .roles(Role.ADMIN.name())
                        .build(),
                User.builder()
                        .username("user")
                        .password(passwordEncoder().encode("user"))
                        .roles(Role.USER.name())
                        .build());
    }

    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

enum Role {
    USER, ADMIN
}

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class ServiceException extends Exception {
    public ServiceException(String message) {
        super(message);
    }
}

