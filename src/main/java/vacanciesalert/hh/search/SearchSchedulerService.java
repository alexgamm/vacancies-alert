package vacanciesalert.hh.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vacanciesalert.ext.VacancyExtKt;
import vacanciesalert.ext.hh.search.Vacancy;
import vacanciesalert.hh.exception.ApiException;
import vacanciesalert.hh.exception.ClientException;
import vacanciesalert.hh.oauth.AuthorizationService;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchSchedulerService {
    private final UserInfoRepository userInfoRepository;
    private final AuthorizationService authorizationService;
    private final SearchVacanciesService searchVacanciesService;
    private final TelegramService telegramService;

    @Value("${search.cutoff:PT10M}")
    private Duration cutoff;

    @Scheduled(fixedRateString = "${search.period:PT10M}")
    public void schedule() {
        try {
            startScheduledTask();
        } catch (Throwable e) {
            log.error("Error while running scheduled search", e);
        }
    }

    private void startScheduledTask() {
        // TODO remove identical vacancies based on id
        log.info("scheduled task begins. Time: {}", Instant.now());
        List<UserInfo> allUsers = userInfoRepository.findUsersWithTags();
        for (UserInfo user : allUsers) {
            notifyUserAboutFreshVacancies(user);
        }
        log.info("scheduled task ends. Time: {}", Instant.now());
    }

    private void notifyUserAboutFreshVacancies(UserInfo user) {
        String accessToken;
        try {
            accessToken = authorizationService.getActualAccessToken(user);
        } catch (ApiException | ClientException e) {
            log.error("Api error when exchanging refresh to access token for user {}", user.getChatId(), e);
            return;
        }
        sendNewVacancies(user, accessToken);
    }

    private void sendNewVacancies(UserInfo user, String accessToken) {
        for (String tag : user.getTags()) {
            UserInfo.Salary salary = user.getSalary();
            List<Vacancy> newVacancies = searchVacanciesService.getNewVacancies(
                    user.getChatId(),
                    accessToken,
                    tag,
                    salary,
                    cutoff
            );
            log.info("For chatId: {}; tag: {}. Found vacancies: {}", user.getChatId(), tag, newVacancies);
            if (newVacancies.isEmpty()) {
                continue;
            }
            for (Vacancy vacancy : newVacancies) {
                String vacancyText = VacancyExtKt.format(vacancy);
                log.info("Message in tg: {}", "Обнаружена новая вакансия по запросу: " + tag + vacancyText);
                telegramService.sendTextMessage(user.getChatId(), vacancyText);
            }
        }
    }
}
