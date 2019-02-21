package org.hisp.dhis.googlesheet;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.TaskScheduler;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduleGoogleSheetTask  implements Runnable
{
    //public static final Logger logger = LoggerFactory.getLogger(ScheduleCustomeSMSTask.class);

    public static final String KEY_TASK = "scheduleGoogleSheetTask";

    private static final String CREDENTIALS_FILE_PATH = "DHIS2CHNDPHC21-461d051b611f.p12";
    
    //private static SpreadsheetService spreadsheetService = null;
    
    private String APPLICATION_NAME = "DHIS2 CHND PHC 21" ;

    private String SERVICE_ACCOUNT = "ward-21@dhis2-chnd-phc-21.iam.gserviceaccount.com";

    //private String CREDENTIALS_FILE_PATH;

    private String SPREAD_SHEET_ID = "1-qsHZjYJWxswKKbsTuiGTImYZSXR7eYzw_7XgzSkBvE";

    private String MYSQL_HOST;

    private String MYSQL_USER;

    private String MYSQL_PASSWORD;

    private String MYSQL_DB;

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList( SheetsScopes.SPREADSHEETS );

    private static HttpTransport httpTransport;
    
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private TrackedEntityInstanceService trackedEntityInstanceService;

    @Autowired
    private TrackedEntityAttributeValueService trackedEntityAttributeValueService;

    @Autowired
    private TrackedEntityAttributeService trackedEntityAttributeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskScheduler taskScheduler;

    //private GoogleSheetConfig googleSheetConfig;
   
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

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

    
    GoogleSheetConfig googleSheetConfig;
    
    private SimpleDateFormat simpleDateFormat;

    private String complateDate = "";

    private Period currentperiod;

    private String trackedEntityInstanceIds = "";

    String currentDate = "";

    String currentMonth = "";

    String currentYear = "";

    String todayDate = "";

    static String inputTemplatePath = "";
    // -------------------------------------------------------------------------
    // Action
    // -------------------------------------------------------------------------

   
    @Override
    public void run()
    {
        inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + CREDENTIALS_FILE_PATH;
        
        try
        {
            //testDhis2SampleSheet();
            pushTeiDataInGoogleSheet();

        }
        catch ( IOException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            System.out.println( "Error SMS " + e1.getMessage() );
        }
        catch ( GeneralSecurityException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    // -------------------------------------------------------------------------
    // Support methods
    // -------------------------------------------------------------------------
    
    public void pushTeiDataInGoogleSheet()
        throws Exception
    {
        System.out.println( "In Side pushTeiDataInGoogleSheet " );
        
        //String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + CREDENTIALS_FILE_PATH;
        
        googleSheetConfig = new GoogleSheetConfig();
        googleSheetConfig.setSPREAD_SHEET_ID( SPREAD_SHEET_ID );
        googleSheetConfig.setAPPLICATION_NAME(  APPLICATION_NAME );
        googleSheetConfig.setSERVICE_ACCOUNT( SERVICE_ACCOUNT );
        googleSheetConfig.setCREDENTIALS_FILE_PATH( inputTemplatePath);
        
        googleSheetConfig.clear();
        System.out.println( "clear sheet  --  " );
        addDataInSheet();
       
    }
    
  
    public void addDataInSheet()
        throws IOException
    {
        
        TrackedEntityAttribute name = trackedEntityAttributeService.getTrackedEntityAttribute( 608 );
        TrackedEntityAttribute gender = trackedEntityAttributeService.getTrackedEntityAttribute( 611 );
        TrackedEntityAttribute age = trackedEntityAttributeService.getTrackedEntityAttribute( 610 );
        List<String> mctsNumbers = new ArrayList<String>( getTrackedEntityInstanceAttributeValueByAttributeId( 5762 ) );
        System.out.println( "List Size --  " + mctsNumbers.size() );
        
        List<List<Object>> fullData = new ArrayList<>();
        
        for( String mctsNumber : mctsNumbers )
        {
            List<Object> data = new ArrayList<>();
            TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( Integer.parseInt( mctsNumber.split( ":" )[0] ) );
            TrackedEntityAttributeValue teiName = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, name );
            TrackedEntityAttributeValue teiSex = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, gender );
            TrackedEntityAttributeValue teiAge = trackedEntityAttributeValueService.getTrackedEntityAttributeValue( tei, age );
            
            data.add( mctsNumber.split( ":" )[0] );
            data.add( mctsNumber.split( ":" )[1] );
            fullData.add( data );
            /*
            if( teiName != null && teiSex != null && teiAge != null )
            {
                System.out.println( "data" + " : " + mctsNumber.split( ":" )[1] + " : " + teiName + " : " + teiSex + " : " + teiAge);
                
                data.add( mctsNumber.split( ":" )[1] );
                data.add( teiName );
                data.add( teiSex );
                data.add( teiAge );
                fullData.add( data );
            }
            */
            
        }
        
 
        ValueRange valueRange = new ValueRange();
        valueRange.setValues( fullData );
        
        System.out.println( "fullData --  " + fullData.size() );

        Sheets service = googleSheetConfig.getService();
        //Sheets service = getService();
        System.out.println( "service --  " + service.getApplicationName() );
        
        if ( service != null )
        {
            service.spreadsheets().values()
                .update( getSPREAD_SHEET_ID(), "Sheet1!A3:L10000000", valueRange )
                .setValueInputOption( "RAW" ).execute();
        }
    }
    
    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    // --------------------------------------------------------------------------------
    
    public List<String> getTrackedEntityInstanceAttributeValueByAttributeId( Integer attributeId )
    {
        List<String> mctsNumbers = new ArrayList<String>();
        
        try
        {
            String query = "SELECT trackedentityinstanceid, value FROM trackedentityattributevalue " + "WHERE trackedentityattributeid =  "
                + attributeId + " AND trackedentityinstanceid > 90000";

            System.out.println( "query: " + query );
            
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String tei = rs.getString( 1 );
                String mctsNo = rs.getString( 2 );
                
                if ( mctsNo != null )
                {
                    mctsNumbers.add( tei + ":" + mctsNo );
                }
            }

            return mctsNumbers;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }
    

        
    
}
