package com.Replica1;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerInfo {
	public Logger logger;
    FileHandler handler;
    public LoggerInfo(String fileName, String name) throws SecurityException, IOException{
        try{
            File file = new File(fileName);
            if(!file.exists()){
                file.createNewFile();
            }
            handler = new FileHandler(fileName,true);
//            logger = Logger.getLogger("abc");
            logger = Logger.getLogger(name);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
	
}