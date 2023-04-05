package com.Replica1;

import javax.xml.ws.Endpoint;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class OutremontServer {
    public static void main(String[] args) {
        try {
            LoggerInfo userLogger = new LoggerInfo("OutremontServer.txt", "OutremontServer");
            userLogger.logger.setLevel(Level.ALL);
            userLogger.logger.info("----Logger for Outremont Server----");

            WebserviceImpl serverImpl = InitializeImplObject();
            Endpoint endpoint = Endpoint.publish("http://localhost:8082/outremont", serverImpl);
            System.out.println("Outremont server is published: " + endpoint.isPublished());

            // Calling 2 threads
            Runnable thread1 = new ThreadClass(44555, userLogger, serverImpl);
            Runnable thread2 = new ThreadClass(44556, userLogger, serverImpl);

            Executor executor = Executors.newFixedThreadPool(2);
            executor.execute(thread1);
            executor.execute(thread2);
        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
        }
    }

    private static WebserviceImpl InitializeImplObject() {
        WebserviceImpl methImpl = null;
        try {
            methImpl = new WebserviceImpl();
            Map<String, Map<String, List<String>>> serverData = new ConcurrentHashMap<String, Map<String, List<String>>>() {
                {
                    put("Avatar", new HashMap<String, List<String>>() {
                        {
                            put("OUTM260323", new ArrayList<String>() {
                                {
                                    add("3");
                                    add("OUTC3000,OUTC3001");
                                }
                            });
                            put("OUTM250323", new ArrayList<String>() {
                                {
                                    add("5");
                                    add("OUTC3002,OUTC3003,ATWC3000");
                                }
                            });
                            // put("OUTM010223", new ArrayList<String>(){
                            // {
                            // add("5");
                            // add("OUTC3004,OUTC3005");
                            // }
                            // });
                            // put("OUTM030223", new ArrayList<String>(){
                            // {
                            // add("3");
                            // add("OUTC3006,OUTC3007");
                            // }
                            // });
                        }
                    });
                }
            };

            // Adding client data
            Map<String, Map<String, Map<String, Integer>>> clientData = new ConcurrentHashMap<String, Map<String, Map<String, Integer>>>() {
                {
                    put("OUTC3000", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("OUTM260323", 2);
                                }
                            });
                        }
                    });

                    put("OUTC3001", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("OUTM260323", 2);
                                }
                            });
                        }
                    });

                    put("ATWC3000", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("OUTM260323", 1);
                                }
                            });
                        }
                    });
                }
            };

            methImpl.setMovieData(serverData);
            methImpl.setClientData(clientData);
            methImpl.setServerName("Outremont");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return methImpl;
    }
}
