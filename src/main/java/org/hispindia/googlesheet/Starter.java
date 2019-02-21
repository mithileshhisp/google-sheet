package org.hispindia.googlesheet;


import java.io.IOException;


public class Starter {

    public static void main(String[] args) {

        GoogleSheetConfig googleSheetConfig = GoogleSheetConfig.builder()
                .SPREAD_SHEET_ID("1roKZCQknxSJcZnfT4Pihur1ZGsfCKRCTmiZ5LLV5IV0")
                .APPLICATION_NAME("google sheet api")
                .CREDENTIALS_FILE_PATH("/src/main/resources/file.txt")
                .SERVICE_ACCOUNT("oniondev-gsheet@rosy-acolyte-225207.iam.gserviceaccount.com")
                .MYSQL_HOST("jdbc:mysql://localhost/")
                .MYSQL_DB("mvapp")
                .MYSQL_USER("root")
                .MYSQL_PASSWORD("root")
                .build();

        try {

            GoogleSheetService googleSheetService = new GoogleSheetService(googleSheetConfig);

//            gSheetService.clear();
            googleSheetService.addData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
