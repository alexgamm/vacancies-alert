package vacanciesalert.model.hhSearchResponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Vacancy {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("area")
    private Area area;
    @JsonProperty("salary")
    private Salary salary;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    @JsonProperty("published_at")
    private Instant publishedAt;
    @JsonProperty("alternate_url")
    private String alternateUrl;
}
