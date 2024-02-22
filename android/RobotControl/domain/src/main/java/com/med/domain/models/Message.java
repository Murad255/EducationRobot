package com.med.domain.models;

import java.util.Random;

public class Message {

    public  String topik;
    public  String message;

    public String name;
    public String moduleType;
    public String dataType;
    public  String bdType;
    public long bdIndex;
    public String UserLogin;
    public  String UserPwd;
    public String LoginStatus;
    public String Request;

    public Long UID;
    static Long sUID = Long.valueOf(1);
    public int status;
    public String data;

    public int OrderID;
    public int ChallengeNumbers;
    public String ChallengeInfo;
    //public String TypeGateway;
    public String Number;
    /*
     * <Module><BDType>SlotItem</BDType><BDIndex>1</BDIndex></Module>
     * */

    public Message(String message,String token)  {
        Begin( message, token);
    }

    public Message(String message)  {
        Begin( message,"");
    }

    public Message(String name, String moduleType,String data)
    {
        Beпin( name,  moduleType, data );
    }

    void Begin(String message, String token)  {
        this.message = message;
        this.topik = token;
        if(isModule()){
            name = ScanText("Name");
            moduleType = ScanText("moduleType");
            dataType = ScanText("DataType");
            UID = ScanLong("UID");
            sUID=UID;
            data = ScanText("Data");
            status=ScanInt("Status");
            bdType = ScanText("BDType");
            bdIndex = ScanLong("BDIndex");
            UserLogin = ScanText("UserLogin");
            UserPwd = ScanText("UserPwd");
            LoginStatus = ScanText("LoginStatus");
            Request= ScanText("Request");

            OrderID = ScanInt("OrderID");
             ChallengeNumbers = ScanInt("ChallengeNumbers");
             ChallengeInfo = ScanText("ChallengeInfo");
           // TypeGateway = ScanText("TypeGateway");
            Number = ScanText("Number");
        }
    }

    void Beпin(String name, String moduleType,String data )  {
        this.name = name;
        this.moduleType = moduleType;
        this.data = data;
        this.UID = Message.sUID+1;
        Message.sUID++;
    }

    public String ScanText(String param){
        try {
            return findText(message,param);
        }
        catch (Exception ex ){
            return null;
        }
    }

    public int ScanInt(String param){
        try {
            if (param!=null)
                if(message.length()>0)
                    return  Integer.parseInt(findText(message,param));
                else return -1;
            else return -1;
        }
        catch (Exception ex ){
            return -1;
        }
    }

    long ScanLong(String param){
        try {
            if (param!=null)
                if(message.length()>0)  return  Long.parseLong(findText(message,param));
                else return -1L;
            else return -1L;
        }
        catch (Exception ex ){
            return -1L;
        }
    }


    public  boolean isModule(){
        try {
            if (message.length() > 0) {
                int find1 = message.indexOf("<Module>");
                int find2 = message.indexOf("</Module>");
                if(find2-find1>0) return  true;
                else  return false;
            }
            else return  false;
        }
        catch (Exception ex){
            return  false;
        }
    }

    public  boolean isLoginStatusReport(){
        try {
            if (this.isModule()) {

                if (this.LoginStatus.length()>1) return  true;
                else  return false;
            }
            else return  false;
        }
        catch (Exception ex){
            return  false;
        }
    }

    public String getDeviceName(){
        String[] subStr = topik.split("/"); // Разделения строки str с помощью метода split()
        return subStr[subStr.length-1];
    }
    public static long getUid(){
        if(sUID<1) sUID = Math.abs( new Random().nextLong());
        return sUID++;
    }

    public static String GenerateToDevice(String Name,String data){
        String msg = "<Module><Name>"+Name+"</Name><UID>" + String.valueOf( sUID) +
                "</UID><ModuleType>all</ModuleType><Data>" + data +
                "</Data></Module>";
        return msg;
    }
    public static String InXML(String tag,String data){
        return "<"+tag+">"+data+"</"+tag+">";
    }

    static String findText(String str, String findContext)throws  Exception
    {
        int find1 = str.indexOf("<" + findContext + ">");
        int find2 = str.indexOf("</" + findContext + ">");
        String findStr = "";
        if (find1>=0 ||find2>0)
            findStr +=  str.substring(find1+ ("<" + findContext + ">").length(),find2);

        return findStr;
    }

    @Override
    public String toString(){
        if (name ==null || name.length()<1) return message;
        String msg = "<Module><Name>"+name+"</Name><ModuleType>"+moduleType.toString()+"</ModuleType><UID>" + UID +
                "</UID><Data>" + data + "</Data></Module>";
        UID++;
        sUID++;
        return  msg;
    }
}
