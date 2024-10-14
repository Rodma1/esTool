package com.chen;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

/**
 * @author:  神的孩子都在歌唱
 * @date: 2023/9/18 14:50
 * @Description:
 */
@SpringBootApplication
public class ChenApplication {
    /**
     * 代码混淆后，包名、类名会存在重复，重写buildDefaultBeanName方法，获取全限定的类名
     */
    static class ProGuardBeanNameGenerator extends AnnotationBeanNameGenerator {
        @Override
        protected String buildDefaultBeanName(BeanDefinition definition) {
            return definition.getBeanClassName();
        }
    }


    public static void main(String[] args)
    {
//        SpringApplication.run(ChenApplication.class, args);
        //SpringApplication.run(DemoApplication.class, args);
        new SpringApplicationBuilder(ChenApplication .class)
                .beanNameGenerator(new ProGuardBeanNameGenerator()).run();
        System.out.println("启动成功");
    }
}
