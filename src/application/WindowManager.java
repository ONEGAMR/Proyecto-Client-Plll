package application;

import javafx.stage.Stage;
import java.util.HashSet;
import java.util.Set;

public class WindowManager {
    private static final Set<Stage> activeStages = new HashSet<>();
    private static final Set<Stage> notificationEnabledStages = new HashSet<>();

    public static void registerStage(Stage stage) {
        activeStages.add(stage);
        stage.setOnCloseRequest(e -> {
            activeStages.remove(stage);
            notificationEnabledStages.remove(stage);
        });
    }

    public static void enableNotificationsForStage(Stage stage) {
        notificationEnabledStages.add(stage);
    }

    public static void disableNotificationsForStage(Stage stage) {
        notificationEnabledStages.remove(stage);
    }

    public static Set<Stage> getActiveStages() {
        return new HashSet<>(activeStages);
    }

    public static Set<Stage> getNotificationEnabledStages() {
        return new HashSet<>(notificationEnabledStages);
    }
}