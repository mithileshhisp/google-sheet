package org.hisp.dhis.googlesheet;

import java.util.List;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GoogleSheetUtil
{
    public static String getCreateStatement( List<Object> headers, String sheet )
    {
        String createStatement = "create table " + sheet.toLowerCase() + "( column_place_holder );";
        String column_str = "";
        String c = "";
        int index = 0;
        int length = headers.size() - 1;
        for ( Object obj : headers )
        {
            c = obj.toString();
            c = c.replace( " ", "_" );
            c = c.replace( ".", "_" );
            c = c.replace( " ,", "," ).toLowerCase();
            if ( index < length )
            {
                column_str += c + " varchar(1000),";
            }
            else
            {
                column_str += c + " varchar(1000)";
            }
            index++;
        }

        createStatement = createStatement.replace( "column_place_holder", column_str );
        return createStatement;
    }

    public static String createInsertStatement( String column, String value, String table_name )
    {
        return "insert into " + table_name + " (" + column + ") value ('" + value + "');";
    }
}
