package crawling;

public class Config {

    //general parameters
    public static int NUMBER_OF_SPIDERS = 1;
    public static int MAX_RUN_TIME_MINUTES = 5;
    public static long MAX_RUNTIME_MILLIS = 1000 * 60 * MAX_RUN_TIME_MINUTES;
    public static long STOP_TIME_MILLIS;

    // parameters set for robustness
    public static final int MAX_URL_SIZE = 256;
    public static final int MAX_URLS_PER_HOST = 5;

    // parameter set for fairness
    public static final int MIN_WAIT_TIME_BEFORE_RECONTACTING_HOST_MILLIS = 200;

    // parameter used for refreshing pages if the last modified estimate fails
    public final static long DEFAULT_WAIT_TIME_BEFORE_RECRAWL = 2 * 24 * 60 * 60 * 1000;   //two days

    // parameters used for refreshing, especially needed at the beginning to avoid
    // the refresher thread to get stuck
    public static long MAX_WAIT_REFRESHER = 1000 * 60;     //one minute
    public static long REFRESHER_WAIT_BEFORE_CHECKING_PAGE_TO_REFRESH_MILLIS = 1000 * 20;    //20 seconds

    // parameters for the frontier on how to behave in case of empty front queues
    public static int WAIT_BEFORE_RETRY_MILLIS = 20 * 1000;    //20 seconds
    public static int MAX_WAIT_ATTEMPTS = 2;

}
