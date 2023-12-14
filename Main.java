public class Main {
    
    public static void main(String[] args) {
        MyLogger.loggerConfig();
        MyLogger.logger.info("This is info");
        MyLogger.logger.warning("This is warning");
        MyLogger.logger.severe("This is severe");
    }
}
