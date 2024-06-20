package vacanciesalert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vacanciesalert.model.entity.UserInfo;

import java.time.Instant;
import java.util.Set;


@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    UserInfo findUserInfoByChatId(Long chatId);

    @Modifying
    @Query("DELETE FROM UserInfo u WHERE u.chatId = :chatId")
    void deleteUserById(Long chatId);

    @Modifying
    @Query("UPDATE UserInfo u SET u.accessToken = :accessToken, u.refreshToken = :refreshToken, u.expiredAt = :expiredAt WHERE u.chatId = :chatId")
    void updateTokensByChatId(Long chatId, String accessToken, String refreshToken, Instant expiredAt);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserInfo u SET u.tags = :tags WHERE u.chatId = :chatId")
    void updateTags(Long chatId, Set<String> tags);

    @Modifying
    @Query("UPDATE UserInfo u SET u.salaryFrom = :salaryFrom, u.salaryTo = :salaryTo WHERE u.chatId = :chatId")
    void updateSalaryRange(Long chatId, Integer salaryFrom, Integer salaryTo);

    @Modifying
    @Query("UPDATE UserInfo u SET u.searchVacanciesFrom = :from WHERE u.chatId = :chatId")
    void updateSearchVacanciesFrom(Long chatId, Instant from);
}