package eaciit.quickfixsniff;

import quickfix.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.logging.*;
import java.lang.invoke.MethodHandles;
import java.net.UnknownHostException;
import java.util.stream.IntStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;
import com.mongodb.client.MongoDatabase;
import com.mongodb.DBObject;
import com.sun.mail.smtp.SMTPTransport;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Arrays;
import java.util.Date;
import java.util.Queue;
import java.util.LinkedList;
import java.time.Instant;
import java.time.Duration;
import java.util.Properties;

public class SniffingApp implements quickfix.Application {
    //private static Logger logger;// = Logger.getLogger(SniffingApp.class.getName());
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
    private Map<String,String> config;
    private MongoClient client;
    private Queue<Instant> LoginQueue;
    public SniffingApp(){
        //logger = Logger.getLogger(SniffingApp.class.getName());
        //logger.setLevel(Level.INFO);
    }
    private static BasicDBObject MessageToBasicDBObject(quickfix.Message message){
        BasicDBObject result = new BasicDBObject();
        result.append("originalmessage",message.toRawString());
        Iterator<Field<?>> headerIterator = message.getHeader().iterator();
        while(headerIterator.hasNext()){
            Field<?> curField = headerIterator.next();
            result.append(Integer.toString(curField.getTag()), curField.getObject());
        }
        Iterator<Field<?>> messageIterator = message.iterator();
        while(messageIterator.hasNext()){
            Field<?> curField = messageIterator.next();
            result.append(Integer.toString(curField.getTag()), curField.getObject());
        }
        Iterator<Field<?>> trailerIterator = message.getTrailer().iterator();
        while(trailerIterator.hasNext()){
            Field<?> curField = trailerIterator.next();
            result.append(Integer.toString(curField.getTag()), curField.getObject());
        }
        return result; 
    }
    public SniffingApp(Map<String,String> config)throws UnknownHostException{
        this.LoginQueue = new LinkedList<Instant>();
        //logger = Logger.getLogger(SniffingApp.class.getName());
        //logger.setLevel(Level.INFO);
        this.config = config;
        if(config.containsKey("MongoUsername")&&config.containsKey("MongoPassword")){
            MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(config.get("MongoUsername"), "admin",
            config.get("MongoPassword").toCharArray());
            //String connectUrl = "mongodb://"+config.get("MongoUsername")+":"+config.get("MongoPassword")+"@"+config.get("MongoHost");
            //this.client = new MongoClient(new MongoClientURI(connectUrl));
            //System.out.println(">>>>"+connectUrl);
            this.client = new MongoClient(new ServerAddress(config.get("MongoHost")),Arrays.asList(mongoCredential));
        }else{
            this.client = new MongoClient(new MongoClientURI(config.get("MongoStoreConnection")));
            System.out.println("<<<<<"+config.get("MongoStoreConnection"));
        }
        
    }
    private void SendEmail(String message){
        Properties prop = System.getProperties();
        DB db = this.client.getDB(this.config.get("MongoDb"));
        DBCollection emailSetting = db.getCollection("EmailSetting");
        DBObject emailSetup = emailSetting.findOne(new BasicDBObject().append("_id", 1));
        prop.put("mail.smtp.host", (String) emailSetup.get("smtpaddress")); //optional, defined in SMTPTransport
        // prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.port", (int)emailSetup.get("port")); // default port 25

        Session session = Session.getInstance(prop, null);
        Message msg = new MimeMessage(session);
        try {		
			// from
            msg.setFrom(new InternetAddress((String) emailSetup.get("senderemail")));
			// to 
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("ops@clearisk.io", false));
			// subject
            msg.setSubject("QuickFix Alert");
			// content 
            msg.setText(message);
            msg.setSentDate(new Date());
			// Get SMTPTransport
            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
			// connect
            t.connect((String) emailSetup.get("smtpaddress"), (String) emailSetup.get("username"), (String) emailSetup.get("password"));			
			// send
            t.sendMessage(msg, msg.getAllRecipients());

            System.out.println("Response: " + t.getLastServerResponse());

            //t.close();

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public void onCreate(SessionID sessionId){

    }
    public void onLogon(SessionID sessionId){
        
    }
    public void onLogout(SessionID sessionId){
        
    }
    public void toAdmin(quickfix.Message message, SessionID sessionId){
        try{
            if(message.getHeader().getChar(35)=='A'){
                message.setString(96,this.config.get("RawData"));
                /*logger.info("LOGGING ON");
                logger.info(Integer.toString(this.LoginQueue.size()));
                if(this.LoginQueue.size()>=5){
                    Instant lastItem = this.LoginQueue.remove();
                    Instant now = Instant.now();
                    Duration diff = Duration.between(lastItem, now);
                    if (diff.toMinutes()<=5){
                        logger.info("Something gone wrong");
                        System.out.println("Something Gone Wrong");
                        //email 
                        this.SendEmail("Too Many Connectino Failure");
                        System.exit(1);
                    }
                }
                this.LoginQueue.add(Instant.now());*/
            }
        }catch(FieldNotFound ex){
            logger.error("File Not Found"+ex.toString(), ex);    
        }
        logger.info("TO AdminXXX",message.toString(),"LLSLSLSL");
    }
    public void toApp(quickfix.Message message, SessionID sessionId) throws DoNotSend{
        logger.info("TO App",message.toString());
    }
    public void fromAdmin(quickfix.Message message, SessionID sessionId) 
    throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon{
        logger.info("FROM Admin",message.toString());
    }
    public void fromApp(quickfix.Message message, SessionID sessionId)
    throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType{
        if(message.getHeader().getInt(35)==8){
            logger.info("GotExecutionReport");
            logger.info(this.config.get("MongoDb"));
            DBObject map = MessageToBasicDBObject(message);
            DB database = this.client.getDB(this.config.get("MongoDb"));
            try{
                WriteResult res = database.getCollection("ExecutionReport").insert(map);
                int p = res.getN();
                if (p!=1){
                    logger.info(res.toString());
                }
            }catch(MongoException mex){
                logger.info(mex.getMessage());
            }
            
        }
        
        logger.info("FROM App",message.toString());
    }
}