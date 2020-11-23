public class Config {

    static final int MAX_URL_SIZE = 256;
    static final int MIN_WAIT_TIME_MILLIS = 200;
    static final int MAX_URLS_PER_HOST = 100;
    // two days
    final static long DEFAULT_WAIT_TIME_BEFORE_RECRAWL = 2 * 24 * 60 * 60 * 1000;
    public static int NUMBER_OF_SPIDERS = 31;
    public static int MAX_RUN_TIME_HOURS = 1;
    //static long MAX_RUNTIME_MILLIS = 1000 * 60 * 60 * MAX_RUN_TIME_HOURS;
    static long MAX_RUNTIME_MILLIS = 300000;
    static long STOP_TIME_MILLIS;
    static int WAIT_BEFORE_RETRY_MILLIS = 20000;
    static int MAX_WAIT_ATTEMPTS = 2;

}
