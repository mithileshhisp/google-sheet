package org.hisp.dhis.schedulecustomesms;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.security.GeneralSecurityException;


/**
 * @author Mithilesh Kumar Thakur
 */


public class SheetsExample
{
    public static void main( String args[] )
        throws IOException, GeneralSecurityException
    {
        // The ID of the spreadsheet to update.
        String spreadsheetId = "1nUCNUPHrxltE2yiyoNifbuYJJzNKgbhRBPnQMCc3ptw"; // TODO: Update placeholder
                                                    // value.
        //https://docs.google.com/spreadsheets/d/1nUCNUPHrxltE2yiyoNifbuYJJzNKgbhRBPnQMCc3ptw/edit?usp=sharing
        // The A1 notation of the values to update.
        String range = "my-range"; // TODO: Update placeholder value.

        // How the input data should be interpreted.
        String valueInputOption = ""; // TODO: Update placeholder value.

        // TODO: Assign values to desired fields of `requestBody`. All existing
        // fields will be replaced:
        ValueRange requestBody = new ValueRange();

        Sheets sheetsService = createSheetsService();
        Sheets.Spreadsheets.Values.Update request = sheetsService.spreadsheets().values()
            .update( spreadsheetId, range, requestBody );
        request.setValueInputOption( valueInputOption );

        UpdateValuesResponse response = request.execute();

        // TODO: Change code below to process the `response` object:
        System.out.println( response );
    }

    public static Sheets createSheetsService()
        throws IOException, GeneralSecurityException
    {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        // TODO: Change placeholder below to generate authentication
        // credentials. See
        // https://developers.google.com/sheets/quickstart/java#step_3_set_up_the_sample
        //
        // Authorize using one of the following scopes:
        // "https://www.googleapis.com/auth/drive"
        // "https://www.googleapis.com/auth/drive.file"
        // "https://www.googleapis.com/auth/spreadsheets"
        GoogleCredential credential = null;

        return new Sheets.Builder( httpTransport, jsonFactory, credential ).setApplicationName(
            "Google-SheetsSample/0.1" ).build();
    }
}
