package eaciit.quickfixsniff;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import java.lang.invoke.MethodHandles;
import java.util.stream.IntStream;
 
public class LoggerRoot {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
 
    public static void main(String... args) {
        IntStream.rangeClosed(1, 10).forEach(counter -> {
            logger.info("Counter:" + counter);
        });
    }
}