package vacanciesalert.hh.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vacanciesalert.hh.exception.ApiException;
import vacanciesalert.hh.exception.ClientException;
import vacanciesalert.hh.oauth.AuthorizationService;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.model.hh.search.Vacancy;
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
        if (user.getTokens().accessTokenExpiration() != null && Instant.now().isBefore(user.getTokens().accessTokenExpiration())) {
            accessToken = user.getTokens().accessToken();
        } else {
            try {
                accessToken = authorizationService.refreshTokens(
                        user.getChatId(),
                        user.getTokens().refreshToken()
                ).accessToken();
            } catch (ApiException | ClientException e) {
                log.error("Api error when exchanging refresh to access token for user {}", user.getChatId(), e);
                return;
            }
        }
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
