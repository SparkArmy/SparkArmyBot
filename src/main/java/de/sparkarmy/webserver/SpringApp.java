package de.sparkarmy.webserver;

import de.sparkarmy.tasks.ThreadController;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class SpringApp {

    @EventListener(ApplicationReadyEvent.class)
    public void springReadyEvent(){
        new ThreadController();
    }

}
