package catering.businesslogic.event;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import catering.businesslogic.menu.Menu;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

public class ServiceInfo implements EventItemInfo {
    private int id;
    private String name;
    private Date date;
    private Time timeStart;
    private Time timeEnd;
    private int participants;
    private String description;
    private String place;
    private String note;
    private String state;
    private int summarySheetId;
    private Menu menu;

    public ServiceInfo(String name) {
        this.name = name;
    }


    public String toString() {
        return name + ": " + date + " (" + timeStart + "-" + timeEnd + "), " + participants + " pp.";
    }

    // STATIC METHODS FOR PERSISTENCE

    public static ObservableList<ServiceInfo> loadServiceInfoForEvent(int event_id) {
        ObservableList<ServiceInfo> result = FXCollections.observableArrayList();
        String query = "SELECT id, name, service_date, time_start, time_end, expected_participants, description, place, note, state, summarySheetId " +
                "FROM Services WHERE event_id = " + event_id;
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                String s = rs.getString("name");
                ServiceInfo serv = new ServiceInfo(s);
                serv.id = rs.getInt("id");
                serv.date = rs.getDate("service_date");
                serv.timeStart = rs.getTime("time_start");
                serv.timeEnd = rs.getTime("time_end");
                serv.participants = rs.getInt("expected_participants");
                serv.description = rs.getString("description");
                serv.place = rs.getString("place");
                serv.note = rs.getString("note");
                serv.state = rs.getString("state");
                serv.summarySheetId = rs.getInt("summarySheetId");
                result.add(serv);
            }
        });

        return result;
    }
    public int getSummarySheetId() {
        return summarySheetId;
    }
    public void setMenu(Menu menu){
        this.menu = menu;
    }
    public Menu getMenu() {
        return menu;
    }
    public int getId() {
        return id;
    }
}
