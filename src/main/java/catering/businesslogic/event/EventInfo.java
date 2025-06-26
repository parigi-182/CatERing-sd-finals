package catering.businesslogic.event;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import catering.businesslogic.user.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

public class EventInfo implements EventItemInfo {
    private int id;
    private String name;
    private Date dateStart;
    private Date dateEnd;
    private int participants;
    private User organizer;
    private String client;
    private String description;
    private User chef; 
    private String note;
    private String state;

    private ObservableList<ServiceInfo> services;

    public EventInfo(String name) {
        this.name = name;
        id = 0;
    }

    public ObservableList<ServiceInfo> getServices() {
        return FXCollections.unmodifiableObservableList(this.services);
    }

    public String toString() {
        return name + ": " + dateStart + "-" + dateEnd + ", " + participants + " pp. (" + organizer.getUserName() + ")";
    }

    // STATIC METHODS FOR PERSISTENCE

    public static ObservableList<EventInfo> loadAllEventInfo() {
        ObservableList<EventInfo> all = FXCollections.observableArrayList();
        String query = "SELECT * FROM Events WHERE true";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                String n = rs.getString("name");
                EventInfo e = new EventInfo(n);
                e.id = rs.getInt("id");
                e.dateStart = rs.getDate("date_start");
                e.dateEnd = rs.getDate("date_end");
                e.participants = rs.getInt("expected_participants");
                int org = rs.getInt("organizer_id");
                e.organizer = User.loadUserById(org);
                e.client = rs.getString("client");
                e.description = rs.getString("description");
                int chef = rs.getInt("chef_id");
                e.note = rs.getString("note");
                e.state = rs.getString("state");
                all.add(e);
            }
        });

        for (EventInfo e : all) {
            e.services = ServiceInfo.loadServiceInfoForEvent(e.id);
        }
        return all;
    }
}
