package vacanciesalert.hh.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vacanciesalert.hh.exception.ApiException;
import vacanciesalert.hh.exception.ClientException;
import vacanciesalert.hh.oauth.AuthorizationService;
import vacanciesalert.model.entity.Salary;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.model.hhSearchResponse.Vacancy;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchSchedulerService {
    private final UserInfoRepository userInfoRepository;
    private final AuthorizationService authorizationService;
    private final SearchVacanciesService searchVacanciesService;
    private final TelegramService telegramService;

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
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
        List<UserInfo> allUsers = userInfoRepository.findUsersWithTagsAndAccessToken();
        for (UserInfo user : allUsers) {
            notifyUserAboutFreshVacancies(user);
        }
        log.info("scheduled task ends. Time: {}", Instant.now());
    }

    private void notifyUserAboutFreshVacancies(UserInfo user) {
        String accessToken;
        if (user.getExpiredAt() != null && Instant.now().isBefore(user.getExpiredAt())) {
            accessToken = user.getAccessToken();
        } else {
            try {
                accessToken = authorizationService.refreshTokens(user.getChatId(), user.getRefreshToken()).accessToken();
            } catch (ApiException | ClientException e) {
                log.error("Api error when exchanging refresh to access token for user {}", user.getChatId(), e);
                return;
            }
        }
        for (String tag : user.getTags()) {
            Salary salary = user.getSalary();
            List<Vacancy> newVacancies = searchVacanciesService.getNewVacancies(user.getChatId(), accessToken, tag, salary, Duration.ofMinutes(10));
            log.info("For chatId: {}; tag: {}. Found vacancies: {}", user.getChatId(), tag, newVacancies);
            if (newVacancies.isEmpty()) {
                continue;
            }
            String header = "Обнаружены новые вакансии по запросу: " + tag + "\n";
            String vacanciesText = getVacanciesMessageText(newVacancies);
            log.info("Message in tg: {}", header + vacanciesText);
            telegramService.sendTextMessage(user.getChatId(), header + vacanciesText);

        }
    }

    private static String getVacanciesMessageText(List<Vacancy> vacancies) {
        String vacanciesText = "";
        for (Vacancy vacancy : vacancies) {
            String salary;
            if (vacancy.getSalary() == null) {
                salary = "Заработная плата не указана";
            } else {
                String from = vacancy.getSalary().getFrom() == null ? "от 0" : "от " + vacancy.getSalary().getFrom();
                String to = vacancy.getSalary().getTo() == null ? "" : " до " + vacancy.getSalary().getTo();
                salary = from + to + " " + vacancy.getSalary().getCurrency();
            }
            vacanciesText += "\n" + vacancy.getName() + ", " + vacancy.getArea().getName() + "\n" + salary + "\n" + vacancy.getAlternateUrl() + "\n";
        }
        return vacanciesText;
    }

}
