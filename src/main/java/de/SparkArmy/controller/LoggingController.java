package de.SparkArmy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggingController {

   public static final Logger logger = LoggerFactory.getLogger(LoggingController.class);

}
