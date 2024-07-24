package vacanciesalert.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
@Getter
public class Salary {
    @Nullable
    @Column(name = "salary_from")
    private Integer from;
    @Nullable
    @Column(name = "salary_to")
    private Integer to;
    @Column(name = "show_hidden_salary_vacancies")
    private boolean showHiddenSalaryVacancies;
}
