import utils.MenuSystem;

public class Main {

    public static void main(String[] args) {
        try {
            MenuSystem menuSystem = new MenuSystem();
            menuSystem.start();

        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}