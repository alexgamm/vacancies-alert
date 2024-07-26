package vacanciesalert.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vacanciesalert.hh.oauth.model.UserTokens;
import vacanciesalert.model.entity.UserInfo;

import java.time.Instant;
import java.util.List;


@Repository
public interface UserInfoRepository extends CrudRepository<UserInfo, Long> {

    @Query(value = "SELECT * FROM user_info WHERE array_length(tags, 1) > 0")
    List<UserInfo> findUsersWithTags();

    @Modifying
    @Query("DELETE FROM user_info WHERE chat_id = :chatId")
    void deleteUserByChatId(Long chatId);

    @Modifying
    @Query("UPDATE user_info SET tokens = :tokens WHERE chat_id = :chatId")
    void updateTokens(Long chatId, UserTokens tokens);

    @Modifying
    @Query("UPDATE user_info SET tags = :tags WHERE chat_id = :chatId")
    void updateTags(Long chatId, String[] tags);

    @Modifying
    @Query("UPDATE user_info SET salary_from = :salaryFrom, salary_to = :salaryTo WHERE chat_id = :chatId")
    void updateSalaryRange(Long chatId, Integer salaryFrom, Integer salaryTo);

    @Modifying
    @Query("UPDATE user_info SET show_hidden_salary_vacancies = :showHiddenSalaryVacancies WHERE chat_id = :chatId")
    void toggleHiddenSalaryVacancies(Long chatId, boolean showHiddenSalaryVacancies);

    @Modifying
    @Query("UPDATE user_info SET search_vacancies_from = :from WHERE chat_id = :chatId")
    void updateSearchVacanciesFrom(Long chatId, Instant from);

}