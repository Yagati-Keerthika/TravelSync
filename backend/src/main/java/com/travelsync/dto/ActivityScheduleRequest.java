package com.travelsync.dto;

import com.travelsync.engine.IntervalDP;
import java.util.List;

public class ActivityScheduleRequest {
    private List<IntervalDP.Activity> activities;
    private int maxSlots;

    public ActivityScheduleRequest() {}

    public List<IntervalDP.Activity> getActivities() { return activities; }
    public void setActivities(List<IntervalDP.Activity> activities) { this.activities = activities; }

    public int getMaxSlots() { return maxSlots; }
    public void setMaxSlots(int maxSlots) { this.maxSlots = maxSlots; }
}
