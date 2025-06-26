package catering.businesslogic.kitchen;

import java.sql.Time;
import java.util.ArrayList;

import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.menu.MenuItem;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;

public class SummarySheet {
    private int id;
    private User owner;
    private ArrayList<Recipe> suggestedRecipes;
    private ArrayList<Recipe> recipes;
    private ArrayList<KitchenTask> taskList;
    private int serviceInfoId;

    public SummarySheet(ServiceInfo service){
        suggestedRecipes = new ArrayList<>();
        recipes = new ArrayList<>();
        taskList = new ArrayList<>();
        this.serviceInfoId = service.getId();
        for (MenuItem menuItem : service.getMenu().getAllItems()) {
            recipes.add(menuItem.getItemRecipe());
        }
    }

    public void addRecipe(Recipe recipe){
        recipes.add(recipe);
    }
    public void removeRecipe(Recipe recipe){
        recipes.remove(recipe);
    }
    public void assignTask(Recipe recipe, int shiftId, User cook){
        taskList.add(new KitchenTask(recipe, cook, shiftId));
        //TO DO: ASSIGN TASK SHIFT TABLE
    }
    public void detailTask(KitchenTask task, Time estimatedTime, String quantity){
        int i = taskList.indexOf(task);
        taskList.get(i).detailTask(estimatedTime, quantity);
        //TO DO: UPDATE SHIFT TABLE
    }
    public void editTask(KitchenTask task, int shiftId, User cook){
        int i = taskList.indexOf(task);
        taskList.get(i).editTask(shiftId, cook);
        //TO DO: UPDATE SHIFT TABLE
    }
    public void removeTask(KitchenTask task){
        taskList.remove(task);

        //TO DO: TASK DISMISS
    }

    public void orderTask(int position, Recipe recipe){
        int i = recipes.indexOf(recipe);
        Recipe swappedRecipe = recipes.get(position);
        recipes.set(position, recipe);
        recipes.set(i, swappedRecipe);
    }
    public void valueSuggestion(Recipe recipeToValue, boolean result){
        int i = suggestedRecipes.indexOf(recipeToValue);
        if(result){
            recipes.add(suggestedRecipes.get(i));
        }
        suggestedRecipes.remove(i);
    }
    public int getServiceInfoId() {
        return serviceInfoId;
    }
}
