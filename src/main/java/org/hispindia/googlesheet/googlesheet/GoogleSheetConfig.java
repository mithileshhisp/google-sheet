package org.hisp.dhis.googlesheet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GoogleSheetConfig
{

    private String APPLICATION_NAME;

    private String SERVICE_ACCOUNT;

    private String CREDENTIALS_FILE_PATH;

    private String SPREAD_SHEET_ID;

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList( SheetsScopes.SPREADSHEETS );

    private static HttpTransport httpTransport;

    public GoogleCredential getGoogleCredential()
    {
        System.out.println( "In Side config " + CREDENTIALS_FILE_PATH );

        // ServletContext context = null;
        // String absoluteDiskPath = context.getRealPath(CREDENTIALS_FILE_PATH);

        InputStream in = null;
        File f = null;
        f = new File( CREDENTIALS_FILE_PATH );
        /*
         * try { //in = new FileInputStream( CREDENTIALS_FILE_PATH );
         * 
         * //in = new FileInputStream( absoluteDiskPath );
         * 
         * } catch ( FileNotFoundException e ) { e.printStackTrace(); }
         */
        try
        {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        }
        catch ( GeneralSecurityException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        System.out.println( "in " + f );

        // Build a service account credential.
        GoogleCredential credential = null;
        try
        {
            credential = new GoogleCredential.Builder().setTransport( httpTransport ).setJsonFactory( JSON_FACTORY )
                .setServiceAccountId( SERVICE_ACCOUNT ).setServiceAccountScopes( SCOPES )
                .setServiceAccountPrivateKeyFromP12File( f ).build();
        }
        catch ( GeneralSecurityException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        System.out.println( "credential " + credential.toString() );
        return credential;

    }

    public String getAPPLICATION_NAME()
    {
        return APPLICATION_NAME;
    }

    public String getSPREAD_SHEET_ID()
    {
        return SPREAD_SHEET_ID;
    }

    public JsonFactory getJsonFactory()
    {
        return JSON_FACTORY;
    }

    public Sheets getService()
    {

        NetHttpTransport HTTP_TRANSPORT = null;
        try
        {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }
        catch ( GeneralSecurityException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        // HttpTransport httpTransport =
        // GoogleNetHttpTransport.newTrustedTransport();
        // JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        System.out.println( "inside sheet-service " + HTTP_TRANSPORT );

        Sheets service = new Sheets.Builder( HTTP_TRANSPORT, this.getJsonFactory(), getGoogleCredential() )
            .setApplicationName( this.getAPPLICATION_NAME() ).build();

        /*
         * return new Sheets.Builder( httpTransport, jsonFactory,
         * getGoogleCredential() ).setApplicationName(
         * this.getAPPLICATION_NAME() ).build();
         */

        return service;
    }

    public void clear()
        throws IOException
    {
        Sheets service = getService();
        if ( service != null )
        {
            Spreadsheet spreadsheetResponse = service.spreadsheets().get( this.getSPREAD_SHEET_ID() )
                .setIncludeGridData( false ).execute();

            for ( Sheet s : spreadsheetResponse.getSheets() )
            {
                String sheet = s.getProperties().getTitle();
                String range = sheet + "!A2:Z10000000";
                ClearValuesRequest clearValuesRequest = new ClearValuesRequest();
                service.spreadsheets().values().clear( this.getSPREAD_SHEET_ID(), range, clearValuesRequest ).execute();
            }
        }
    }

    public String getSERVICE_ACCOUNT()
    {
        return SERVICE_ACCOUNT;
    }

    public void setSERVICE_ACCOUNT( String sERVICE_ACCOUNT )
    {
        SERVICE_ACCOUNT = sERVICE_ACCOUNT;
    }

    public String getCREDENTIALS_FILE_PATH()
    {
        return CREDENTIALS_FILE_PATH;
    }

    public void setCREDENTIALS_FILE_PATH( String cREDENTIALS_FILE_PATH )
    {
        CREDENTIALS_FILE_PATH = cREDENTIALS_FILE_PATH;
    }

    public void setAPPLICATION_NAME( String aPPLICATION_NAME )
    {
        APPLICATION_NAME = aPPLICATION_NAME;
    }

    public void setSPREAD_SHEET_ID( String sPREAD_SHEET_ID )
    {
        SPREAD_SHEET_ID = sPREAD_SHEET_ID;
    }

}
