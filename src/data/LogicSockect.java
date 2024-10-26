package data;

import application.Logic;
import application.ServiceViewGUIController;
import domain.Meal;
import domain.Recharge;
import domain.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogicSockect {

    public static boolean us_confirm;
    public static ArrayList<Meal> meals = new ArrayList<>();
    public static ArrayList<Recharge> recharges = new ArrayList<>();


    public static List<String> separarPalabras(String texto) {
        // Usa el método split() para separar por comas
        String[] palabrasArray = texto.split(",");
        // Convierte el arreglo a una lista y la retorna
        return new ArrayList<>(Arrays.asList(palabrasArray));
    }

    private static ServiceViewGUIController serviceController;
    private static int expectedMealCount = 0;

    public static void setServiceController(ServiceViewGUIController controller) {
        serviceController = controller;
    }

    public synchronized static void setListMeals(String meal) {
        String[] mealData = meal.split(",");
        meals.add(new Meal(mealData[1], Integer.parseInt(mealData[2])));

        if (serviceController != null) {
            serviceController.onMealReceived();
        }
    }

    public synchronized static void setExpectedMealCount(int count) {
        expectedMealCount = count;
    }

    public synchronized static int getExpectedMealCount() {
        return expectedMealCount;
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

        if(Logic.user != null && !Logic.user.getTypeUser().equals("estudiante")){

            SocketClient.setValidate(-2);
        }

        if(SocketClient.validate == -2) {
            user = "El perfil no pertenece a un estudiante";
        }

        return user;
    }

    public synchronized static void setListMealsOrder(String meal){

        String[] mealData = meal.split(",");
        System.out.println(mealData[1] +" "+Integer.parseInt(mealData[2])+" "+Integer.parseInt(mealData[3])+"  Esto llega");
        meals.add(new Meal(mealData[1],Integer.parseInt(mealData[2]),Integer.parseInt(mealData[3])));
        System.out.println(meals);
    }

    public synchronized static void setListRecharge(String recharge){

        String[] rechargeData = recharge.split(",");

        recharges.add(new Recharge(Double.parseDouble(rechargeData[1]),LocalDate.parse(rechargeData[2])));
    }
    public synchronized static ArrayList<?> getListRecharges(){
        return recharges;
    }

    public synchronized static ArrayList<?> getListMeals(){
        return meals;
    }

    public synchronized static void setUs_confirm(boolean us_confirm) {
        LogicSockect.us_confirm = us_confirm;
    }

    public synchronized static boolean confirm() {
        return us_confirm;
    }
}