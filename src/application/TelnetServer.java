package application;

import java.io.*;
import java.net.Socket;

public class TelnetServer {

    private static Socket serverSocket;
    private static PrintStream out;
    private static InputStreamReader in;
    private static int port = 5554;

    public static void connect() throws IOException {
        serverSocket = new Socket("localhost", port);

        out = new PrintStream(serverSocket.getOutputStream());
        in = new InputStreamReader(serverSocket.getInputStream());
        int data = in.read();
        String input = "";
        while(data != -1){
            char theChar = (char) data;
            input += theChar;
            if(input.contains("OK"))
                break;
            data = in.read();
        }

        String[] inputs = input.split("\n");
        String filePath = "";
        for (String word: inputs) {
            if(word.contains("emulator_console_auth_token"))
                filePath = word;
        }

        filePath = filePath.replace("'", "").trim();

        File file = new File(filePath);
        System.out.println("Filepath: " + filePath);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String authToken = reader.readLine();

        out.println("auth " + authToken);
        System.out.println("auth " +authToken);

    }

    public static void setSensor(String command) {
        if(out != null) {
            out.println("sensor set ");
            System.out.println("sensor set " + command);
        }
    }

    public static void sendSMS(String command) {
        if(out != null) {
            out.println("sms send 000000000 " + command);
        }
    }
}
