package vacanciesalert.model.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
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
import org.jetbrains.annotations.Nullable;
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
    @NotNull
    @Convert(converter = EncryptionConverter.class)
    @Column(name = "access_token")
    private String accessToken;
    @NotNull
    @Convert(converter = EncryptionConverter.class)
    @Column(name = "refresh_token")
    private String refreshToken;
    @NotNull
    @Column(name = "expired_at")
    private Instant expiredAt;
    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> tags;
    @Nullable
    @Column(name = "search_vacancies_from")
    private Instant searchVacanciesFrom;
    @Embedded
    private Salary salary;

}

