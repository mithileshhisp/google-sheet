package org.hispindia.googlesheet;


import java.io.IOException;


public class Starter {
	
	private static final String CREDENTIALS_FILE_PATH = "DHIS2CHNDPHC21-461d051b611f.p12";

	//private static SpreadsheetService spreadsheetService = null;

	private static String APPLICATION_NAME = "DHIS2 CHND PHC 21" ;

	private static String SERVICE_ACCOUNT = "ward-21@dhis2-chnd-phc-21.iam.gserviceaccount.com";

	//private String CREDENTIALS_FILE_PATH;

	private static String SPREAD_SHEET_ID = "1-qsHZjYJWxswKKbsTuiGTImYZSXR7eYzw_7XgzSkBvE";

    public static void main(String[] args) {

        GoogleSheetConfig googleSheetConfig = GoogleSheetConfig.builder()
                .SPREAD_SHEET_ID(SPREAD_SHEET_ID)
                .APPLICATION_NAME(APPLICATION_NAME)
                //.CREDENTIALS_FILE_PATH("/Users/iamsrivastava/Project/OnionDev/hispindia-googlesheet/src/main/resources/file.txt")
                .CREDENTIALS_FILE_PATH(CREDENTIALS_FILE_PATH)
                .SERVICE_ACCOUNT(SERVICE_ACCOUNT)
                .MYSQL_HOST("jdbc:mysql://localhost/")
                .MYSQL_DB("mvapp")
                .MYSQL_USER("root")
                .MYSQL_PASSWORD("ashish")
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
