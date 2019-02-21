package org.hisp.dhis.schedulecustomesms;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.googlesheet.GoogleSheetConfig;
import org.hisp.dhis.googlesheet.GoogleSheetService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Mithilesh Kumar Thakur
 */
public class ScheduleCustomeSMSTask
    implements Runnable
{

    //public static final Logger logger = LoggerFactory.getLogger(ScheduleCustomeSMSTask.class);

    private static final Pattern VALID_A1_PATTERN = Pattern.compile("([A-Z]+)([0-9]+)");

    private String summaryReportName = ""; 
    private String detailReportName = "";
    
    

    private static URL spreadsheetsFeedUrl = null;

    private final static int NAME_ATTRIBUTE_ID = 136;

    private final static int MOBILE_NUMBER_ATTRIBUTE_ID = 142;

    private final static int NPCDCS_FOLLOW_UP_PROGRAM_STAGE_ID = 133470;

    private final static int ANC_FIRST_VISIT_PROGRAM_STAGE_ID = 1339;

    private final static int ANC_VISITS_2_4_PROGRAM_STAGE_ID = 1364;

    private final static int CHILD_HEALTH_IMMUNIZATION_PROGRAM_STAGE_ID = 2125;

    private final static int POST_NATAL_CARE_PROGRAM_STAGE_ID = 1477;

    public static final String KEY_TASK = "scheduleCustomeSMSTask";

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart. If modifying
     * these scopes, delete your previously saved tokens/ folder.
     */
    //private static final List<String> SCOPES = Collections.singletonList( SheetsScopes.SPREADSHEETS );
    
    private static final List<String> SCOPES = Arrays.asList( SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE );
    
    //private static final Set<String> SCOPES = new HashSet<String>( SheetsScopes.all() );
    
    
    //private static final List<String> SCOPES = Arrays.asList(  GmailScopes.MAIL_GOOGLE_COM );
    
    //private static final List<String> SCOPES =  Arrays.asList( GmailScopes.MAIL_GOOGLE_COM );

    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    
    private static final String P12_FILE_PATH = "DHIS2 CHND PHC 21-461d051b611f.p12";
    
    

    private static HttpTransport HTTP_TRANSPORT;
    
    //private static SpreadsheetService spreadsheetService = null;
    
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

    private GoogleSheetConfig googleSheetConfig;
    
    public GoogleSheetConfig getGoogleSheetConfig()
    {
        return googleSheetConfig;
    }

    public void setGoogleSheetConfig( GoogleSheetConfig googleSheetConfig )
    {
        this.googleSheetConfig = googleSheetConfig;
    }
    
    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private SimpleDateFormat simpleDateFormat;

    private String complateDate = "";

    private Period currentperiod;

    private String trackedEntityInstanceIds = "";

    String currentDate = "";

    String currentMonth = "";

    String currentYear = "";

    String todayDate = "";

    // -------------------------------------------------------------------------
    // Action
    // -------------------------------------------------------------------------

    @Override
    public void run()
    {

        /*        
        
   */

        simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        SimpleDateFormat timeFormat = new SimpleDateFormat( "HH:mm:ss" );
        // get current date time with Date()
        Date date = new Date();
        System.out.println( timeFormat.format( date ) );

        todayDate = simpleDateFormat.format( date );
        currentDate = simpleDateFormat.format( date ).split( "-" )[2];
        currentMonth = simpleDateFormat.format( date ).split( "-" )[1];
        currentYear = simpleDateFormat.format( date ).split( "-" )[0];
        String currentHour = timeFormat.format( date ).split( ":" )[0];

        try
        {
            testDhis2SampleSheet();
            //pushTeiDataInGoogleSheet();
            
            
            //sheetsQuickstart();
            // scheduledNPCDCSProgramCustomeSMS( MOBILE_NUMBER_ATTRIBUTE_ID,
            // NPCDCS_FOLLOW_UP_PROGRAM_STAGE_ID );
            // scheduledANCProgrammeCustomeSMS( MOBILE_NUMBER_ATTRIBUTE_ID,
            // ANC_FIRST_VISIT_PROGRAM_STAGE_ID );
            // scheduledANCVISITS24CustomeSMS( MOBILE_NUMBER_ATTRIBUTE_ID,
            // ANC_VISITS_2_4_PROGRAM_STAGE_ID );
            // scheduledPNCProgrammeCustomeSMS( MOBILE_NUMBER_ATTRIBUTE_ID,
            // POST_NATAL_CARE_PROGRAM_STAGE_ID );
            // scheduledChildHealthProgrammeCustomeSMS(
            // MOBILE_NUMBER_ATTRIBUTE_ID,
            // CHILD_HEALTH_IMMUNIZATION_PROGRAM_STAGE_ID );
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

        // daily Message

        /*
         * try { scheduledCustomeSMS( mobileNumbers, dailyMessages ); } catch (
         * IOException e ) { e.printStackTrace(); System.out.println(
         * "Error SMS " + e.getMessage() ); }
         */

    }

    // -------------------------------------------------------------------------
    // Support methods
    // -------------------------------------------------------------------------

    // NPCDCS Program (On Scheduling)
    public void scheduledNPCDCSProgramCustomeSMS( Integer mobile_attribute_id, Integer program_stage_id )
        throws IOException
    {
        System.out.println( " NPCDCS_FOLLOW_UP SMS Scheduler Started at : " + new Date() + " -- current date  -  "
            + todayDate );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( NAME_ATTRIBUTE_ID );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + program_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + mobile_attribute_id;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );

                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // one day before
                    Calendar oneDayBefore = Calendar.getInstance();
                    oneDayBefore.setTime( dueDateObject );
                    oneDayBefore.add( Calendar.DATE, -1 );
                    Date oneDayBeforeDate = oneDayBefore.getTime();

                    String oneDayBeforeDateString = simpleDateFormat.format( oneDayBeforeDate );

                    // System.out.println( " 11-------- oneDayBeforeDateString "
                    // + oneDayBeforeDateString );
                    if ( todayDate.equalsIgnoreCase( oneDayBeforeDateString ) )
                    {
                        // System.out.println(
                        // " 12-------- oneDayBeforeDateString " +
                        // oneDayBeforeDateString );
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        String teiName = " ";
                        if ( teaValue != null )
                        {
                            if ( teaValue.getValue() != null )
                            {
                                teiName = teaValue.getValue();
                            }
                        }

                        /*
                         * old String customMessage = teiName + " " +
                         * " डिस्पेंसरी में उच्च रक्त चाप, मधुमेह, स्ट्रोक वा केंसर जाँच कार्यक्रम में आप में पाई गयी बीमारी कि नियमित जाँच/ चेक-अप के लिए आपको डिस्पेंसरी में "
                         * + dueDate + " " +
                         * " को 9 बजे से लेकर 12 बजे का समय दिया गया हैं |  स्वस्थ रहने के लिए ज़रूरी है की आप समय समय पर अपनी जाँच करते रहें "
                         * ;
                         */
                        String customMessage = teiName
                            + " "
                            + ", 25 सेक्टर की डिस्पेंसरी में हाइ बीपी, शुगर, स्ट्रोक और कैंसर प्रोग्राम में जाँच के दौरान आप  में पाई गयी बीमारी की नियमित जाँच के लिए आप को दिनांक "
                            + dueDate
                            + " "
                            + "को डिस्पेंसरी आने का अनुरोध  किया जाता है| डिस्पेंसरी सुबह 9 बजे से दोपहर 2 बजे तक खुली होती है| “स्वस्थ रहने के लिए ज़रूरी  है कि आप समय समय पर अपनी जाँच कराते रहें ";

                        // bulkSMSHTTPInterface.sendUnicodeSMS( customMessage,
                        // mobileNo );
                        System.out.println( teaValue.getValue() + " -------- > " + customMessage + " -------- >"
                            + mobileNo );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( " NPCDCS_FOLLOW_UP SMS Scheduler End at : " + new Date() );
    }

    // ANC Programme (On Scheduling)
    public void scheduledANCProgrammeCustomeSMS( Integer mobile_attribute_id, Integer program_stage_id )
        throws IOException
    {
        System.out.println( " ANC Programme SMS Scheduler Started at : " + new Date() + " -- current date  -  "
            + todayDate );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( NAME_ATTRIBUTE_ID );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + program_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + mobile_attribute_id;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );

                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // one day before
                    Calendar oneDayBefore = Calendar.getInstance();
                    oneDayBefore.setTime( dueDateObject );
                    oneDayBefore.add( Calendar.DATE, -1 );
                    Date oneDayBeforeDate = oneDayBefore.getTime();

                    String oneDayBeforeDateString = simpleDateFormat.format( oneDayBeforeDate );
                    // System.out.println( " 21-------- oneDayBeforeDateString "
                    // + oneDayBeforeDateString );
                    if ( todayDate.equalsIgnoreCase( oneDayBeforeDateString ) )
                    {
                        // System.out.println(
                        // " 22-------- oneDayBeforeDateString " +
                        // oneDayBeforeDateString );
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        String teiName = " ";
                        if ( teaValue != null )
                        {
                            if ( teaValue.getValue() != null )
                            {
                                teiName = teaValue.getValue();
                            }
                        }

                        /*
                         * String customMessage = teiName + " " +
                         * " आपकी प्रसवपूर्व देखभाल  कि नियमित जाँच के लिए आपको डिस्पेंसरी में  "
                         * + dueDate + " " +
                         * " को 9 बजे से लेकर 12 बजे के बीच का समय दिया गया हैं | गर्भ अवस्था में माँ और बच्चे के स्वास्थ्य  के लिए समय समय पर जाँच करना ज़रूरी है"
                         * ;
                         */

                        String customMessage = teiName
                            + " "
                            + ", सूचित किया जाता है कि प्रेग्नेन्सी दौरान देखभाल हेतु नियमित जाँच के लिए आपको   डिस्पेंसरी में दिनांक  "
                            + dueDate
                            + " "
                            + " को आने का अनुरोध किया जाता है| डिस्पेंसरी सुबह 9 बजे से दोपहर 2 बजे तक खुली होती है| 'गर्भ अवस्था में माँ और बच्चे के स्वास्थ्य के लिए समय समय पर  जाँच करवाना ज़रूरी है ' ";

                        bulkSMSHTTPInterface.sendUnicodeSMS( customMessage, mobileNo );
                        System.out.println( teaValue.getValue() + " -------- > " + customMessage + " -------- >"
                            + mobileNo );

                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( "ANC Programme  SMS Scheduler End at : " + new Date() );
    }

    // ANC Programme 2 and 4 (On Scheduling)
    public void scheduledANCVISITS24CustomeSMS( Integer mobile_attribute_id, Integer program_stage_id )
        throws IOException
    {
        System.out.println( " ANC Programme 2 and 4 SMS Scheduler Started at : " + new Date() + " -- current date  -  "
            + todayDate );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( NAME_ATTRIBUTE_ID );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + program_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + mobile_attribute_id;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );

                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // one day before
                    Calendar oneDayBefore = Calendar.getInstance();
                    oneDayBefore.setTime( dueDateObject );
                    oneDayBefore.add( Calendar.DATE, -1 );
                    Date oneDayBeforeDate = oneDayBefore.getTime();

                    String oneDayBeforeDateString = simpleDateFormat.format( oneDayBeforeDate );
                    // System.out.println( " 31-------- oneDayBeforeDateString "
                    // + oneDayBeforeDateString );
                    if ( todayDate.equalsIgnoreCase( oneDayBeforeDateString ) )
                    {
                        // System.out.println(
                        // " 32-------- oneDayBeforeDateString " +
                        // oneDayBeforeDateString );
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        String teiName = " ";
                        if ( teaValue != null )
                        {
                            if ( teaValue.getValue() != null )
                            {
                                teiName = teaValue.getValue();
                            }
                        }

                        /*
                         * String customMessage = teiName + " " +
                         * " आपकी प्रसवपूर्व देखभाल  कि नियमित जाँच के लिए आपको डिस्पेंसरी में  "
                         * + dueDate + " " +
                         * " को 9 बजे से लेकर 12 बजे के बीच का समय दिया गया हैं | गर्भ अवस्था में माँ और बच्चे के स्वास्थ्य  के लिए समय समय पर जाँच करना ज़रूरी है"
                         * ;
                         */

                        String customMessage = teiName
                            + " "
                            + ", सूचित किया जाता है कि प्रेग्नेन्सी दौरान देखभाल हेतु नियमित जाँच के लिए आपको   डिस्पेंसरी में दिनांक  "
                            + dueDate
                            + " "
                            + " को आने का अनुरोध किया जाता है| डिस्पेंसरी सुबह 9 बजे से दोपहर 2 बजे तक खुली होती है| 'गर्भ अवस्था में माँ और बच्चे के स्वास्थ्य के लिए समय समय पर  जाँच करवाना ज़रूरी है ' ";

                        bulkSMSHTTPInterface.sendUnicodeSMS( customMessage, mobileNo );
                        System.out.println( teaValue.getValue() + " -------- > " + customMessage + " -------- >"
                            + mobileNo );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( " ANC Programme 2 and 4 SMS Scheduler End at : " + new Date() );
    }

    // PNC Programme(On Scheduling)
    public void scheduledPNCProgrammeCustomeSMS( Integer mobile_attribute_id, Integer program_stage_id )
        throws IOException
    {
        System.out.println( " PNC Programme SMS Scheduler Started at : " + new Date() + " -- current date  -  "
            + todayDate );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( NAME_ATTRIBUTE_ID );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + program_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + mobile_attribute_id;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );

                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // one day before
                    Calendar oneDayBefore = Calendar.getInstance();
                    oneDayBefore.setTime( dueDateObject );
                    oneDayBefore.add( Calendar.DATE, -1 );
                    Date oneDayBeforeDate = oneDayBefore.getTime();

                    String oneDayBeforeDateString = simpleDateFormat.format( oneDayBeforeDate );
                    // System.out.println( " 41-------- oneDayBeforeDateString "
                    // + oneDayBeforeDateString );
                    if ( todayDate.equalsIgnoreCase( oneDayBeforeDateString ) )
                    {
                        // System.out.println(
                        // " 42-------- oneDayBeforeDateString " +
                        // oneDayBeforeDateString );
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        String teiName = " ";
                        if ( teaValue != null )
                        {
                            if ( teaValue.getValue() != null )
                            {
                                teiName = teaValue.getValue();
                            }
                        }

                        /*
                         * String customMessage = teiName + " " +
                         * " आपकी प्रसव के बाद कि देखभाल कि नियमित जाँच के लिए आपसे    "
                         * + dueDate + " " +
                         * " को डिस्पेंसरी  कि एएनएम या आशा दीदी आकर मिलेंगी |";
                         */

                        String customMessage = teiName
                            + " "
                            + " ,आपकी डेलिवरी के बाद की देखभाल हेतु दिनांक   "
                            + dueDate
                            + " को डिस्पेंसरी की आशा या ए एन एम "
                            + " "
                            + " दीदी आपको मिलने आएँगी| उस दौरान माँ और नवजात से संबंधित कोई भी दिक्कत हो तो दीदी को बताएँ| वो आप  की मदद करेंगी |";

                        bulkSMSHTTPInterface.sendUnicodeSMS( customMessage, mobileNo );
                        System.out.println( teaValue.getValue() + " -------- > " + customMessage + " -------- >"
                            + mobileNo );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( " PNC Programme SMS Scheduler End at : " + new Date() );
    }

    // Child Health Programme(On Scheduling)
    public void scheduledChildHealthProgrammeCustomeSMS( Integer mobile_attribute_id, Integer program_stage_id )
        throws IOException
    {
        System.out.println( " Child Health Programme(On Scheduling) SMS Scheduler Started at : " + new Date()
            + " -- current date  -  " + todayDate );

        TrackedEntityAttribute teAttribute = trackedEntityAttributeService
            .getTrackedEntityAttribute( NAME_ATTRIBUTE_ID );

        BulkSMSHttpInterface bulkSMSHTTPInterface = new BulkSMSHttpInterface();

        try
        {
            String query = "SELECT pi.trackedentityinstanceid, psi.organisationunitid, psi.duedate::date,teav.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentityattributevalue teav ON teav.trackedentityinstanceid = pi.trackedentityinstanceid "
                + "WHERE psi.programstageid = "
                + program_stage_id
                + " AND psi.status = 'SCHEDULE' and  "
                + "teav.trackedentityattributeid =  " + mobile_attribute_id;

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiID = rs.getInt( 1 );
                Integer orgUnitID = rs.getInt( 2 );
                String dueDate = rs.getString( 3 );
                String mobileNo = rs.getString( 4 );

                if ( teiID != null && orgUnitID != null && dueDate != null && mobileNo != null
                    && mobileNo.length() == 10 )
                {
                    Date dueDateObject = simpleDateFormat.parse( dueDate );

                    // one day before
                    Calendar oneDayBefore = Calendar.getInstance();
                    oneDayBefore.setTime( dueDateObject );
                    oneDayBefore.add( Calendar.DATE, -1 );
                    Date oneDayBeforeDate = oneDayBefore.getTime();

                    String oneDayBeforeDateString = simpleDateFormat.format( oneDayBeforeDate );
                    // System.out.println( " 51-------- oneDayBeforeDateString "
                    // + oneDayBeforeDateString );
                    if ( todayDate.equalsIgnoreCase( oneDayBeforeDateString ) )
                    {
                        // System.out.println(
                        // " 52-------- oneDayBeforeDateString " +
                        // oneDayBeforeDateString );
                        TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiID );
                        TrackedEntityAttributeValue teaValue = trackedEntityAttributeValueService
                            .getTrackedEntityAttributeValue( tei, teAttribute );
                        OrganisationUnit orgUnit = organisationUnitService.getOrganisationUnit( orgUnitID );

                        String teiName = " ";
                        if ( teaValue != null )
                        {
                            if ( teaValue.getValue() != null )
                            {
                                teiName = teaValue.getValue();
                            }
                        }
                        /*
                         * String customMessage = teiName + " " +
                         * " आपके बच्चे के टीकाकरण के लिए आपको  " + dueDate +
                         * " " +
                         * " को 9 बजे से लेकर 12 बजे के बीच डिस्पेंसरी में आकर बच्चे को टीका लगवाना हैं  | बच्चे को जानलेवा बीमारियों से बचाने के लिए टीकाकरण करना ज़रूरी होता है |"
                         * ;
                         */
                        String customMessage = teiName
                            + " "
                            + " , कृपा अपने बच्चे को दिनांक   "
                            + dueDate
                            + " वाले दिन डिस्पेंसरी में  ला कर उस का टीकाकरण करवाएँ| "
                            + " "
                            + " डिस्पेंसरी सुबह 9 बजे से दोपहर 2 बजे तक खुली होती है| 'बच्चे को जानलेवा बीमारियों से बचाने के लिए टीकाकरण करवाना ज़रूरी होता है '";

                        bulkSMSHTTPInterface.sendUnicodeSMS( customMessage, mobileNo );
                        System.out.println( teaValue.getValue() + " -------- > " + customMessage + " -------- >"
                            + mobileNo );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }

        System.out.println( " Child Health Programme(On Scheduling) SMS Scheduler End at : " + new Date() );
    }

    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    // --------------------------------------------------------------------------------
    public String getTrackedEntityInstanceIdsByAttributeId( Integer attributeId )
    {
        String trackedEntityInstanceIds = "-1";

        try
        {
            String query = "SELECT trackedentityinstanceid FROM trackedentityattributevalue "
                + "WHERE value = 'true' AND trackedentityattributeid =  " + attributeId
                + " order by trackedentityinstanceid ASC ";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                if ( teiId != null )
                {
                    trackedEntityInstanceIds += "," + teiId;
                }
            }

            return trackedEntityInstanceIds;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }

    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    // --------------------------------------------------------------------------------
    public List<String> getTrackedEntityInstanceAttributeValueByAttributeIdAndTrackedEntityInstanceIds(
        Integer attributeId, String trackedEntityInstanceIdsByComma )
    {
        List<String> mobileNumbers = new ArrayList<String>();

        try
        {
            String query = "SELECT value FROM trackedentityattributevalue " + "WHERE trackedentityattributeid =  "
                + attributeId + " AND trackedentityinstanceid in ( " + trackedEntityInstanceIdsByComma + ")";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                String mobileNo = rs.getString( 1 );
                if ( mobileNo != null )
                {
                    mobileNumbers.add( mobileNo );
                }
            }

            return mobileNumbers;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
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
    
    
    
    
    
    
    
    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance from tracked entity attribute value
    // --------------------------------------------------------------------------------
    public List<TrackedEntityInstance> getTrackedEntityInstancesByAttributeId( Integer attributeId )
    {
        List<TrackedEntityInstance> teiList = new ArrayList<TrackedEntityInstance>();

        try
        {
            String query = "SELECT trackedentityinstanceid FROM trackedentityattributevalue "
                + "WHERE value = 'true' AND trackedentityattributeid =  " + attributeId
                + " order by trackedentityinstanceid ASC ";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer teiId = rs.getInt( 1 );
                if ( teiId != null )
                {
                    TrackedEntityInstance tei = trackedEntityInstanceService.getTrackedEntityInstance( teiId );
                    teiList.add( tei );
                }
            }

            return teiList;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }

    // --------------------------------------------------------------------------------
    // Get TrackedEntityInstance Ids from tracked entity attribute value
    // --------------------------------------------------------------------------------
    public String getLatestEventOrgAndDataValue( Integer psId, Integer dataElementId, Integer teiId )
    {
        String orgUnitIdAndValue = "";
        List<String> tempResult = new ArrayList<>();
        try
        {
            String query = "SELECT psi.organisationunitid, tedv.dataelementid,tedv.value FROM programstageinstance psi "
                + "INNER JOIN programinstance pi ON  pi.programinstanceid = psi.programinstanceid "
                + "INNER JOIN trackedentitydatavalue tedv  ON  tedv.programstageinstanceid = psi.programstageinstanceid "
                + "WHERE psi.programstageid = "
                + psId
                + " AND tedv.dataelementid = "
                + dataElementId
                + "  AND pi.trackedentityinstanceid =  " + teiId + " order by psi.lastupdated desc ";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer orgUnitId = rs.getInt( 1 );
                String value = rs.getString( 3 );

                if ( orgUnitId != null && value != null )
                {
                    tempResult.add( orgUnitId + ":" + value );
                }
            }

            if ( tempResult != null && tempResult.size() > 0 )
            {
                orgUnitIdAndValue = tempResult.get( 0 );
            }

            return orgUnitIdAndValue;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal Attribute id", e );
        }
    }

    //
    private String convertHtmlEntities( String s )
    {
        Pattern pattern = Pattern.compile( "\\&#(\\d{1,7});" );
        Matcher m = pattern.matcher( s );
        StringBuffer sb = new StringBuffer();
        while ( m.find() )
        {
            int cp = Integer.parseInt( m.group( 1 ) );
            String ch = new String( new int[] { cp }, 0, 1 );
            m.appendReplacement( sb, ch );
        }
        m.appendTail( sb );
        return sb.toString();
    }

    //
    public void testDhis2SampleSheet()
        throws Exception
    {
        System.out.println( "Inside testDhis2SampleSheet " );
        // The ID of the spreadsheet to update.
        String spreadsheetId = "1nUCNUPHrxltE2yiyoNifbuYJJzNKgbhRBPnQMCc3ptw"; // TODO:
                                                                               // Update
                                                                               // placeholder
        // value.
        // https://docs.google.com/spreadsheets/d/1nUCNUPHrxltE2yiyoNifbuYJJzNKgbhRBPnQMCc3ptw/edit?usp=sharing
        // The A1 notation of the values to update.
        //String range = "A4:H"; // TODO: Update placeholder value.
        //final String range = "Class Data!A2:E";
        //String range =  "Sheet1!A1:E";
        // How the input data should be interpreted.
        
        //String valueInputOption = "test-value"; // TODO: Update placeholder
        
        String valueInputOption = "USER_ENTERED";// value.
        String range = "Sheet1!A1:B5";
        String range1 = "Sheet1!B2:D5";
        String majorDimensionRow =  "ROWS";
        String majorDimensionColumn =  "COLUMNS";
        
        //Sheets sheetsService = createSheetsService();
        
        // TODO: Assign values to desired fields of `requestBody`. All existing
        // fields will be replaced:
        //WriteExample();
        
        // for batch update
        
        // The new values to apply to the spreadsheet.
        /*
        List<ValueRange> data = new ArrayList<>(); // TODO: Update placeholder value.
        
        // TODO: Assign values to desired fields of `requestBody`:
        BatchUpdateValuesRequest requestBody = new BatchUpdateValuesRequest();
        requestBody.setValueInputOption(valueInputOption);
        requestBody.setData(data);

        Sheets sheetsService = createSheetsService();
        Sheets.Spreadsheets.Values.BatchUpdate request = sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, requestBody);

        BatchUpdateValuesResponse response = request.execute();
       
        System.out.println(response);
        */
        
        
        // How the input data should be interpreted.
        //String valueInputOption = ""; // TODO: Update placeholder value.

        // How the input data should be inserted.
        
        /*
        String insertDataOption = "INSERT-Value"; // TODO: Update placeholder value.

        // TODO: Assign values to desired fields of `requestBody`:
        ValueRange requestBody = new ValueRange();

        Sheets sheetsService = createSheetsService();
        
        Sheets.Spreadsheets.Values.Append request =
            sheetsService.spreadsheets().values().append(spreadsheetId, range, requestBody);
        request.setValueInputOption(valueInputOption);
        request.setInsertDataOption(insertDataOption);

        AppendValuesResponse response = request.execute();
        
        System.out.println(response);
        */
        // TODO: Change code below to process the `response` object:
        


        //
       
        /*
        String content ="just now";
        List<Object> data1 = new ArrayList<>();
        data1.add (content);
        List<List<Object>> data = new ArrayList<>();
        data.add (data1);
        ValueRange valueRange=new ValueRange();
        valueRange.setValues(data);
        Sheets sheetsService = createSheetsService();
        AppendValuesResponse response = sheetsService.spreadsheets().values().append(spreadsheetId, range, valueRange).setValueInputOption("RAW").execute();
        
        System.out.println( "Response -- " + response );
        */ 
        
        //
        
        /*
        ValueRange requestBody = new ValueRange();

        Sheets sheetsService = createSheetsService();
        
        System.out.println( "Inside service Application Name " + sheetsService.getApplicationName() );
        
        System.out.println( "Inside service " + sheetsService );
        
        
        Sheets.Spreadsheets.Values.Update request = sheetsService.spreadsheets().values().update( spreadsheetId, range, requestBody );
        request.setValueInputOption( "RAW" );

        UpdateValuesResponse response = request.execute();
        
        System.out.println( "Response -- " + response );
        */
        
        // TODO: Change code below to process the `response` object:
        
        
        /*
        {
            "range":"Sheet1!A1",
            "majorDimension": "ROWS",
            "values": [
              ["Hello World"]
            ],
          }
        
        */
       
        try 
        {
            
            Sheets sheetsService = createSheetsService();
            
            String tempcontent1 ="Mithilesh";
            String tempcontent2 ="Kumar";
            String tempcontent3 ="Thakur";
            List<Object> myData = new ArrayList<>();
            myData.add (tempcontent1 );
            myData.add (tempcontent2 );
            myData.add (tempcontent3 );
          
            
            List<List<Object>> writeData = new ArrayList<>();
            for (Object someData: myData) 
            {
                List<Object> dataRow = new ArrayList<>();
                dataRow.add( someData );
                writeData.add(dataRow);
            }

            //ValueRange vr = new ValueRange().setValues(writeData).setMajorDimension( majorDimensionColumn );
            ValueRange vr = new ValueRange().setValues(writeData);
            //Sheets.sheetsService().values().update(id, writeRange, vr).setValueInputOption("RAW").execute();
            
            Sheets.Spreadsheets.Values.Update updateRequest = sheetsService.spreadsheets().values().update( spreadsheetId, range, vr );
            updateRequest.setValueInputOption( "RAW" );

            UpdateValuesResponse updateResponse = updateRequest.execute();
            System.out.println( "Update Response -- " + updateResponse );
        }
        catch (Exception e) 
        {
            // handle exception
        }     
        
        
        
        /*
        List<List<Object>> values = Arrays.asList(
            Arrays.asList(
                    // Cell values ...
                )
            // Additional rows ...
            );
        
        List<ValueRange> data = new ArrayList<ValueRange>();
        
        String tempcontent1 ="Mithilesh";
        String tempcontent2 ="Kumar";
        String tempcontent3 ="Thakur";
        List<Object> myData = new ArrayList<>();
        myData.add (tempcontent1 );
        myData.add (tempcontent2 );
        myData.add (tempcontent3 );
      
        
        List<List<Object>> writeData = new ArrayList<>();
        for (Object someData: myData) 
        {
            List<Object> dataRow = new ArrayList<>();
            dataRow.add( someData );
            writeData.add(dataRow);
            data.add(new ValueRange().setRange(range).setValues(values));
            // Additional ranges to update ...
        }
        
        List<ValueRange> datas = new ArrayList<ValueRange>();
        datas.add(new ValueRange().setRange(range).setValues(writeData));
        datas.add(new ValueRange().setRange(range1).setValues(writeData));
        
        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest().setValueInputOption(valueInputOption).setData(datas);
        BatchUpdateValuesResponse result = sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();
        System.out.printf("%d cells updated.", result.getTotalUpdatedCells());
        */
        


        
        
        
    }

    public void WriteExample() throws Exception 
    {
        String spreadsheetId = "1nUCNUPHrxltE2yiyoNifbuYJJzNKgbhRBPnQMCc3ptw";
        Sheets service = createSheetsService();
        System.out.println( "Inside WriteExample Application Name " + service.getApplicationName() );
        
        System.out.println( "Inside WriteExample service " + service );
        List<Request> requests = new ArrayList<>();

          List<CellData> values = new ArrayList<>();

          values.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("Hello World!")));
            requests.add(new Request().setUpdateCells(new UpdateCellsRequest()
                            .setStart(new GridCoordinate()
                                    .setSheetId(0)
                                    .setRowIndex(0)
                                    .setColumnIndex(0))
                            .setRows(Arrays.asList(
                                    new RowData().setValues(values)))
                            .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));

            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            service.spreadsheets().batchUpdate( spreadsheetId, batchUpdateRequest).execute();
            
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static Sheets createSheetsService()
        throws Exception
    {
        System.out.println( "Inside createSheetsService " );
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

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String range = "Class Data!A2:E";
        // Sheets service = new Sheets.Builder( HTTP_TRANSPORT, JSON_FACTORY,
        // getCredentials( HTTP_TRANSPORT ) ).setApplicationName(
        // APPLICATION_NAME ).build();

        // GoogleCredential credential = null;

        //GoogleCredential credential = (GoogleCredential) getCredentials( HTTP_TRANSPORT );

        Credential credential =  getCredentials( HTTP_TRANSPORT );;
        
        return new Sheets.Builder( httpTransport, jsonFactory, credential ).setApplicationName(
            "Google-SheetsSample/0.1" ).build();
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs
     * .google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms
     * /edit
     * @throws URISyntaxException 
     */

    public void sheetsQuickstart()
        throws IOException, GeneralSecurityException, URISyntaxException
    {
        // Build a new authorized API client service.
        System.out.println( "Inside sheetsQuickstart " );
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1nUCNUPHrxltE2yiyoNifbuYJJzNKgbhRBPnQMCc3ptw";
        final String range = "Class Data!A2:E";
        
        Sheets service = new Sheets.Builder( HTTP_TRANSPORT, JSON_FACTORY, getCredentials( HTTP_TRANSPORT ) ).setApplicationName( APPLICATION_NAME ).build();
        
        System.out.println( "Inside service Application Name " + service.getApplicationName() );
        
        System.out.println( "Inside service " + service );
        
        ValueRange response = service.spreadsheets().values().get( spreadsheetId, range ).execute();
        
        List<List<Object>> values = response.getValues();
        if ( values == null || values.isEmpty() )
        {
            System.out.println( "No data found." );
        }
        else
        {
            System.out.println( "Name, Major" );
            for ( List row : values )
            {
                // Print columns A and E, which correspond to indices 0 and 4.
                System.out.printf( "%s, %s\n", row.get( 0 ), row.get( 4 ) );
            }
        }
    }

    
    /**
     * Creates an authorized Credential object.
     * 
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     * @throws URISyntaxException 
     */
    @SuppressWarnings( "unused" )
    private static Credential getCredentials( final NetHttpTransport HTTP_TRANSPORT )
        throws IOException, URISyntaxException
    {
        System.out.println( "Inside getCredentials 1 " );

        // Load client secrets.
        String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + CREDENTIALS_FILE_PATH;
        
        System.out.println( "inputTemplatePath " + inputTemplatePath );

        //InputStream in = ScheduleCustomeSMSTask.class.getResourceAsStream( CREDENTIALS_FILE_PATH );
        Reader in = new FileReader( inputTemplatePath );
        // Load client secrets.
        
        File f = new File( inputTemplatePath );
        URI u = f.toURI();
        
        //Load client secrets 
        //URI filePath = new URI ( inputTemplatePath );      
        //Reader clientSecretReader = new InputStreamReader(new FileInputStream (u.toString()));
        //Reader clientSecretReader = new  InputStreamReader(ScheduleCustomeSMSTask.class.getResourceAsStream(inputTemplatePath));
        System.out.println( "Inside getCredentials 2 " + in );
        //GoogleClientSecrets clientSecrets =  GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);
         

        
        //GoogleClientSecrets clientSecrets = GoogleClientSecrets.load( JSON_FACTORY, new InputStreamReader( in ) );
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load( JSON_FACTORY,  in );

        System.out.println( "Inside getCredentials 3 " + clientSecrets );

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder( HTTP_TRANSPORT, JSON_FACTORY,
            clientSecrets, SCOPES )
            .setDataStoreFactory( new FileDataStoreFactory( new java.io.File( TOKENS_DIRECTORY_PATH ) ) )
            .setAccessType( "offline" ).setApprovalPrompt("force") .build();
        LocalServerReceiver receier = new LocalServerReceiver.Builder().setPort( 8888 ).build();
        try
        {
            return new AuthorizationCodeInstalledApp( flow, receier ).authorize( "user" );
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            System.out.println( "Inside getCredentials 4 Catch Block " + clientSecrets );
            e.printStackTrace();
        }
        return null;
    }

    public static Credential authorize( final NetHttpTransport HTTP_TRANSPORT )
    {
        // Load client secrets.
        System.out.println( "Inside authorize 1 " );
        String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + CREDENTIALS_FILE_PATH;
        System.out.println( "inputTemplatePath " + inputTemplatePath );
        final InputStream in = ScheduleCustomeSMSTask.class.getResourceAsStream( inputTemplatePath );
        System.out.println( "Inside authorize 2 " + in );
        
        Credential credential = null;
        try
        {
            final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load( JSON_FACTORY,
                new InputStreamReader( in ) );

            // Build flow and trigger user authorization request.
            final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder( HTTP_TRANSPORT,
                JSON_FACTORY, clientSecrets, SCOPES )
                .setDataStoreFactory( new FileDataStoreFactory( new java.io.File( TOKENS_DIRECTORY_PATH ) ) )
                .setAccessType( "offline" ).build();
            try
            {
                credential = new AuthorizationCodeInstalledApp( flow, new LocalServerReceiver() ).authorize( "user" );
            }
            catch ( Exception e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch ( final IOException e )
        {
            //throw new ResourceServerException( "Incorrect authorization", e );
            throw new RuntimeException( "Incorrect authorization", e  );
        }
        return credential;
    }

    @SuppressWarnings( "unused" )
    private static Credential getAuthorize( final NetHttpTransport HTTP_TRANSPORT )
        throws Exception
    {
        // load client secrets
        System.out.println( "Inside authorize 1 " );
        String inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + CREDENTIALS_FILE_PATH;
        System.out.println( "inputTemplatePath " + inputTemplatePath );
        Reader in = new FileReader( inputTemplatePath );
        
        System.out.println( "Inside getAuthorize 2 " + in );
        
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load( JSON_FACTORY, in );
        if ( clientSecrets.getDetails().getClientId().startsWith( "Your " )
            || clientSecrets.getDetails().getClientSecret().startsWith( "Your " ) )
        {
            System.out.println( "Enter Client ID and Secret from https://code.google.com/apis/console/?api=plus "
                + "into client_secrets.json" );
            System.exit( 1 );
        }
        // set up authorization code flow
        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder( HTTP_TRANSPORT,
            JSON_FACTORY, clientSecrets, SCOPES )
            .setDataStoreFactory( new FileDataStoreFactory( new java.io.File( TOKENS_DIRECTORY_PATH ) ) )
            .setAccessType( "offline" ).build();
        // authorize
        return new AuthorizationCodeInstalledApp( flow, new LocalServerReceiver() ).authorize( "user" );
    }
    

    
}
