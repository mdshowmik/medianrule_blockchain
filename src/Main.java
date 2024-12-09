/*import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        Controller controller = new Controller();
        controller.startApplication();
    }
}*/


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException, IOException {
        Controller controller = new Controller();
        try (PrintStream fileOut = new PrintStream(new FileOutputStream("output.txt"))) {
            controller.startApplication(fileOut);  // Redirect output to file
        }
    }
}
