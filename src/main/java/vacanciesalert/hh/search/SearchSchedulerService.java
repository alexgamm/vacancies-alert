package vacanciesalert.hh.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vacanciesalert.hh.oauth.AuthorizationService;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.model.hhSearchResponse.Vacancy;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchSchedulerService {
    private final UserInfoRepository userInfoRepository;
    private final AuthorizationService authorizationService;
    private final SearchVacanciesService searchVacanciesService;
    private final TelegramService telegramService;


    @Scheduled(fixedRate = 600000)
    public void schedule() {
        // TODO remove identical vacancies based on id
        log.info("scheduled task begins. Time: {}", Instant.now());
        List<UserInfo> allUsers = userInfoRepository.findAll();
        for (UserInfo user : allUsers) {
            if (user.getTags() == null) {
                continue;
            }
            Map<String, List<Vacancy>> newVacancies = searchVacanciesService.getNewVacancies(
                    authorizationService.decodeToken(user.getAccessToken()),
                    user.getTags(),
                    Instant.now().minusSeconds(600)
            );
            log.info("For chatId: {}. Found vacancies: {}", user.getChatId(), newVacancies);
            if (newVacancies.values().stream().allMatch(List::isEmpty)) {
                continue;
            }
            for (String tag : newVacancies.keySet()) {
                List<Vacancy> vacancies = newVacancies.get(tag);
                if (!vacancies.isEmpty()) {
                    String header = "Обнаружены новые вакансии по запросу: " + tag + "\n";
                    String vacanciesText = getVacanciesMessageText(vacancies);
                    log.info("Message in tg: {}", header + vacanciesText);
                    telegramService.sendTextMessage(user.getChatId().toString(), header + vacanciesText);
                }
            }
        }
        log.info("scheduled task ends. Time: {}", Instant.now());
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
