package ai.maum.chathub.initialize;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppStartupRunner implements CommandLineRunner {

    private final InitializationService initializationService;

    @Override
    public void run(String... args) throws Exception {
        initializationService.performInitializationIfNeeded();
    }
}
