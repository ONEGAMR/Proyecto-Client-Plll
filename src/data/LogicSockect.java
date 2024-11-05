package data;

import application.LogInController;
import application.Logic;
import domain.Meal;
import domain.Recharge;
import domain.User;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class LogicSockect {
    private static LogInController activeController;
    private static boolean us_confirm;
    public static ArrayList<Meal> meals = new ArrayList<>();
    public static ArrayList<Recharge> recharges = new ArrayList<>();
    public static int listSize;
    private static int totalimages;
    public static int imageCount;

    // Getters and Setters
    public static void setActiveController(LogInController controller) {
        activeController = controller;
    }

    public synchronized static void setUs_confirm(boolean value) {
        us_confirm = value;
    }

    public synchronized static boolean confirm() {
        return us_confirm;
    }

    public synchronized static ArrayList<Meal> getListMeals() {
        return meals;
    }

    public synchronized static ArrayList<?> getListRecharges() {
        return recharges;
    }

    public static void resetSizeList() {
        listSize = 0;
    }

    // Operaciones de comidas
    public synchronized static void clearMeals() {
        meals.clear();
    }

    public synchronized static void setListMeals(String meal) {
        String[] mealData = meal.split(",");
        meals.add(new Meal(mealData[1], Integer.parseInt(mealData[2]), mealData[3]));
    }

    public synchronized static void setListMealsOrder(String meal) {
        String[] mealData = meal.split(",");
        meals.add(new Meal(mealData[1], Integer.parseInt(mealData[2]), Double.parseDouble(mealData[3])));
    }

    // Recharge Operations
    public synchronized static void setListRecharge(String recharge) {
        String[] rechargeData = recharge.split(",");
        recharges.add(new Recharge(
                Double.parseDouble(rechargeData[1]),
                LocalDate.parse(rechargeData[2])
        ));
    }

    // Operaciones de usuarios
    public static void fullUser(String user) {
        if (Logic.user == null) {
            Logic.user = new User();
        }

        String[] userData = user.split(",");
        Logic.user.setCarnet(userData[2]);
        Logic.user.setNombre(userData[3]);
        Logic.user.setCorreoElectronico(userData[4]);
        Logic.user.setTelefono(Integer.parseInt(userData[5]));
        Logic.user.setEstaActivo(Boolean.parseBoolean(userData[6]));
        Logic.user.setFechaIngreso(LocalDate.parse(userData[7]));
        Logic.user.setGenero(userData[8].charAt(0));
        Logic.user.setDineroDisponible(Double.parseDouble(userData[9]));
        Logic.user.setPassword(userData[10]);
        Logic.user.setTypeUser(userData[11]);
        Logic.user.setRoutePhoto(userData[12]);
    }

    public static String validateUser() {
        if (SocketClient.validate == -1) {
            return "Numero de identificacion no registrado";
        }
        if (SocketClient.validate == 1) {
            return "Contraseña incorrecta";
        }
        if (Logic.user != null && Logic.user.getTypeUser() != null && !Logic.user.getTypeUser().equals("estudiante")) {
            SocketClient.setValidate(-2);
            return "El perfil no pertenece a un estudiante";
        }
        if (Logic.user != null && !Logic.user.isEstaActivo()) {
            return "El perfil no esta inactivo";
        }
        return null;
    }

    // Manejo de imagenes
    public static void handleImageTransfer(String message) {
        String[] parts = message.split(",", 3);
        if (parts[0].equals("imageCount")) {
            totalimages = Integer.parseInt(parts[1]);
            cleanImageDirectory();
            return;
        }

        if (parts.length != 3) return;

        saveImage(parts[1], parts[2]);
    }

    private static void saveImage(String fileName, String base64Image) {
        try {
            File directory = new File("src/images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            byte[] imageData = Base64.getDecoder().decode(base64Image);
            File outputFile = new File("src/images/" + fileName);
            Files.write(outputFile.toPath(), imageData);

            imageCount++;
            updateImageProgressUI(fileName);

        } catch (IOException e) {
            System.err.println("Error saving image " + fileName + ": " + e.getMessage());
        }
    }

    private static void updateImageProgressUI(String fileName) {
        if (activeController != null && activeController.message != null) {
            Platform.runLater(() -> {
                String progressMessage = String.format("%s (%d/%d)", fileName, imageCount, totalimages);
                activeController.message.setTextFill(Color.rgb(93, 93, 93));
                activeController.message.setText(progressMessage);
            });
        }
    }

    private static void cleanImageDirectory() {
        File directory = new File("src/images");
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                Arrays.stream(files)
                        .filter(File::isFile)
                        .forEach(file -> {
                            if (!file.delete()) {
                                System.err.println("Could not delete file: " + file.getName());
                            }
                        });
            }
        }
    }

    // Metodos de Synchronization
    public static void SleepList() {
        waitUntilCondition(() -> meals.size() < listSize);
    }

    public static void SleepListRecharges() {
        waitUntilCondition(() -> recharges.size() < listSize);
    }

    public static void SleepListImages() {
        waitUntilCondition(() -> imageCount < totalimages);
    }

    private static void waitUntilCondition(java.util.function.BooleanSupplier condition) {
        if (listSize > 0 || totalimages > 0) {
            try {
                while (condition.getAsBoolean()) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<String> separarPalabras(String texto) {
        return new ArrayList<>(Arrays.asList(texto.split(",")));
    }
}