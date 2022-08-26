package de.SparkArmy.springBoot;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
public class StandardMappings {

    @ResponseBody
    @GetMapping("/")
    public ResponseEntity<String> defaultSite(){
        return new ResponseEntity<>("Hier gibt es nichts zu sehen", HttpStatus.OK);
    }
}
