package com.chen.common.utils.json;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ReadJsonUtils {

    public static String readJsonFile(String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        File jsonFile = new File(filename);
        try (Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8")) {
            int ch;
            while ((ch = reader.read()) != -1) {
                stringBuilder.append((char) ch);
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
