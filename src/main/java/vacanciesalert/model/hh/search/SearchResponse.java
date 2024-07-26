package vacanciesalert.model.hh.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SearchResponse {
    private List<Vacancy> items;
}
