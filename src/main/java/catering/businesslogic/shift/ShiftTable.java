package catering.businesslogic.shift;

import java.time.LocalDateTime;
import java.util.ArrayList;

import catering.businesslogic.event.EventInfo;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.user.User;

public class ShiftTable {
    private ArrayList<Shift> shifts;

    public ShiftTable(){
        shifts = new ArrayList<>();
    }

    public void addShift(LocalDateTime shiftStart, LocalDateTime shiftEnd, User staff){
        shifts.add(new Shift(shiftStart, shiftEnd, staff));
    }


    public void dismiss(EventInfo event){
        for (ServiceInfo serviceInfo : event.getServices()) {
            dismiss(serviceInfo);
        }
    }
    public void dismiss(ServiceInfo service){
        for (Shift shift : shifts) {
            shift.dismiss(service);
        }
    }

}
