package catering.businesslogic.shift;

import java.time.LocalDateTime;
import java.util.ArrayList;

import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.kitchen.KitchenTask;
import catering.businesslogic.user.User;

public class Shift {
    private LocalDateTime shiftStart;
    private LocalDateTime shiftEnd;
    private String place;
    private String type;
    private ArrayList<KitchenTask> assignedTasks;
    private ServiceInfo referredService;
    private User staff;

    public Shift(LocalDateTime shiftStart, LocalDateTime shiftEnd, User staff){
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.staff = staff;
    }
    public void addTask(KitchenTask task){
        assignedTasks.add(task);
    }
    public void removeTask(KitchenTask task){
        assignedTasks.remove(task);
    }
    public void dismiss(ServiceInfo service){
        for (KitchenTask task : assignedTasks) {
            if(task.getSummarySheetId() == service.getSummarySheetId()){
                assignedTasks.remove(task);
            }
        }
    }
}
