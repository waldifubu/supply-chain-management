package com.example.supplychainmanagement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class InitializeData {

    private final DataSource dataSource;

    private final String firstrun = "firstrun";

    @Value("${app.mysqlfile}")
    private String SQLDataFile;

    public InitializeData(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() throws Exception {
        Path path = Paths.get(firstrun);

        if (Files.isRegularFile(path)) {
            System.out.println("Firstrun file found. Let's run SQL scripts");
            runScripts();
        }
    }

    private void runScripts() {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.setSqlScriptEncoding("UTF-8");
        resourceDatabasePopulator.setContinueOnError(false);
        resourceDatabasePopulator.setIgnoreFailedDrops(false);
        resourceDatabasePopulator.addScript(new ClassPathResource(SQLDataFile));
        try {
            resourceDatabasePopulator.execute(dataSource);
            System.out.println("Delete firstrun file");
            Path path = Paths.get(firstrun);
            Files.delete(path);
        } catch (Exception exception) {
            System.err.println("Source error: " + exception.getCause());
            System.err.println("Message: " + exception.getMessage());
        }
    }
}
