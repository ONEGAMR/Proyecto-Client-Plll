package data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import application.ServiceViewGUIController;
import javafx.application.Platform;


public class SocketClient {

    private static String HOST;
    private static final int PUERTO = 12345;
    private static PrintWriter salida;
    private static BufferedReader entrada;
    private static Socket socket;
    public static int validate;

    public static void reset() {
        validate = 0;
    }

    public static void setValidate(int validate) {
        SocketClient.validate = validate;
    }

    // Método estático para conectarse al servidor y comenzar a escuchar mensajes
    public static void connectToServer(String host) {
        HOST = host;
        new Thread(() -> { initThread(); receiveMessages();}).start();
    }

    // Método estático para enviar un mensaje al servidor
    public static void sendMessage(String message) {
        if (salida != null) {
            salida.println(message);  // Enviar mensaje al servidor
            salida.flush();
        }
    }

    // Método estático para inicializar la conexión con el servidor
    public static void initThread() {
        try {
            socket = new Socket(HOST, PUERTO);  // Conectar al servidor
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // Preparar para leer mensajes
            salida = new PrintWriter(socket.getOutputStream(), true);  // Preparar para enviar mensajes

            System.out.println("Conexión con el servidor establecida.");

        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }

    public synchronized static void receiveMessages() {

        while (true) {
            try {
                String message = entrada.readLine();

                if (message != null) {
                    // Procesar el mensaje y actualizar la variable 'validate'
                    if(LogicSockect.separarPalabras(message).get(0).equals("user")) {
                        validate = Integer.parseInt(LogicSockect.separarPalabras(message).get(1));
                        //si todo esta bien llena los datos del usuario con los datos recibidos
                        System.out.println(message+ " mensaje de llegada");
                        if(LogicSockect.separarPalabras(message).size() >  2){

                            LogicSockect.fullUser(message);
                        }
                    }

                    //recibe la confirmacion de que se actualizo el usuario
                    if(LogicSockect.separarPalabras(message).get(0).equals("us_confirm")){
                        System.out.println(message);
                        LogicSockect.setUs_confirm(Boolean.parseBoolean(LogicSockect.separarPalabras(message).get(1)));
                    }

                    if(LogicSockect.separarPalabras(message).get(0).equals("listMeals")){

                        System.out.println(message);
                        LogicSockect.setListMeals(message);

                    }

                    if(LogicSockect.separarPalabras(message).get(0).equals("listOrder")){

                        System.out.println(message +"dentra a list order");
                        LogicSockect.setListMealsOrder(message);
                    }

                    if(LogicSockect.separarPalabras(message).get(0).equals("listRecharge")){

                        System.out.println(message +"dentra a list recharge");
                        LogicSockect.setListRecharge(message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error al recibir mensajes del servidor: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}