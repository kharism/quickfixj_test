package eaciit.quickfixsniff;
import quickfix.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import java.util.*;
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
    public static Map<String,String> ReadConfig(FileReader fis)throws Exception{
        int bufferSize = 8 * 1024;
        HashMap<String,String> result = new HashMap<String,String>();
        BufferedReader bis = new BufferedReader(fis);
        String line = bis.readLine();
        while(line!=null){
            if(line.contains("=")){
                String[] lineSplited = line.split("=");
                if(lineSplited.length<2){
                    line = bis.readLine();
                    continue;
                }
                result.put(lineSplited[0],String.join("=",lineSplited[1]));
            }
            line = bis.readLine();
        }
        bis.close();
        fis.close();
        return result;
    }
    public static void main( String[] args )throws Exception
    {
        if (args.length != 1) return;
        String fileName = args[0];
        Map<String,String> config = ReadConfig(new FileReader(fileName));
        Application application = null;
        try{
            application = new SniffingApp(config);
        }catch(UnknownHostException ex){
            System.out.println("Unknown Host Found");
            System.exit(1);
        }
        if (application==null){
            System.exit(1);
        }

        SessionSettings settings = new SessionSettings(new FileInputStream(fileName));
        MessageStoreFactory storeFactory = new JdbcStoreFactory(settings);
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
