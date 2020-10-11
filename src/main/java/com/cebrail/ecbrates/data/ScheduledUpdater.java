package com.cebrail.ecbrates.data;

public interface ScheduledUpdater {

    //@Scheduled(fixedRate = 1000)
    void dailyUpdate();

    void weeklyUpdate();

    void monthlyUpdate();
}
