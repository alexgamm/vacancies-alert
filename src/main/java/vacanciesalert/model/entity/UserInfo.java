package vacanciesalert.model.entity;


import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_info")
@Getter
public class UserInfo {
    @NotNull
    @Id
    @Column(name = "chat_id")
    private Long chatId;
    @NotNull
    @Column(name = "access_token")
    private String accessToken;
    @NotNull
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
}

