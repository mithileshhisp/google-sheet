package org.hisp.dhis.schedulecustomesms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;




import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.hisp.dhis.constant.Constant;


/**
 * @author Mithilesh Kumar Thakur
 */
public class BulkSMSHttpInterface
{
    public static final String SMS_USER_GROUP_ID = "SMS_USER_GROUP_ID";//
    
    private String username, password, phoneNo, senderName;

    private URL url;

    private String url_string, data, response = "";

    Properties properties;
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }
    
    private UserGroupService userGroupService;

    public void setUserGroupService( UserGroupService userGroupService )
    {
        this.userGroupService = userGroupService;
    }
    
    
    /*****this constructor takes the username , password and sendername from a file a file**********/
    /*
    public BulkSMSHttpInterface() throws FileNotFoundException, IOException
    {
        properties = new Properties();

        properties.load( new FileReader( System.getenv( "DHIS2_HOME" ) + File.separator + "hibernate.properties" ) );
        username = getUsername();
        password = getPassword();
        senderName = getSenderName();
    }
    
    
    public BulkSMSHttpInterface( String username, String password, String senderName )
    {
        this.username = username;
        this.password = password;
        this.senderName = senderName;
    }    
    */
    // getter
    /*
    public String getUsername()
    {
        return properties.getProperty( "bsms.username" );
    }

    public String getPassword()
    {
        return properties.getProperty( "bsms.password" );
    }

    public String getSenderName()
    {
        return properties.getProperty( "bsms.sender" );
    }
    */
    
    // sending message to single mobile no
    public String sendMessage( String message, String phoneNo ) throws MalformedURLException, IOException
    {
        if (message==null || phoneNo==null)
        {
            return "either message or phone no null";
        }
        
        else if (message.equalsIgnoreCase( "") || phoneNo.equalsIgnoreCase( "") )
        {
            return "either message or phone no empty";
        }
        
        
        username = "hispindia";
        password = "hisp1234";
        senderName = "HSSPIN";
        
        String token = "2Ca4N06OnVfBBl0BMemc";
        String from = "infoSMS";
        
        //Populating the data according to the api link
        
        //data = "username=" + username + "&password=" + password + "&sendername=" + senderName + "&mobileno=" + phoneNo + "&message=" + message;
        
        data = "token=" + token + "&from=" + from + "&to=" + phoneNo + "&text=" + message;
        
        //data = "username=" + username + "&password=" + password +  "&to=" + phoneNo + "&from=" + senderName + "&text=" + message;
        
        //http://myvaluefirst.com/smpp/sendsms?username=nrhmhttp&password=nrhm1234&to=9643208387&from=NRHMHR&text=hi

        //this link is used for sending sms(there are different links for different functions.refer to the api for more details)
        //url_string = "http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?";
        url_string = "http://api.sparrowsms.com/v2/sms/?";
        
        //url_string = "https://myvaluefirst.com/smpp/sendsms?";
        
       // http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=9643274071&message=aaaaa
        
        //http://api.sparrowsms.com/v2/sms/?token=2Ca4N06OnVfBBl0BMemc&from=infoSMS&to=9643274071&text=test
        
        //System.out.println(" Mobile No -------------------->"+ phoneNo );
        
        //System.out.println(" URL -------------------->"+ url_string );
        
        //System.out.println(" Data -------------------->"+ data );
        
        //System.out.println(" Data -------------------->"+ message );
        
        url = new URL( url_string );
        URLConnection conn = url.openConnection();
        conn.setDoOutput( true );
        
        //sending data:
        OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream() );
        out.write( data );
        out.flush();

        //recieving response:
        InputStreamReader in = new InputStreamReader( conn.getInputStream() );
        BufferedReader buff_in = new BufferedReader( in );
        while ( buff_in.ready() )
        {
            response += buff_in.readLine() + "   ";
            //System.out.println( response + " " + data );
        }

        buff_in.close();
        out.close();

        return response;
    }
    
    
    // sending message to single mobile no
    public String sendSMS( String message, String phoneNo )
        throws UnsupportedEncodingException
    {
        String resopnseString = "";
        System.out.println( phoneNo + " -- 1 -- " + message );
        
        String finalmessage = "";
       
        	        for(int i = 0 ; i< message.length();i++){
       
        	 
       
        	            char ch = message.charAt(i);
       
        	            int j = (int) ch;
        
        	            String sss = "&#"+j+";";
        
        	            finalmessage = finalmessage+sss;
        
        	        }
        
        
        // System.out.println(encodeMessage(new String(message.getBytes())));
        try
        {
            // Construct data
            
            /*
            String[] tempMessage = message.split( "," );

            message = gujratiTranslationMap.get( "PERFECT_MESSAGE" ) + " " + gujratiTranslationMap.get( "male" ) + "("
                + tempMessage[0] + "," + tempMessage[1] + "," + tempMessage[2] + ")"
                + gujratiTranslationMap.get( "female" ) + "(" + tempMessage[3] + "," + tempMessage[4] + ","
                + tempMessage[5] + ")" + gujratiTranslationMap.get( "sideEffect" ) + "(" + tempMessage[6] + ") "
                + smsDate;
            
            String user = "username=" + "harsh.atal@gmail.com";
            String hash = "&hash=" + "04fa1b5546432e99162704a7025403879d589271";
            message = "&message=" + message;
            String sender = "&sender=" + "TXTLCL";
            
            String numbers = "&numbers=" + mobileNo + "&unicode=1";
            */
            //Populating the data according to the api link
            
            /*
            username = "hispindia";
            password = "hisp1234";
            senderName = "HSSPIN";
           
            data = "username=" + username + "&password=" + password + "&sendername=" + senderName + "&mobileno=" + phoneNo + "&message=" + message;
            url_string = "http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?";
            */
            
        	// for INPART
        	//http://msdgweb.mgov.gov.in/esms/sendsmsrequest?username=PHD25PGIMER&password=sph@25&smsservicetype=unicodemsg&content=test&mobileno=9643274071&senderid=PGIMER 
        	
        	username = "PHD25PGIMER";
        	password = "sph@25";
        	String senderid = "PGIMER";
            String smsservicetype = "unicodemsg";
            data = "username=" + username + "&password=" + password + "&smsservicetype=" + smsservicetype + "&content=" + finalmessage + "&mobileno=" + phoneNo + "&senderid=" + senderid;
            
            url_string = "http://msdgweb.mgov.gov.in/esms/sendsmsrequest?";
        	/*
            String token = "2Ca4N06OnVfBBl0BMemc";
            String from = "infoSMS";
            data = "token=" + token + "&from=" + from + "&to=" + phoneNo + "&text=" + message;
            */
            
            // Send data
            //HttpURLConnection conn = (HttpURLConnection) new URL( url_string ).openConnection();
            
            
            
            //Populating the data according to the api link
            
            //data = "username=" + username + "&password=" + password + "&sendername=" + senderName + "&mobileno=" + phoneNo + "&message=" + message;
            
            /*
            HttpURLConnection conn = (HttpURLConnection) new URL( "http://msdgweb.mgov.gov.in/esms/sendsmsrequest?" ).openConnection();
            conn.setDoOutput( true );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Length", Integer.toString( data.length() ) );
            conn.getOutputStream().write( data.getBytes( "UTF-8" ) );
            final BufferedReader rd = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
            final StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ( (line = rd.readLine()) != null )
            {
                stringBuffer.append( line );
            }

            rd.close();

            resopnseString = stringBuffer.toString();
            */
            
            
            url = new URL( url_string );
            URLConnection conn = url.openConnection();
            conn.setDoOutput( true );
            
            //sending data:
            OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream() );
            out.write( data );
            out.flush();

            //recieving response:
            InputStreamReader in = new InputStreamReader( conn.getInputStream() );
            BufferedReader buff_in = new BufferedReader( in );
            while ( buff_in.ready() )
            {
                response += buff_in.readLine() + "   ";
                //System.out.println( response + " " + data );
            }

            buff_in.close();
            out.close();

            //System.out.println( "SMS Response : --" + stringBuffer.toString() );
        }

        catch ( Exception e )
        {
            System.out.println( "Error SMS " + e );
        }
        
        return response;
        //return resopnseString;

    }
        
    // sending message to multiple mobile no
    public String sendMessages( String message, List<String> phonenos ) throws MalformedURLException, IOException
    {

        Iterator<String> it = phonenos.iterator();

        while ( it.hasNext() )
        {
            if ( phoneNo == null )
            {
                phoneNo = (String) it.next();
            } 
            
            else
            {
                phoneNo += "," + it.next();
            }
        }
        
        //System.out.println(" Mobile No -------------------->"+ phoneNo );

        data = "username=" + username + "&password=" + password + "&sendername=" + senderName + "&mobileno=" + phoneNo + "&message=" + message;

        //for sending multiple sms (same as single sms)
        //url_string = "http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?";
        
        url_string = "http://myvaluefirst.com/smpp/sendsms?";
        
        url = new URL( url_string );
        URLConnection conn = url.openConnection();
        conn.setDoOutput( true );
        
        //System.out.println(" URL -------------------->"+ url_string );
        
        //System.out.println(" Data -------------------->"+ data );
        
        OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream() );
        out.write( data );
        out.flush();

        InputStreamReader in = new InputStreamReader( conn.getInputStream() );
        BufferedReader buff_in = new BufferedReader( in );

        while ( buff_in.ready() )
        {
            response += buff_in.readLine() + "   ";
            //System.out.println( response + " " + data );

        }

        buff_in.close();
        out.close();

        return response;

    }   
    
    // get phoneNo of users
    public List<String> getUsersMobileNumber( Integer organisationUnitId )
    {
        List<String> mobileNumbers = new ArrayList<String>();
        
        System.out.println(" OrgUnit Id in SMS Service " + organisationUnitId );
        
        System.out.println(" Organisation Unit Service " + organisationUnitService );
        
        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();
        
        //List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getOrganisationUnitBranch( organisationUnitId ) );
        
        List<User> orgUnitUserList = new ArrayList<User>();
        for( OrganisationUnit orgUnit : orgUnitList )
        {
            if( orgUnit.getUsers() != null && orgUnit.getUsers().size() > 0 )
            {
                orgUnitUserList.addAll( orgUnit.getUsers() );
            }
        }
        
        // SMS user Details
        Constant smsUserGroupConstant = constantService.getConstantByName( SMS_USER_GROUP_ID );
        
        UserGroup userGroup = userGroupService.getUserGroup( (int) smsUserGroupConstant.getValue() );
        List<User> smsUsers = new ArrayList<User>( userGroup.getMembers() );
        
        smsUsers.retainAll( orgUnitUserList );
        
        try
        {
            for( User user : smsUsers )
            {
                if( user.getPhoneNumber() != null && user.getPhoneNumber().equalsIgnoreCase( "" ) )
                {
                    mobileNumbers.add( user.getPhoneNumber()  );
                }
            }
            
            System.out.println("-------------------- > " + mobileNumbers );
            
            return mobileNumbers;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal OrganisationUnit id", e );
        }
        
    }        
    
    // sending message to single mobile no
    /*
    public String sendHindiMessage( String message, String phoneNo ) throws MalformedURLException, IOException
    {
        if (message==null || phoneNo==null)
        {
            return "either message or phone no null";
        }
        
        else if (message.equalsIgnoreCase( "") || phoneNo.equalsIgnoreCase( "") )
        {
            return "either message or phone no empty";
        }
        username = "PHD25PGIMER";
    	password = "sph@25";
    	String senderId = "PGIMER";
        String smsservicetype = "unicodemsg";
        
        String response = null;
        String encryptedPassword;
        String genratedhashKey = hashGenerator(username, senderId, message, secureKey);
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod("http://msdgweb.mgov.gov.in/esms/sendsmsrequest");
        try 
        {
    
        	encryptedPassword  = MD5(password);
	   
        	method.addParameter(new NameValuePair("mobileno",mobileNumber));
	   
            method.addParameter(new NameValuePair("senderid",senderId));
   
            method.addParameter(new NameValuePair("content",message));
	
            method.addParameter(new NameValuePair("smsservicetype","singlemsg"));
	
            method.addParameter(new NameValuePair("username",username));
	   
            method.addParameter(new NameValuePair("password",encryptedPassword));
	
            method.addParameter(new NameValuePair("key",genratedhashKey));
	   
            client.executeMethod(method);
	    
            response = method.getResponseBodyAsString();
        
        } 
        catch (HttpException e) 
        {
        
        	e.printStackTrace();
        
        } 
	    catch (IOException e) 
	    {
	    
	    	e.printStackTrace();
	    
	    }
        catch (NoSuchAlgorithmException e) 
        {
        
	        // TODO Auto-generated catch block
	
	        e.printStackTrace();
        }
        
        	 
        return response;
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        username = "hispindia";
        password = "hisp1234";
        senderName = "HSSPIN";
        
        String token = "2Ca4N06OnVfBBl0BMemc";
        String from = "infoSMS";
        
        //Populating the data according to the api link
        
        //data = "username=" + username + "&password=" + password + "&sendername=" + senderName + "&mobileno=" + phoneNo + "&message=" + message;
        
        data = "token=" + token + "&from=" + from + "&to=" + phoneNo + "&text=" + message;
        
        //data = "username=" + username + "&password=" + password +  "&to=" + phoneNo + "&from=" + senderName + "&text=" + message;
        
        //http://myvaluefirst.com/smpp/sendsms?username=nrhmhttp&password=nrhm1234&to=9643208387&from=NRHMHR&text=hi

        //this link is used for sending sms(there are different links for different functions.refer to the api for more details)
        //url_string = "http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?";
        url_string = "http://api.sparrowsms.com/v2/sms/?";
        
        //url_string = "https://myvaluefirst.com/smpp/sendsms?";
        
       // http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=9643274071&message=aaa
        
        //http://api.sparrowsms.com/v2/sms/?token=2Ca4N06OnVfBBl0BMemc&from=infoSMS&to=9643274071&text=test
        
        //System.out.println(" Mobile No -------------------->"+ phoneNo );
        
        //System.out.println(" URL -------------------->"+ url_string );
        
        //System.out.println(" Data -------------------->"+ data );
        
        //System.out.println(" Data -------------------->"+ message );
        
        url = new URL( url_string );
        URLConnection conn = url.openConnection();
        conn.setDoOutput( true );
        
        //sending data:
        OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream() );
        out.write( data );
        out.flush();

        //recieving response:
        InputStreamReader in = new InputStreamReader( conn.getInputStream() );
        BufferedReader buff_in = new BufferedReader( in );
        while ( buff_in.ready() )
        {
            response += buff_in.readLine() + "   ";
            //System.out.println( response + " " + data );
        }

        buff_in.close();
        out.close();

        return response;
    }    
    
    */
    
    public String sendUnicodeSMS( String message , String mobileNumber )
    {
    	//System.out.println(  " message -------- > " + message  + " -------- >" + mobileNumber );
    	
    	String response = null;
		String encryptedPassword;
		
		String username = "PHD25PGIMER";
    	String password = "sph@25";
    	String senderId = "PGIMER";
    	String secureKey = "c2e059c4-1a77-425c-8d9b-9b958139ce5c";
		
		String finalmessage = "";
		for(int i = 0 ; i< message.length();i++){

			char ch = message.charAt(i);
			int j = (int) ch;
			String sss = "&#"+j+";";
			finalmessage = finalmessage+sss;
		}
	
		
		String genratedhashKey = hashGenerator(username, senderId, finalmessage, secureKey);
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod("https://msdgweb.mgov.gov.in/esms/sendsmsrequest");
		
		try {
			encryptedPassword  = MD5(password);
			method.addParameter(new NameValuePair("bulkmobno",mobileNumber));
			method.addParameter(new NameValuePair("senderid",senderId));
			method.addParameter(new NameValuePair("content",finalmessage));
			method.addParameter(new NameValuePair("smsservicetype","unicodemsg"));
			method.addParameter(new NameValuePair("username",username));
			method.addParameter(new NameValuePair("password",encryptedPassword));
			method.addParameter(new NameValuePair("key",genratedhashKey));
			client.executeMethod(method);
			response = method.getResponseBodyAsString();
		} 
		catch (HttpException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println( response + " -------- > " + finalmessage  + " -------- >" + mobileNumber );

		return response;

	}   
    
    
    protected String hashGenerator(String userName, String senderId, String content, String secureKey) 
    {
		// TODO Auto-generated method stub
		StringBuffer finalString=new StringBuffer();
		finalString.append(userName.trim()).append(senderId.trim()).append(content.trim()).append(secureKey.trim());
		//		logger.info("Parameters for SHA-512 : "+finalString);
		String hashGen=finalString.toString();
		StringBuffer sb = null;
		MessageDigest md;
		try 
		{
			md = MessageDigest.getInstance("SHA-512");
			md.update(hashGen.getBytes());
			byte byteData[] = md.digest();
			//convert the byte to hex format method 1
			sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}

		} 
		catch (NoSuchAlgorithmException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}  
    
    
    private static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  
	{ 
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] md5 = new byte[64];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		md5 = md.digest();
		return convertedToHex(md5);
	}   
    
    private static String convertedToHex(byte[] data) 
	{ 
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < data.length; i++) 
		{ 
			int halfOfByte = (data[i] >>> 4) & 0x0F;
			int twoHalfBytes = 0;

			do 
			{ 
				if ((0 <= halfOfByte) && (halfOfByte <= 9)) 
				{
					buf.append( (char) ('0' + halfOfByte) );
				}

				else 
				{
					buf.append( (char) ('a' + (halfOfByte - 10)) );
				}

				halfOfByte = data[i] & 0x0F;

			} while(twoHalfBytes++ < 1);
		} 
		return buf.toString();
	}   
    
    
    
    
    
    
    
    
    
    
}

