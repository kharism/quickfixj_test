package eaciit.quickfixsniff;
import quickfix.*;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Hello world!
 *
 */
public class App 
{
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private static final Logger log = LoggerFactory.getLogger(App.class);
    //private static Banzai banzai;
    private boolean initiatorStarted = false;
    private Initiator initiator = null;
    //private JFrame frame = null;

    public synchronized void logon() {
        if (!initiatorStarted) {
            try {
                initiator.start();
                initiatorStarted = true;
            } catch (Exception e) {
                log.error("Logon failed", e);
            }
        } else {
            for (SessionID sessionId : initiator.getSessions()) {
                Session.lookupSession(sessionId).logon();
            }
        }
    }
    public void logout() {
        for (SessionID sessionId : initiator.getSessions()) {
            Session.lookupSession(sessionId).logout("user requested");
        }
    }
    public void stop() {
        shutdownLatch.countDown();
    }
    public static void main( String[] args )throws Exception
    {
        if (args.length != 1) return;
        String fileName = args[0];
        Application application = new SniffingApp();
        SessionSettings settings = new SessionSettings(new FileInputStream(fileName));
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();
        Initiator initiator = new SocketInitiator
        (application, storeFactory, settings, logFactory, messageFactory);
        
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                System.out.println("W: interrupt received, killing server…");
                initiator.stop();
                //context.close();
            }
        });
        System.out.println("StartListening");
        initiator.start();
        while(true) 
        {  
            
        }
        
        //System.out.println( "Hello World!" );
    }
}
