package de.SparkArmy.controller;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LoggingMarker {

    public static final Marker CONFIG = MarkerFactory.getMarker("CONFIG");
    public static final Marker FILHANDLER = MarkerFactory.getMarker("FILEHANDLER");
    public static final Marker REQUESTS = MarkerFactory.getMarker("REQUESTS");

}
