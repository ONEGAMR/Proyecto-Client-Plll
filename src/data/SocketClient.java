package data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import application.Logic;
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


                        System.out.println(validate+"socket");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error al recibir mensajes del servidor: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
