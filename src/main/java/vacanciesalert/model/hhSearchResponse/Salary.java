package vacanciesalert.model.hhSearchResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Salary {
    // TODO make /disable/enable vacancies with hidden salary
    // TODO set salary range
    private Integer from;
    private Integer to;
    private String currency;
}
