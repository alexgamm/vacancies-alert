package vacanciesalert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vacanciesalert.model.entity.UserInfo;

import java.time.Instant;
import java.util.List;
import java.util.Set;


@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    @Query(value = "SELECT * FROM user_info u WHERE u.access_token IS NOT NULL AND jsonb_array_length(u.tags) > 0", nativeQuery = true)
    List<UserInfo> findUsersWithTagsAndAccessToken();

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
    @Query("UPDATE UserInfo u SET u.salary.from = :salaryFrom, u.salary.to = :salaryTo WHERE u.chatId = :chatId")
    void updateSalaryRange(Long chatId, Integer salaryFrom, Integer salaryTo);

    @Modifying
    @Query("UPDATE UserInfo u SET u.salary.showHiddenSalaryVacancies = :showHiddenSalaryVacancies WHERE u.chatId = :chatId")
    void toggleHiddenSalaryVacancies(Long chatId, boolean showHiddenSalaryVacancies);

    @Modifying
    @Query("UPDATE UserInfo u SET u.searchVacanciesFrom = :from WHERE u.chatId = :chatId")
    void updateSearchVacanciesFrom(Long chatId, Instant from);

    // TODO add updateShowHiddenVacancies
}