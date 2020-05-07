package com.github.xjs.auditlog.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Slf4j
public class LogUtil {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MsgArgs{
        private String msg;
        private Object[] args;
        public static MsgArgs of(String msg, Object... args){
            return new MsgArgs(msg, args);
        }
    }

    public static void debugOnlyMsg(Logger logger, Supplier<String> supplier){
        if(logger.isDebugEnabled()){
            String msg = supplier.get();
            logger.debug(msg);
        }
    }

    public static void debugMsgWithArgs(Logger logger, Supplier<MsgArgs> supplier){
        if(logger.isDebugEnabled()){
            MsgArgs msgArg = supplier.get();
            logger.debug(msgArg.getMsg(), msgArg.getArgs());
        }
    }

    public static void infoOnlyMsg(Logger logger, Supplier<String> supplier){
        if(logger.isInfoEnabled()){
            String msg = supplier.get();
            logger.info(msg);
        }
    }

    public static void infoMsgWithArgs(Logger logger, Supplier<MsgArgs> supplier){
        if(logger.isInfoEnabled()){
            MsgArgs msgArg = supplier.get();
            logger.info(msgArg.getMsg(), msgArg.getArgs());
        }
    }

}
