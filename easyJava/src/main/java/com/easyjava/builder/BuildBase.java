package com.easyjava.builder;

import com.easyjava.bean.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BuildBase {
    private static Logger logger = LoggerFactory.getLogger(BuildBase.class);

    public static void execute() {
        List<String> headInfoList = new ArrayList();

        //生成date枚举
        headInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headInfoList,"DateTimePatternEnum", Constants.PATH_ENUMS);

        headInfoList.clear();

        headInfoList.add("package " + Constants.PACKAGE_UTILS);
        build(headInfoList,"DateUtils", Constants.PATH_UTILS);

        //生成baseMapper
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_MAPPERS);
        build(headInfoList,"BaseMapper", Constants.PATH_MAPPERS);

        //生成pageSize枚举
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headInfoList,"PageSize", Constants.PATH_ENUMS);

        //生成SimplePage
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_QUERY);
        headInfoList.add("import " + Constants.PACKAGE_ENUMS + ".PageSize");
        build(headInfoList,"SimplePage", Constants.PATH_QUERY);

        //生成baseQuery
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_QUERY);
        build(headInfoList,"BaseQuery", Constants.PATH_QUERY);

        //生成PaginationResultVO
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_VO);
        build(headInfoList,"PaginationResultVO", Constants.PATH_VO);

        //生成Exception
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_EXCEPTION);
        headInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum;");
        build(headInfoList,"BusinessException", Constants.PATH_EXCEPTION);

        //生成ResponseCodeEnum
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_ENUMS);
        build(headInfoList,"ResponseCodeEnum", Constants.PATH_ENUMS);

        //生成BaseController
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_CONTROLLER);
        headInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum;");
        headInfoList.add("import " + Constants.PACKAGE_VO + ".ResponseVO;");
        build(headInfoList,"ABaseController", Constants.PATH_CONTROLLER);

        //生成ResponseVo
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_VO);
        build(headInfoList,"ResponseVO", Constants.PATH_VO);

        //生成AGlobalExceptionHandlerController
        headInfoList.clear();
        headInfoList.add("package " + Constants.PACKAGE_CONTROLLER);
        headInfoList.add("import " + Constants.PACKAGE_ENUMS + ".ResponseCodeEnum");
        headInfoList.add("import " + Constants.PACKAGE_VO + ".ResponseVO");
        headInfoList.add("import " + Constants.PACKAGE_EXCEPTION + ".BusinessException");
        build(headInfoList,"AGlobalExceptionHandlerController", Constants.PATH_CONTROLLER);
    }

    private static void build(List<String> headerInfoList,String fileName, String outPutPath) {
        File folder = new File(outPutPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File javaFile = new File(outPutPath, fileName + ".java");

        OutputStream out = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;

        InputStream in = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            out = new FileOutputStream(javaFile);
            osw = new OutputStreamWriter(out, "UTF-8");
            bw = new BufferedWriter(osw);

            String tempLatePath = BuildBase.class.getClassLoader().getResource("template/" + fileName + ".txt").getPath();

            in = new FileInputStream(tempLatePath);
            isr = new InputStreamReader(in, "UTF-8");
            br = new BufferedReader(isr);

            for(String head : headerInfoList){
                bw.write(head + ";");
                bw.newLine();
                if(head.contains("package ")){
                    bw.newLine();
                }
            }

            String lineInfo = null;
            while ((lineInfo = br.readLine()) != null) {
                bw.write(lineInfo);
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            logger.error("生成基础类失败:{},失败:", fileName, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
