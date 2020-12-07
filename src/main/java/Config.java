public class Config {

    //general parameters
    public static int NUMBER_OF_SPIDERS = 600;
    public static int MAX_RUN_TIME_MINUTES = 2;
    static long MAX_RUNTIME_MILLIS = 1000 * 60 * MAX_RUN_TIME_MINUTES;
    static long STOP_TIME_MILLIS;

    // parameters set for robustness
    static final int MAX_URL_SIZE = 256;
    static final int MAX_URLS_PER_HOST = 100;

    // parameter set for fairness
    static final int MIN_WAIT_TIME_BEFORE_RECONTACTING_HOST_MILLIS = 200;

    // parameter used for refreshing pages if the last modified estimate fails
    final static long DEFAULT_WAIT_TIME_BEFORE_RECRAWL = 2 * 24 * 60 * 60 * 1000;   //two days

    // parameters used for refreshing, especially needed at the beginning to avoid
    // the refresher thread to get stuck
    static long MAX_WAIT_REFRESHER = 1000 * 60;     //one minute
    static long REFRESHER_WAIT_BEFORE_CHECKING_PAGE_TO_REFRESH_MILLIS = 1000 * 20;    //20 seconds

    // parameters for the frontier on how to behave in case of empty front queues
    static int WAIT_BEFORE_RETRY_MILLIS = 20 * 1000;    //20 seconds
    static int MAX_WAIT_ATTEMPTS = 2;

}
