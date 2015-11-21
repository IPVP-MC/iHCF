package com.doctordark.hcf.eventgame;

import java.time.LocalDateTime;
import java.util.Map;

public interface IEventScheduler {

    Map<LocalDateTime, String> getScheduleMap();
}
