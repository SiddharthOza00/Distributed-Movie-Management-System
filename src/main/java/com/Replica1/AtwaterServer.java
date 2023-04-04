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

public class AtwaterServer {
    public static void main(String[] args) {
        try {

            // Logger code
            LoggerInfo userLogger = new LoggerInfo("AtwaterServer.txt", "AtwaterServer");
            userLogger.logger.setLevel(Level.ALL);
            userLogger.logger.info("----Logger for Atwater Server----");

            WebserviceImpl serverImpl = InitializeImplObject();
            Endpoint endpoint = Endpoint.publish("http://localhost:8080/atwater", serverImpl);
            System.out.println("Atwater server is published: " + endpoint.isPublished());

            // Calling 2 threads
            Runnable thread1 = new ThreadClass(44551, userLogger, serverImpl);
            Runnable thread2 = new ThreadClass(44552, userLogger, serverImpl);

            Executor executor = Executors.newFixedThreadPool(2);
            executor.execute(thread1);
            executor.execute(thread2);

        }

        catch (Exception e) {
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
                            put("ATWM220323", new ArrayList<String>() {
                                {
                                    add("3");
                                    add("ATWC3000,ATWC3001");
                                }
                            });
                            put("ATWM210323", new ArrayList<String>() {
                                {
                                    add("5");
                                    add("ATWC3002,ATWC3000");
                                }
                            });
                            // put("ATWM090223", new ArrayList<String>(){
                            // {
                            // add("5");
                            // add("ATWC3004,ATWC3005");
                            // }
                            // });
                            // put("ATWM100223", new ArrayList<String>(){
                            // {
                            // add("3");
                            // add("ATWC3006,ATWC3007");
                            // }
                            // });
                        }
                    });

                    put("Avengers", new HashMap<String, List<String>>() {
                        {
                            put("ATWM240323", new ArrayList<String>() {
                                {
                                    add("3");
                                    add("ATWC3000");
                                }
                            });
                            put("ATWM230323", new ArrayList<String>() {
                                {
                                    add("3");
                                    add("ATWC3000");
                                }
                            });
                        }
                    });
                }
            };

            // Adding client data
            Map<String, Map<String, Map<String, Integer>>> clientData = new ConcurrentHashMap<String, Map<String, Map<String, Integer>>>() {
                {
                    put("ATWC3000", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("ATWM220323", 2);
                                    put("ATWM210323", 2);
                                }
                            });

                            put("Avengers", new HashMap<String, Integer>() {
                                {
                                    put("ATWM240323", 2);
                                    put("ATWM230323", 3);
                                }
                            });
                        }
                    });

                    put("ATWC3001", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("ATWM220323", 2);
                                }
                            });
                        }
                    });
                    put("ATWC3002", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("ATWM210323", 2);
                                }
                            });
                        }
                    });
                }
            };

            methImpl.setMovieData(serverData);
            methImpl.setClientData(clientData);
            methImpl.setServerName("Atwater");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return methImpl;

    }
}