package com.chen.common.utils.json;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ReadJsonUtils {

    public static String readJsonFile(String filename) {
        String jsonString = null; // 默认为 null，表示文件读取失败
        File jsonFile = new File(filename);
        try (Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8)) {
            StringBuilder stringBuffer = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                stringBuffer.append((char) ch);
            }
            jsonString = stringBuffer.toString();
        } catch (FileNotFoundException e) {
            log.error("File not found: {}", filename, e);
        } catch (IOException e) {
            log.error("Error reading file: {}", filename, e);
        }
        return jsonString; // 如果文件读取失败，则返回 null
    }
}
