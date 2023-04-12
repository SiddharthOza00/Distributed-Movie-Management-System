package com.Request;

import javax.persistence.criteria.CriteriaBuilder.In;

public class Config {
    public static final Integer SEQUENCE_PORT = 2233;
    
    public static final String FRONTEND_IP = "192.168.48.35";

    public static final Integer FRONTEND_PORT = 44553;

    public static final String SEQUENCER_IP = "192.168.48.53";

    public static final String RM1_IP = "192.168.48.35";
    
    public static final String RM2_IP = "192.168.48.53";

    public static final String RM3_IP = "192.168.48.254";

    public static final String RM4_IP = "192.168.48.36";

    public static final Integer RM1_PORT_SQ = 9955;

    public static final Integer RM2_PORT_SQ = 9956;

    public static final Integer RM3_PORT_SQ = 9957;

    public static final Integer RM4_PORT_SQ = 9958;

    public static final Integer RM1_PORT_FE = 11111;
    
    public static final Integer RM2_PORT_FE = 11112;

    public static final Integer RM3_PORT_FE = 11113;

    public static final Integer RM4_PORT_FE = 11114;

}
