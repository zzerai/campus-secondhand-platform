package com.ruoyi;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.InputStream;
import java.util.Properties;

/**
 * 启动程序
 *
 * 2026.5.9
 * @author ruoyi
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RuoYiApplication
{
    public static void main(String[] args)
    {
        // 加载.env文件到系统属性，使Spring的${...}占位符可以解析
        loadEnv();

        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(RuoYiApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  若依启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }

    /**
     * 加载.env文件到系统属性
     */
    private static void loadEnv()
    {
        // 1. 优先用dotenv-java从文件系统加载（生产环境 .env 与 JAR 同目录）
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .filename(".env")
                .ignoreIfMissing()
                .load();
        if (!dotenv.entries().isEmpty())
        {
            dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
            return;
        }

        // 2. 回退：从classpath加载（开发环境 .env 在 resources 目录下）
        try (InputStream is = RuoYiApplication.class.getClassLoader().getResourceAsStream(".env"))
        {
            if (is != null)
            {
                Properties props = new Properties();
                props.load(is);
                props.forEach((k, v) -> System.setProperty((String) k, (String) v));
            }
        }
        catch (Exception ignored)
        {
            // .env 文件不可用，应用将使用默认值
        }
    }
}
