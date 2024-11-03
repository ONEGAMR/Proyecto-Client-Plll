package data;

import application.Logic;
import domain.Meal;
import domain.Recharge;
import domain.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class LogicSockect {

    public static boolean us_confirm;
    public static ArrayList<Meal> meals = new ArrayList<>();
    public static ArrayList<Recharge> recharges = new ArrayList<>();
    public static int listSize;
    public static int totalimages;
    public static int imageCount;

    // Add this new method
    public synchronized static void clearMeals() {
        meals.clear();
    }

    // In LogicSockect.setListMeals
    public synchronized static void setListMeals(String meal) {
        String[] mealData = meal.split(",");
        System.out.println("Adding meal: " + Arrays.toString(mealData));
        meals.add(new Meal(mealData[1], Integer.parseInt(mealData[2]), mealData[3]));
        System.out.println("Current meals size: " + meals.size());
    }


    public static List<String> separarPalabras(String texto) {
        // Usa el método split() para separar por comas
        String[] palabrasArray = texto.split(",");
        // Convierte el arreglo a una lista y la retorna
        return new ArrayList<>(Arrays.asList(palabrasArray));
    }

    public static void fullUser(String user) {
        if (Logic.user == null) {
            Logic.user = new User();  // Asegurar la inicialización
        }

        Logic.user.setCarnet(user.split(",")[2]);
        Logic.user.setNombre(user.split(",")[3]);
        Logic.user.setCorreoElectronico(user.split(",")[4]);
        Logic.user.setTelefono(Integer.parseInt(user.split(",")[5]));
        Logic.user.setEstaActivo(Boolean.parseBoolean(user.split(",")[6]));
        Logic.user.setFechaIngreso(LocalDate.parse(user.split(",")[7]));
        Logic.user.setGenero(user.split(",")[8].charAt(0));
        Logic.user.setDineroDisponible(Double.parseDouble(user.split(",")[9]));
        Logic.user.setPassword(user.split(",")[10]);
        Logic.user.setTypeUser(user.split(",")[11]);
        Logic.user.setRoutePhoto(user.split(",")[12]);
    }

    public synchronized static String validateUser() {

        String user = null;

        if(SocketClient.validate == -1) {
            user = "Numero de identificacion no registrado";
        }

        if(SocketClient.validate == 1) {
            user = "Contraseña incorrecta";
        }

        if(Logic.user != null && Logic.user.getTypeUser() != null  && !Logic.user.getTypeUser().equals("estudiante")){

            SocketClient.setValidate(-2);
        }

        if(SocketClient.validate == -2) {
            user = "El perfil no pertenece a un estudiante";
        }

        if(Logic.user != null && !Logic.user.isEstaActivo()){

            user = "El perfil no esta inactivo";
        }

        return user;
    }

    public synchronized static void setListMealsOrder(String meal){

        String[] mealData = meal.split(",");
        meals.add(new Meal(mealData[1],Integer.parseInt(mealData[2]),Double.parseDouble(mealData[3])));
        System.out.println(meals);
    }

    public synchronized static void setListRecharge(String recharge){

        String[] rechargeData = recharge.split(",");

        recharges.add(new Recharge(Double.parseDouble(rechargeData[1]),LocalDate.parse(rechargeData[2])));
    }
    public synchronized static ArrayList<?> getListRecharges(){
        return recharges;
    }

    public synchronized static ArrayList<Meal> getListMeals(){
        return meals;
    }

    public synchronized static void setUs_confirm(boolean us_confirm) {
        LogicSockect.us_confirm = us_confirm;
    }

    public synchronized static boolean confirm() {
        return us_confirm;
    }

    public static void handleImageTransfer(String message) {


        String[] parts = message.split(",", 3);
        if (parts[0].equals("imageCount")) {
            System.out.println("Preparing to receive " + parts[1] + " images");
            totalimages = Integer.parseInt(parts[1]);
            return;
        }

        if (parts.length != 3) return;

        String imageType = parts[0];
        String fileName = parts[1];
        String base64Image = parts[2];

        try {
            // Crear el directorio si no existe
            File directory = new File("src/images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Decodificar la imagen
            byte[] imageData = Base64.getDecoder().decode(base64Image);

            // Guardar la imagen
            File outputFile = new File("src/images/" + fileName);
            Files.write(outputFile.toPath(), imageData);

            System.out.println("Image saved: " + fileName);

            imageCount++;
            System.out.println("imagen sa: "+imageCount);

        } catch (IOException e) {
            System.out.println("Error saving image " + fileName + ": " + e.getMessage());
        }
    }

    public static void SleepList(){

        if(listSize > 0) {
            try {
                while (meals.size() < listSize) {

                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void SleepListRecharges(){

        if(listSize > 0) {
            try {
                while (recharges.size() < listSize) {

                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void SleepListImages(){

        System.out.println("Cuenta "+imageCount);
        if(totalimages > 0) {
            try {
                while (imageCount < totalimages) {

                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static  void resetSizeList(){

        listSize = 0;
    }
}