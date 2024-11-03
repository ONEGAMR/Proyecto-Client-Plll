package data;

import application.Logic;
import application.NotificationManager;
import application.WindowManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

public class SocketClient {

    private static final int PUERTO = 12345;
    public static int validate;
    public static boolean isConnected = false;
    private static String HOST;
    private static PrintWriter salida;
    private static BufferedReader entrada;
    private static Socket socket;

    public static void closeWindows(Node node, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(Logic.class.getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            WindowManager.registerStage(stage);

            if (path.equals("/presentation/MainLayout.fxml")) {
                WindowManager.enableNotificationsForStage(stage);
            }

            stage.show();

            Stage currentStage = (Stage) node.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reset() {
        validate = 0;
    }

    public static void setValidate(int validate) {
        SocketClient.validate = validate;
    }

    // Método estático para conectarse al servidor y comenzar a escuchar mensajes
    public static boolean connectToServer(String host) {
        HOST = host;
        new Thread(() -> {
            initThread();
            receiveMessages();
        }).start();

        Logic.sleepThread();
        return isConnected;
    }

    public static void disconnectFromServer() {

        try {
            isConnected = false;
            Logic.sleepTrhead();

            if (socket != null && !socket.isClosed()) {

                if (salida != null) {

                    salida.close();
                    salida = null;
                }

                if (entrada != null) {

                    entrada.close();
                    entrada = null;
                }

                socket.close();
                socket = null;

                System.out.println("Desconectado del servidor");
            }
        } catch (IOException e) {

            System.out.println("Error al desconectarse del servidor: " + e.getMessage());
        }
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
            isConnected = true;
        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }

    public synchronized static void receiveMessages() {

        while (isConnected) {
            try {
                if (entrada == null || socket == null || socket.isClosed()) {

                    System.out.println("Conexion cerrada, terminando loo[ de recepcion");
                    break;
                }

                String message = entrada.readLine();

                if (message == null) {
                    System.out.println("Conexion cerrada por el servidor");
                    isConnected = false;
                    break;
                }


                // Procesar el mensaje y actualizar la variable 'validate'
                if (LogicSockect.separarPalabras(message).get(0).equals("user")) {
                    validate = Integer.parseInt(LogicSockect.separarPalabras(message).get(1));
                    //si todo esta bien llena los datos del usuario con los datos recibidos
                    System.out.println(message + " mensaje de llegada");
                    if (LogicSockect.separarPalabras(message).size() > 2) {

                        LogicSockect.fullUser(message);
                    }
                }

                //recibe la confirmacion de que se actualizo el usuario
                if (LogicSockect.separarPalabras(message).get(0).equals("us_confirm")) {
                    System.out.println(message);
                    LogicSockect.setUs_confirm(Boolean.parseBoolean(LogicSockect.separarPalabras(message).get(1)));
                }

                if (LogicSockect.separarPalabras(message).get(0).equals("listSize")) {
                    LogicSockect.listSize = Integer.parseInt(LogicSockect.separarPalabras(message).get(1));
                    System.out.println(message);
                }

                if (LogicSockect.separarPalabras(message).get(0).equals("listMeals")) {

                    System.out.println(message);
                    LogicSockect.setListMeals(message);

                }

                if (LogicSockect.separarPalabras(message).get(0).equals("listOrder")) {

                    System.out.println(message + "dentra a list order");
                    LogicSockect.setListMealsOrder(message);
                }

                if (LogicSockect.separarPalabras(message).get(0).equals("listRecharge")) {

                    System.out.println(message + "dentra a list recharge");
                    LogicSockect.setListRecharge(message);
                }

                if (LogicSockect.separarPalabras(message).get(0).equals("image") ||
                        LogicSockect.separarPalabras(message).get(0).equals("singleImage") ||
                        LogicSockect.separarPalabras(message).get(0).equals("imageCount")) {
                    LogicSockect.handleImageTransfer(message);
                }

                //se recibe la recargar
                if (LogicSockect.separarPalabras(message).get(0).equals("newBalance")) {


                    Logic.user.setDineroDisponible(Double.parseDouble(message.split(",")[1]));
                    try {
                        //ServiceRequestController.getInstance().setBalance(Double.parseDouble(message.split(",")[1]));
                    } catch (NullPointerException e) {
                        System.out.println("La ventana no está abierta o el método getInstance() devolvió null.");
                    } catch (NumberFormatException e) {
                        System.out.println("El mensaje no contiene un número válido para el balance.");
                    } catch (Exception e) {
                        System.out.println("Ocurrió un error inesperado: " + e.getMessage());
                    }

                }

                //Se recibe el cambio de estado de los pedidos
                if (LogicSockect.separarPalabras(message).get(0).equals("notifyStatus")) {

                    System.out.println("Pedido" + message.split(",")[1]);

                    // Mostrar la notificación en todas las ventanas activas
                    NotificationManager.showNotificationInAllWindows(message.split(",")[1]);

                }

                if (LogicSockect.separarPalabras(message).get(0).equals("BD")) {
                    System.out.println(LogicSockect.separarPalabras(message).get(1));
                }

            } catch (IOException e) {

                if(!isConnected){
                    System.out.println("Conexion cerrada intencionalmente");
                    break;
                }

                System.out.println("Error al recibir mensajes del servidor: " + e.getMessage());
                isConnected = false;
                break;
            }
        }

        if(!isConnected){
            disconnectFromServer();
        }

    }
}