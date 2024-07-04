package vacanciesalert.hh.exception;

import org.springframework.http.HttpStatusCode;

public class ApiException extends Exception {

    public ApiException(HttpStatusCode status, String responseBody) {
        super(String.format("%s: %s", status, responseBody));
    }
}
