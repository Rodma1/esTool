package com.chen.common.utils.json;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ReadJsonUtils {

    public static String readJsonFile(String filename) {
        String jsonString = "";
        File jsonFile = new File(filename);
        try {
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuilder stringBuffer = new StringBuilder();
            while ((ch = reader.read()) != -1) {
                stringBuffer.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonString = stringBuffer.toString();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}
