package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.JsonUtils;
import com.easyjava.utils.PropertiesUtils;
import com.easyjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildTable {

    private static final Logger logger = LoggerFactory.getLogger(BuildTable.class);
    private static Connection conn = null;

    private static String SQL_SHOW_TABLE_STATUS = "show table status";

    private static String SQL_SHOW_TABLE_FIELDS = "show full fields from %s";

    private static String SQL_SHOW_TABLE_INDEX = "show index from %s";


    static {
        String driverName = PropertiesUtils.getString("db.driver.name");
        String url = PropertiesUtils.getString("db.url");
        String username = PropertiesUtils.getString("db.username");
        String password = PropertiesUtils.getString("db.password");
        try {
            Class.forName(driverName);
            conn = DriverManager.getConnection(url,username,password);
        } catch (Exception e) {
            logger.error("数据库连接失败",e);
        }

    }

    public static List<TableInfo> getTable(){
        PreparedStatement ps = null;
        ResultSet tableResult = null;

        List<TableInfo> tableInfoList = new ArrayList<TableInfo>();
        try{
            ps = conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            tableResult = ps.executeQuery();
            while(tableResult.next()){
                String tableName = tableResult.getString("name");
                String comment = tableResult.getString("comment");
                //logger.info("tableName:{},comment:{}",tableName,comment);

                String beanName = tableName;
                if(Constants.IGNORE_TABLE_PREFIX){
                    beanName = tableName.substring(tableName.indexOf("_")+1);
                }
                beanName = processFiled(beanName,true);

                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(tableName);
                tableInfo.setBeanName(beanName);
                tableInfo.setComment(comment);
                tableInfo.setBeanParamName(beanName + Constants.SUFFIX_BEAN_QUERY);

                readFieldInfo(tableInfo);

                getKeyIndexInfo(tableInfo);
                //logger.info("tableInfo:{}",JsonUtils.toJson(tableInfo));
                tableInfoList.add(tableInfo);
            }
        }catch (Exception e){
            logger.error("读取表失败");
        }finally {
            if(tableResult != null){
                try {
                    tableResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return tableInfoList;
    }


    private static void readFieldInfo(TableInfo tableInfo){
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        List<FieldInfo> fieldInfoList = new ArrayList<FieldInfo>();

        List<FieldInfo> extendFieldList = new ArrayList<FieldInfo>();

        try{
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_FIELDS,tableInfo.getTableName()));
            fieldResult = ps.executeQuery();
            boolean haveDateTime = false;
            boolean haveDate = false;
            boolean haveBigDecimal = false;

            while(fieldResult.next()){
                String field = fieldResult.getString("field");
                String type = fieldResult.getString("type");
                String extra = fieldResult.getString("extra");
                String comment = fieldResult.getString("comment");

                if(type.indexOf("(") > 0){
                    type = type.substring(0,type.indexOf("("));
                }
                String propertyName = processFiled(field,false);


                FieldInfo fieldInfo = new FieldInfo();
                fieldInfoList.add(fieldInfo);

                fieldInfo.setFieldName(field);
                fieldInfo.setSqlType(type);
                fieldInfo.setComment(comment);
                fieldInfo.setAutoIncrement("auto_increment".equalsIgnoreCase(extra));
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setJavaType(processJavaType(type));

                if(ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,type)){
                    haveDateTime = true;
                }
                if(ArrayUtils.contains(Constants.SQL_DATE_TYPES,type)){
                    haveDate = true;
                }
                if(ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE,type)){
                    haveBigDecimal = true;
                }

                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, type)) {
                    FieldInfo fuzzyField = new FieldInfo();
                    fuzzyField.setJavaType(fieldInfo.getJavaType());
                    fuzzyField.setPropertyName(propertyName+Constants.SUFFIX_BEAN_QUERY_FUZZY);
                    fuzzyField.setFieldName(fieldInfo.getFieldName());
                    fuzzyField.setSqlType(type);
                    extendFieldList.add(fuzzyField);
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, type) || ArrayUtils.contains(Constants.SQL_DATE_TYPES, type)) {

                    FieldInfo timeStartField = new FieldInfo();
                    timeStartField.setJavaType("String");
                    timeStartField.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_TIME_START);
                    timeStartField.setFieldName(fieldInfo.getFieldName());
                    timeStartField.setSqlType(type);
                    extendFieldList.add(timeStartField);

                    FieldInfo timeEndField = new FieldInfo();
                    timeEndField.setJavaType("String");
                    timeEndField.setPropertyName(propertyName + Constants.SUFFIX_BEAN_QUERY_TIME_END);
                    timeEndField.setFieldName(fieldInfo.getFieldName());
                    timeEndField.setSqlType(type);
                    extendFieldList.add(timeEndField);
                }
            }
            tableInfo.setHaveBigDecimal(haveBigDecimal);
            tableInfo.setHaveDateTime(haveDateTime);
            tableInfo.setHaveDate(haveDate);
            tableInfo.setFieldList(fieldInfoList);
            tableInfo.setExtendFieldList(extendFieldList);
        }catch (Exception e){
            logger.error("读取表失败");
        }finally {
            if(fieldResult != null){
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<FieldInfo> getKeyIndexInfo(TableInfo tableInfo){
        PreparedStatement ps = null;
        ResultSet fieldResult = null;

        List<FieldInfo> fieldInfoList = new ArrayList<FieldInfo>();
        try{
            Map<String,FieldInfo> tempMap = new HashMap();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()){
                tempMap.put(fieldInfo.getFieldName(),fieldInfo);
            }
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_INDEX,tableInfo.getTableName()));
            fieldResult = ps.executeQuery();
            while(fieldResult.next()){
                String keyName = fieldResult.getString("key_name");
                Integer nonUnique = fieldResult.getInt("non_unique");
                String columnName = fieldResult.getString("column_name");
                if(nonUnique == 1){
                    continue;
                }
                List<FieldInfo> keyFieldList = tableInfo.getKeyIndexMap().get(keyName);
                if(null == keyFieldList){
                    keyFieldList = new ArrayList();
                    tableInfo.getKeyIndexMap().put(keyName, keyFieldList);
                }
                /*for(FieldInfo fieldInfo : tableInfo.getFieldList()){
                    if(fieldInfo.getFieldName().equals(columnName)){
                        keyFieldList.add(fieldInfo);
                    }
                }*/
                keyFieldList.add(tempMap.get(columnName));
            }
        }catch (Exception e){
            logger.error("读取索引失败");
        }finally {
            if(fieldResult != null){
                try {
                    fieldResult.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return fieldInfoList;
    }


    private static String processFiled(String field,Boolean uperCaseFirstLetter){
        StringBuffer sb = new StringBuffer();
        String[] fields = field.split("_");
        sb.append(uperCaseFirstLetter? StringUtils.upperFirstLetter(fields[0]):fields[0]);
        for(int i=1;i<fields.length;i++){
            sb.append(StringUtils.upperFirstLetter(fields[i]));
        }
        return sb.toString();
    }

    private static String processJavaType(String type){
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE,type)){
            return "Integer";
        }else if (ArrayUtils.contains(Constants.SQL_LONG_TYPE,type)){
            return "Long";
        }else if(ArrayUtils.contains(Constants.SQL_STRING_TYPE,type)){
            return "String";
        }else if(ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,type)|| ArrayUtils.contains(Constants.SQL_DATE_TYPES,type)){
            return "Date";
        }else if(ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE,type)){
            return "BigDecimal";
        }else {
            throw new RuntimeException("无法识别的类型:" + type);
        }
    }
}
