package org.bwebserver.content.impl;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Very simple template service, which loads HTML templates from a specific location
 * and provides them to callers
 */
public class TemplateService {

    private static TemplateService templateService;

    TemplateService(){

    }

    public static TemplateService getInstrance(){
        if (templateService == null){
            templateService = new TemplateService();
        }
        return templateService;
    }

    public String getTemplate(String templateName) throws IOException {
        Path currentRelativePath = Paths.get("");
        File template = new File(String.format("%s%stemplates%s%s.html", currentRelativePath.toAbsolutePath(), File.separator, File.separator, templateName));
        if (!template.exists()){
            throw new FileNotFoundException("Could not load template. Please make sure the templates folder are copied inside current running path!");
        }
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(template));
        byte[] buf = new byte[(int) template.length()];
        br.read(buf);
        br.close();
        return new String(buf);
    }
}
