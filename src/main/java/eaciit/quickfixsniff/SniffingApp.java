package eaciit.quickfixsniff;
import quickfix.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.logging.*;
import java.lang.invoke.MethodHandles;
import java.util.stream.IntStream;
import java.util.Map;

public class SniffingApp implements quickfix.Application {
    //private static Logger logger;// = Logger.getLogger(SniffingApp.class.getName());
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
    private Map<String,String> config;
    public SniffingApp(){
        //logger = Logger.getLogger(SniffingApp.class.getName());
        //logger.setLevel(Level.INFO);
    }
    public SniffingApp(Map<String,String> config){
        //logger = Logger.getLogger(SniffingApp.class.getName());
        //logger.setLevel(Level.INFO);
        this.config = config;
    }
    public void onCreate(SessionID sessionId){

    }
    public void onLogon(SessionID sessionId){

    }
    public void onLogout(SessionID sessionId){

    }
    public void toAdmin(Message message, SessionID sessionId){
        try{
            if(message.getHeader().getChar(35)=='A'){
                message.setString(96,this.config.get("RawData"));
            }
        }catch(FieldNotFound ex){
            logger.error("File Not Found"+ex.toString(), ex);    
        }
        logger.info("TO AdminXXX",message.toString(),"LLSLSLSL");
    }
    public void toApp(Message message, SessionID sessionId) throws DoNotSend{
        logger.info("TO App",message.toString());
    }
    public void fromAdmin(Message message, SessionID sessionId) 
    throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon{
        logger.info("FROM Admin",message.toString());
    }
    public void fromApp(Message message, SessionID sessionId)
    throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType{
        logger.info("FROM App",message.toString());
    }
}