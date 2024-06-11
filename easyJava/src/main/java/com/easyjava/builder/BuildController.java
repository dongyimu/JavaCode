package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

public class BuildController {
    private static final Logger logger = LoggerFactory.getLogger(BuildService.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_CONTROLLER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String className = tableInfo.getBeanName() + "Controller";
        File poFile = new File(folder, className + ".java");
        String serviceName = tableInfo.getBeanName() + "Service";
        String serviceBeanName = StringUtils.lowerFirstLetter(serviceName);

        OutputStream os = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            os = new FileOutputStream(poFile);
            osw = new OutputStreamWriter(os, "UTF-8");
            bw = new BufferedWriter(osw);
            bw.write("package " + Constants.PACKAGE_CONTROLLER + ";");
            bw.newLine();
            bw.newLine();

            bw.write("import java.util.List;");
            bw.newLine();
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_PO + "." + tableInfo.getBeanName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_QUERY + "." + tableInfo.getBeanParamName() + ";");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_VO + ".ResponseVO;");
            bw.newLine();
            bw.write("import " + Constants.PACKAGE_SERVICE + "." + serviceName + ";");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RequestBody;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RestController;");
            bw.newLine();
            bw.write("import org.springframework.web.bind.annotation.RequestMapping;");
            bw.newLine();
            bw.write("import javax.annotation.Resource;");
            bw.newLine();
            bw.newLine();

            BuildComment.createClassComment(bw, tableInfo.getComment() + "Controller");
            bw.write("@RestController");
            bw.newLine();
            bw.write("@RequestMapping(\"" + StringUtils.lowerFirstLetter(tableInfo.getBeanName()) + "\")");
            bw.newLine();
            bw.write("public class " + className + " extends ABaseController {");
            bw.newLine();
            bw.newLine();

            bw.write("\t@Resource");
            bw.newLine();

            bw.write("\tprivate " + serviceName + " " + serviceBeanName + ";");
            bw.newLine();
            bw.newLine();

            bw.write("\t@RequestMapping(\"loadDateList\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO loadDateList (" + tableInfo.getBeanParamName() + " query) {");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(getSuccessResponseVO(" + serviceBeanName + ".findListByPage(query)));");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();


            BuildComment.createFieldComment(bw, "新增");
            bw.write("\t@RequestMapping(\"add\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO add(" + tableInfo.getBeanName() + " bean) {");
            bw.newLine();
            bw.write("\t\tthis." + serviceBeanName + ".add(bean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "批量新增");
            bw.write("\t@RequestMapping(\"addBatch\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO addBatch(@RequestBody List<" + tableInfo.getBeanName() + "> listBean) {");
            bw.newLine();
            bw.write("\t\tthis." + serviceBeanName + ".addBatch(listBean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            BuildComment.createFieldComment(bw, "批量新增或修改");
            bw.write("\t@RequestMapping(\"addOrUpdateBatch\")");
            bw.newLine();
            bw.write("\tpublic ResponseVO addOrUpdateBatch(@RequestBody List<" + tableInfo.getBeanName() + "> listBean) {");
            bw.newLine();
            bw.write("\t\tthis." + serviceBeanName + ".addOrUpdateBatch(listBean);");
            bw.newLine();
            bw.write("\t\treturn getSuccessResponseVO(null);");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();

            for (Map.Entry<String, List<FieldInfo>> entry : tableInfo.getKeyIndexMap().entrySet()) {
                List<FieldInfo> keyFieldIndoList = entry.getValue();
                Integer index = 0;
                StringBuilder methodName = new StringBuilder();
                StringBuilder methodParams = new StringBuilder();
                StringBuilder paramsBuilder = new StringBuilder();
                for (FieldInfo fieldInfo : keyFieldIndoList) {
                    index++;
                    methodName.append(StringUtils.upperFirstLetter(fieldInfo.getPropertyName()));
                    if (index < keyFieldIndoList.size()) {
                        methodName.append("And");
                    }
                    methodParams.append(fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName());
                    paramsBuilder.append(fieldInfo.getPropertyName());
                    if (index < keyFieldIndoList.size()) {
                        methodParams.append(", ");
                        paramsBuilder.append(", ");
                    }
                }


                //查询
                BuildComment.createFieldComment(bw, "根据" + methodName + "查询");
                String methodNameResult = "get" + tableInfo.getBeanName() + "By" + methodName;
                bw.write("\t@RequestMapping(\"" + methodNameResult + "\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO " + methodNameResult + "(" + methodParams + ") {");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(this." + serviceBeanName + ".get" + tableInfo.getBeanName() + "By" + methodName + "(" + paramsBuilder + "));");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
                //更新
                BuildComment.createFieldComment(bw, "根据" + methodName + "更新");
                methodNameResult = "update" + tableInfo.getBeanName() + "By" + methodName;
                bw.write("\t@RequestMapping(\"" + methodNameResult + "\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO " + methodNameResult + "(" + tableInfo.getBeanName() + " bean, " + methodParams + ") {");
                bw.newLine();
                bw.write("\t\tthis." + serviceBeanName + ".update" + tableInfo.getBeanName() + "By" + methodName + "(bean, " + paramsBuilder + ");");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(null);");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
                //删除
                BuildComment.createFieldComment(bw, "根据" + methodName + "删除");
                methodNameResult = "delete" + tableInfo.getBeanName() + "By" + methodName;
                bw.write("\t@RequestMapping(\"" + methodNameResult + "\")");
                bw.newLine();
                bw.write("\tpublic ResponseVO " + methodNameResult + "(" + methodParams + ") {");
                bw.newLine();
                bw.write("\t\tthis." + serviceBeanName + ".delete" + tableInfo.getBeanName() + "By" + methodName + "(" + paramsBuilder + ");");
                bw.newLine();
                bw.write("\t\treturn getSuccessResponseVO(null);");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
                bw.newLine();
            }


            bw.write("}");

            bw.flush();
        } catch (Exception e) {
            logger.error("创建Service impl失败", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
