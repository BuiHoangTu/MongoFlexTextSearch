package special.org.background.tasks;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class UpdateDb {
    @Scheduled(fixedRate = 30_000) // ms
    public void updateDB() {

    }
}
