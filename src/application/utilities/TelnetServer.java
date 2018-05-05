package application.utilities;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.Socket;

public class TelnetServer {

    private static PrintStream out;
    private static InputStreamReader in;
    private static int port;

    public static void connect(int portNumber) {
        port = portNumber;

        Task task = new Task<Void>() {

            @Override
            public Void call() throws IOException {
                Socket serverSocket = new Socket("localhost", port);

                out = new PrintStream(serverSocket.getOutputStream());
                in = new InputStreamReader(serverSocket.getInputStream());

                int data = in.read();
                StringBuilder stringBuilder = new StringBuilder();
                while (data != -1) {
                    char ch = (char) data;
                    stringBuilder.append(ch);
                    if (stringBuilder.toString().contains("OK"))
                        break;
                    data = in.read();
                }

                String[] inputs = stringBuilder.toString().split("\n");
                String filePath = "";
                for (String word : inputs) {
                    if (word.contains("emulator_console_auth_token"))
                        filePath = word;
                }

                filePath = filePath.replace("'", "").trim();

                File file = new File(filePath);

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String authToken = reader.readLine();

                out.println("auth " + authToken);

                return null;
            }
        };

        new Thread(task).start();
    }

    public static void setSensor(String command) {
        if(out != null)
            out.println("sensor set " + command);
    }

    public static void sendSMS(String command) {
        if(out != null) {
            out.println("sms send " + command);
        }
    }

    public static void makeCall(String command) {
        if(out != null) {
            out.println("gsm call " + command);
        }
    }

    public static void holdCall(String command) {
        if(out != null) {
            out.println("gsm hold " + command);
        }
    }

    public static void unHoldCall(String command) {
        if(out != null) {
            out.println("gsm accept " + command);
        }
    }

    public static void endCall(String command) {
        if(out != null) {
            out.println("gsm cancel " + command);
        }
    }

    public static void networkSpeed(String command) {
        if(out != null) {
            out.println("network speed " + command);
        }
    }

    public static void gsmSignal(String command) {
        if(out != null) {
            out.println("gsm signal-profile " + command);
        }
    }

    public static void voiceStatus(String command) {
        if(out != null) {
            out.println("gsm voice " + command);
        }
    }

    public static void dataStatus(String command) {
        if(out != null) {
            out.println("gsm data " + command);
        }
    }

    public static void powerCapacity(String command) {
        if(out != null) {
            out.println("power capacity " + command);
        }
    }

    public static void batteryHealth(String command) {
        if(out != null) {
            out.println("power health " + command);
        }
    }

    public static void batteryStatus(String command) {
        if(out != null) {
            out.println("power status " + command);
        }
    }

    public static void setCharging(String command) {
        if(out != null) {
            out.println("power ac " + command);
        }
    }

    public static void setLocation(String command) {
        if(out != null) {
            out.println("geo fix " + command);
            System.out.println("geo fix " + command);
        }
    }
}
