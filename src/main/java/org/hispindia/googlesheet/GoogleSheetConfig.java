package org.hispindia.googlesheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import lombok.Builder;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;


@Builder
public class GoogleSheetConfig
{

    private String APPLICATION_NAME;

    private String SERVICE_ACCOUNT;

    private String CREDENTIALS_FILE_PATH;

    private String SPREAD_SHEET_ID;

    private String MYSQL_HOST;

    private String MYSQL_USER;

    private String MYSQL_PASSWORD;

    private String MYSQL_DB;

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList( SheetsScopes.SPREADSHEETS );

    private static HttpTransport httpTransport;

    public GoogleCredential getGoogleCredential()
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream( CREDENTIALS_FILE_PATH );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }

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

        // Build a service account credential.
        GoogleCredential credential = null;
        try
        {
            credential = new GoogleCredential.Builder().setTransport( httpTransport ).setJsonFactory( JSON_FACTORY )
                .setServiceAccountId( SERVICE_ACCOUNT ).setServiceAccountScopes( SCOPES )
                .setServiceAccountPrivateKeyFromP12File( in ).build();
        }
        catch ( GeneralSecurityException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

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

        Sheets service = new Sheets.Builder( HTTP_TRANSPORT, this.getJsonFactory(), getGoogleCredential() )
            .setApplicationName( this.getAPPLICATION_NAME() ).build();

        return service;
    }

    public String getMYSQL_HOST()
    {
        return MYSQL_HOST;
    }

    public String getMYSQL_USER()
    {
        return MYSQL_USER;
    }

    public String getMYSQL_PASSWORD()
    {
        return MYSQL_PASSWORD;
    }

    public String getMYSQL_DB()
    {
        return MYSQL_DB;
    }
}
