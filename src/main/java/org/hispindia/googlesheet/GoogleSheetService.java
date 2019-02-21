package org.hispindia.googlesheet;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GoogleSheetService
{

    private GoogleSheetConfig googleSheetConfig;

    public GoogleSheetService( GoogleSheetConfig googleSheetConfig )
    {
        this.googleSheetConfig = googleSheetConfig;

    }

    public void read() throws Exception {
        
        Connection con = DriverManager.getConnection(googleSheetConfig.getMYSQL_HOST() + googleSheetConfig.getMYSQL_DB(), googleSheetConfig.getMYSQL_USER(), googleSheetConfig.getMYSQL_PASSWORD());
        Sheets service = googleSheetConfig.getService();
        if (service != null) {
            Spreadsheet spreadsheetResponse = service.spreadsheets().get(googleSheetConfig.getSPREAD_SHEET_ID()).setIncludeGridData(false)
                    .execute();

            for (Sheet s : spreadsheetResponse.getSheets()) {
                String sheet = s.getProperties().getTitle();
                String range = sheet + "!1:1";
                ValueRange response = service.spreadsheets().values()
                        .get(googleSheetConfig.getSPREAD_SHEET_ID(), range)
                        .execute();

                List<List<Object>> values = response.getValues();
                AtomicReference<String> column = new AtomicReference<>();
                
                
                values.forEach(a -> {
                    commitInDB("drop table IF EXISTS " + sheet.toLowerCase() + ";", con);
                    commitInDB(GoogleSheetUtil.getCreateStatement(a, sheet), con);
                    column.set(StringUtils.join(a, ','));
                });
                
                
                String data_range = sheet + "!A2:Z100000";
                ValueRange data_sheet_range = service.spreadsheets().values()
                        .get(googleSheetConfig.getSPREAD_SHEET_ID(), data_range)
                        .execute();

                List<List<Object>> data_values = data_sheet_range.getValues();
                AtomicReference<String> insert_value = new AtomicReference<>();
                
               
                data_values.forEach(data -> {
                    insert_value.set(StringUtils.join(data, "','"));
                    String in = GoogleSheetUtil.createInsertStatement(column.get(), insert_value.get(), sheet.toLowerCase());
                    commitInDB(in, con);
                });
                
                
                System.gc();
            }
        }

        con.close();

    }

    public void commitInDB( String statement, Connection connection )
    {
        try
        {
            // create Statement object
            Statement stmt = connection.createStatement();
            // send sql command
            stmt.executeUpdate( statement ); // Create Table JDBC

            // close the database connection
            stmt.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.out.println( e.getMessage() );
        }

    }

    public void clear() throws IOException {
        Sheets service = googleSheetConfig.getService();
        if (service != null) {
            Spreadsheet spreadsheetResponse = service.spreadsheets().get(googleSheetConfig.getSPREAD_SHEET_ID()).setIncludeGridData(false)
                    .execute();

            for (Sheet s : spreadsheetResponse.getSheets()) {
                String sheet = s.getProperties().getTitle();
                String range = sheet + "!A2:Z10000000";
                ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
                service.spreadsheets().values().clear(googleSheetConfig.getSPREAD_SHEET_ID(), range, clearValuesRequest).execute();
            }
        }
    }

    public void addData() throws IOException {


        List<List<Object>> fullData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<Object> data = new ArrayList<>();
            data.add("919871813362");
            data.add(i);
            fullData.add(data);
        }

        ValueRange valueRange = new ValueRange();
        valueRange.setValues(fullData);

        Sheets service = googleSheetConfig.getService();
        if (service != null) {
            service.spreadsheets().values().update(googleSheetConfig.getSPREAD_SHEET_ID(), "Sheet1!A2:L10000000", valueRange)
                    .setValueInputOption("RAW").execute();
        }
    }
}
