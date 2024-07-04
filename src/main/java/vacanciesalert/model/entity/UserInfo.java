package vacanciesalert.model.entity;


import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vacanciesalert.utils.EncryptionConverter;

import java.time.Instant;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Accessors(chain = true)
@Table(name = "user_info")
@Getter
@Builder
public class UserInfo {
    @NotNull
    @Id
    @Column(name = "chat_id")
    private Long chatId;
    @Nullable
    @Convert(converter = EncryptionConverter.class)
    @Column(name = "access_token")
    private String accessToken;
    @Nullable
    @Convert(converter = EncryptionConverter.class)
    @Column(name = "refresh_token")
    private String refreshToken;
    @Nullable
    @Column(name = "expired_at")
    private Instant expiredAt;
    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> tags;
    @Nullable
    @Column(name = "search_vacancies_from")
    private Instant searchVacanciesFrom;
    @Column(name = "salary_from")
    private Integer salaryFrom;
    @Nullable
    @Column(name = "salary_to")
    private Integer salaryTo;
    @Column(name = "show_hidden_salary_vacancies")
    private boolean showHiddenSalaryVacancies;

}

