package uz.tengebank.loan_service_stub;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;

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

@RequestMapping("loan-service/api/v1")
@RestController
class LoanServiceApiV1 {
    @CrossOrigin
    @ResponseBody
    @PostMapping(path = "/loan-request", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> sendLoanRequest(@RequestBody LoanRq rq, HttpServletRequest request) throws ServiceException {
        if (!rq.getAgreement()) {
            throw new ServiceException("Agreement not accepted");
        }
        return ResponseEntity.ok().body("OK");
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

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class ServiceException extends Exception {
    public ServiceException(String message) {
        super(message);
    }
}

