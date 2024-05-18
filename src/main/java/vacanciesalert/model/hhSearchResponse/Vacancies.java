package vacanciesalert.model.hhSearchResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Vacancies {
    private List<Vacancy> items;
}
