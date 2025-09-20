package vacanciesalert.ext.hh.search

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

class Vacancy {

    @JsonProperty("id")
    val id: String? = null

    @JsonProperty("name")
    val name: String? = null

    @JsonProperty("area")
    val area: Area? = null

    @JsonProperty("contacts")
    val contacts: Contact? = null

    @JsonProperty("experience")
    val experience: Experience? = null

    @JsonProperty("department")
    val department: Department? = null

    @JsonProperty("employer")
    val employer: Employer? = null

    @JsonProperty("salary")
    val salary: Salary? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    @JsonProperty("published_at")
    val publishedAt: Instant? = null

    @JsonProperty("alternate_url")
    val alternateUrl: String? = null
}
