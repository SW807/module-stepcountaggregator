package dk.aau.cs.psylog.analysis.stepcountaggregator;

import android.content.Intent;

import dk.aau.cs.psylog.module_lib.IScheduledTask;
import dk.aau.cs.psylog.module_lib.ScheduledService;

public class PsyLogService extends ScheduledService {

    public PsyLogService() {
        super("debug name - stepcount aggregator");
    }

    @Override
    public void setScheduledTask() {
        this.scheduledTask = new Aggregator(this);
    }
}
