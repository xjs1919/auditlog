package com.github.xjs.auditlog.aop;

import com.github.xjs.auditlog.log.AuditLog;

import java.util.ArrayList;
import java.util.List;

public class AuditContext {

    private AuditContext(){}

    private static ThreadLocal<List<AuditLog.Diff>> diffList = new ThreadLocal<List<AuditLog.Diff>>(){
        protected List<AuditLog.Diff> initialValue() {
            return new ArrayList<AuditLog.Diff>();
        }
    };

    public static void addDiff(AuditLog.Diff diff){
        diffList.get().add(diff);
    }

    public static List<AuditLog.Diff> getDiffList(){
        return diffList.get();
    }

    public static void clear(){
        diffList.remove();
    }


}
