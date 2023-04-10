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

public class VerdunServer {
    public static void main(String[] args) {

        try {
            // Loggers
            LoggerInfo userLogger = new LoggerInfo("VerdunServer.txt", "VerdunServer");
            userLogger.logger.setLevel(Level.ALL);
            userLogger.logger.info("----Logger for Verdun Server----");

            WebserviceImpl serverImpl = InitializeImplObject();
            Endpoint endpoint = Endpoint.publish("http://localhost:8082/verdun", serverImpl);
            System.out.println("Verdun server is published: " + endpoint.isPublished());

            Runnable thread1 = new ThreadClass(8001, userLogger, serverImpl);
            Runnable thread2 = new ThreadClass(44554, userLogger, serverImpl);

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
                            put("VERM240323", new ArrayList<String>() {
                                {
                                    add("1");
                                    add("VERC3000,VERC3001,ATWC3000");
                                }
                            });
                            put("VERM230323", new ArrayList<String>() {
                                {
                                    add("5");
                                    add("VERC3002,VERC3003");
                                }
                            });
                            // put("VERM010223", new ArrayList<String>(){
                            // {
                            // add("5");
                            // add("VERC3004,VERC3005");
                            // }
                            // });
                            // put("VERM030223", new ArrayList<String>(){
                            // {
                            // add("3");
                            // add("VERC3006,VERC3007");
                            // }
                            // });
                        }
                    });
                }
            };

            // Adding client data
            Map<String, Map<String, Map<String, Integer>>> clientData = new ConcurrentHashMap<String, Map<String, Map<String, Integer>>>() {
                {
                    put("VERC3000", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("VERM240323", 2);
                                }
                            });
                        }
                    });

                    put("VERC3001", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("VERM240323", 2);
                                }
                            });
                        }
                    });

                    put("ATWC3000", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("VERM240323", 1);
                                }
                            });
                        }
                    });

                    put("VERC3002", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("VERM230323", 1);
                                }
                            });
                        }
                    });

                    put("VERC3003", new HashMap<String, Map<String, Integer>>() {
                        {
                            put("Avatar", new HashMap<String, Integer>() {
                                {
                                    put("VERM230323", 1);
                                }
                            });
                        }
                    });
                }
            };

            methImpl.setMovieData(serverData);
            methImpl.setClientData(clientData);
            methImpl.setServerName("Verdun");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return methImpl;

    }
}
