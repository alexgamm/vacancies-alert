package vacanciesalert.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "vacancy")
@IdClass(Vacancy.VacancyId.class)
public class Vacancy {
    @Id
    private long userId;
    @Id
    private long vacancyId;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class VacancyId implements Serializable {
        private long userId;
        private long vacancyId;
    }
}
