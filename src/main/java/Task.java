import java.util.List;
import java.util.ArrayList;
import org.sql2o.*;

public class Task {
  private int id;
  private String descriptions;
  private String dueDate;
  private String notes;

  public int getId() {
    return id;
  }

  public String getDescriptions() {
    return descriptions;
  }

  public String getDueDate() {
    return dueDate;
  }

  public String getNotes() {
    return notes;
  }

  public Task(String descriptions, String dueDate, String notes) {
    this.descriptions = descriptions;
    this.dueDate = dueDate;
    this.notes = notes;
  }

  @Override
  public boolean equals(Object otherTask){
    if (!(otherTask instanceof Task)) {
      return false;
    } else {
      Task newTask = (Task) otherTask;
      return this.getDescriptions().equals(newTask.getDescriptions()) &&
      this.getId() == newTask.getId() && this.getDueDate() == newTask.getDueDate();
    }
  }


  public static List<Task> all() {
    String sql = "SELECT id, descriptions, dueDate, notes FROM tasks ORDER BY dueDate";
    try(Connection con = DB.sql2o.open()) {
      return con.createQuery(sql).executeAndFetch(Task.class);
    }
  }

  public void save() {
    try(Connection con = DB.sql2o.open()) {
      String sql = "INSERT INTO tasks(descriptions, dueDate, notes) VALUES (:descriptions, :dueDate, :notes)";
      this.id = (int) con.createQuery(sql, true)
      .addParameter("descriptions", descriptions)
      .addParameter("dueDate", dueDate)
      .addParameter("notes", notes)
      .executeUpdate()
      .getKey();
    }
  }

  public static Task find(int id) {
    try(Connection con = DB.sql2o.open()) {
      String sql = "SELECT * FROM tasks where id=:id";
      Task task = con.createQuery(sql)
      .addParameter("id", id)
      .executeAndFetchFirst(Task.class);
      return task;
    }
  }

  public void update(String descriptions) {
    try(Connection con = DB.sql2o.open()) {
      String sql = "UPDATE tasks SET descriptions = :descriptions, dueDate = :dueDate, notes = :notes";
      con.createQuery(sql)
      .addParameter("descriptions", descriptions)
      .addParameter("dueDate", dueDate)
      .addParameter("notes", notes)
      .addParameter("id", id)
      .executeUpdate();
    }
  }

  public void addCategory(Category category) {
    try(Connection con = DB.sql2o.open()) {
      String sql = "INSERT INTO categories_tasks (category_id, task_id) VALUES (:category_id, :task_id)";
      con.createQuery(sql)
      .addParameter("category_id", category.getId())
      .addParameter("task_id", this.getId())
      .executeUpdate();
    }
  }

  public ArrayList<Category> getCategories() {
    try(Connection con = DB.sql2o.open()){
      String sql = "SELECT category_id FROM categories_tasks WHERE task_id = :task_id";
      List<Integer> categoryIds = con.createQuery(sql)
      .addParameter("task_id", this.getId())
      .executeAndFetch(Integer.class);

      ArrayList<Category> categories = new ArrayList<Category>();

      for (Integer categoryId : categoryIds) {
        String taskQuery = "Select * From categories WHERE id = :categoryId";
        Category category = con.createQuery(taskQuery)
        .addParameter("categoryId", categoryId)
        .executeAndFetchFirst(Category.class);
        categories.add(category);
      }
      return categories;
    }
  }

  public void delete() {
    try(Connection con = DB.sql2o.open()) {
      String deleteQuery = "DELETE FROM tasks WHERE id = :id;";
      con.createQuery(deleteQuery)
      .addParameter("id", id)
      .executeUpdate();

      String joinDeleteQuery = "DELETE FROM categories_tasks WHERE task_id = :taskId";
      con.createQuery(joinDeleteQuery)
      .addParameter("taskId", this.getId())
      .executeUpdate();
    }
  }
}
