package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogicSockect {

    public static List<String> separarPalabras(String texto) {
        // Usa el método split() para separar por comas
        String[] palabrasArray = texto.split(",");
        // Convierte el arreglo a una lista y la retorna
        return new ArrayList<>(Arrays.asList(palabrasArray));
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
}
