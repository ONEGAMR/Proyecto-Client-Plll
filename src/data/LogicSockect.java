package data;

import application.Logic;
import domain.Meal;
import domain.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogicSockect {

public static boolean us_confirm;
public static ArrayList<Meal> meals = new ArrayList<>();
private static String beforeRoute;

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
    }

    public static String validateUser() {

        String user = null;

        if(SocketClient.validate == -1) {
            user = "Numero de identificacion no registrado";
        }

        if(SocketClient.validate == 1) {
            user = "Contraseña incorrecta";
        }


        return user;
    }

    public synchronized static void setListMeals(String meal){

        String[] mealData = meal.split(",");

        meals.add(new Meal(mealData[1],Integer.parseInt(mealData[2])));
        System.out.println(mealData[1] +" "+Integer.parseInt(mealData[2]) + "probando si esta bien");
        System.out.println(meals);
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
