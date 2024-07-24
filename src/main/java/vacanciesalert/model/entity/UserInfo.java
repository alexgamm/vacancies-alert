package vacanciesalert.model.entity;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import vacanciesalert.hh.oauth.model.UserTokens;

import java.time.Instant;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Getter
@Builder
public class UserInfo implements Persistable<Long> {
    @NotNull
    @Id
    @Column("chat_id")
    private Long chatId;
    @NotNull
    @Column("tokens")
    private UserTokens tokens;
    @NotNull
    private Set<String> tags;
    @Nullable
    @Column("search_vacancies_from")
    private Instant searchVacanciesFrom;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private Salary salary;

    @Transient
    private boolean isNew = false;

    @Override
    public Long getId() {
        return chatId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    public static class Salary {
        @Nullable
        @Column("salary_from")
        private Integer from;
        @Nullable
        @Column("salary_to")
        private Integer to;
        @Column("show_hidden_salary_vacancies")
        private boolean showHiddenSalaryVacancies;
    }
}

