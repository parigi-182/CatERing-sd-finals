package catering.businesslogic.kitchen;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;
import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class KitchenTask{
    private int id;
    private int summarySheetId;
    private Time estimatedTime;
    private String quantity;
    private int shiftId;
    private boolean completed;
    private Recipe recipe;
    private User cook;

    public KitchenTask(){}

    public KitchenTask(Recipe recipe, User cook, int shiftId){
        this.recipe = recipe;
        this.cook = cook;
        this.shiftId = shiftId;
        completed = false;
    }

    public void detailTask(Time estimatedTime, String quantity){
        this.estimatedTime = estimatedTime;
        this.quantity = quantity;
    }
    public void editTask(int shiftId, User cook){
        this.shiftId = shiftId;
        this.cook = cook;
    }

    public static ObservableList<KitchenTask> loadKitchenTaskInfo(int task_id) {
        ObservableList<KitchenTask> result = FXCollections.observableArrayList();
        String query = "SELECT * FROM KitchenTask Where id = " + task_id;
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                KitchenTask kitchenTask = new KitchenTask();
                kitchenTask.id = rs.getInt("id");
                kitchenTask.summarySheetId = rs.getInt("summarySheetId");
                kitchenTask.estimatedTime = rs.getTime("estimatedTime");
                kitchenTask.quantity = rs.getString("quantity");
                kitchenTask.shiftId = rs.getInt("shiftId");
                kitchenTask.completed = rs.getBoolean("completed");
                kitchenTask.recipe = Recipe.loadRecipeById(rs.getInt("recipeId"));
                kitchenTask.cook = User.loadUserById(rs.getInt("cookId"));
                result.add(kitchenTask);
            }
        });
        
        return result;
    }
    public static void saveAllNewTask(List<KitchenTask> tasks){
        String query = "INSERT INTO catering.KitchenTask (summarySheetId, estimatedTime, quantity, shiftId, completed, recipeId, cookId) VALUES (?,?,?,?,?,?,?)";
        PersistenceManager.executeBatchUpdate(query, tasks.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException {
                ps.setInt(1, tasks.get(batchCount).summarySheetId);
                ps.setTime(2, tasks.get(batchCount).estimatedTime);
                ps.setString(3, tasks.get(batchCount).quantity);
                ps.setInt(4, tasks.get(batchCount).shiftId);
                ps.setBoolean(5, tasks.get(batchCount).completed);
                ps.setInt(6, tasks.get(batchCount).recipe.getId());
                ps.setInt(7, tasks.get(batchCount).cook.getId());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                tasks.get(count).id = rs.getInt(1);
                throw new UnsupportedOperationException("Unimplemented method 'handleGeneratedIds'");
            }
        });
    }

    public static void eraseOldTask(KitchenTask task){
        String query = "DELETE FROM KitchenTask WHERE id = " + task.id;
        PersistenceManager.executeUpdate(query);
    }

    public static void detailTask(KitchenTask task){
        String query = "UPDATE tasks SET quantity = " + task.quantity + ", estimatedTime = " +
        task.estimatedTime + "WHERE id = " + task.id;
        PersistenceManager.executeUpdate(query);
    }
    public static void updateTaskCompleted(KitchenTask task){
        String query = "UPDATE tasks SET completed = 1 WHERE id = " + task.id;
        PersistenceManager.executeUpdate(query);
    }

    public int getSummarySheetId() {
        return summarySheetId;
    }
    public int getId() {
        return id;
    }
    public boolean equals(KitchenTask obj) {
        return (this.id == obj.getId());
    }
}