import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class JMouv extends Table {
    public static String file ="JMouv_";
    public static String tableName = "F_JMOUV";
    public static String configList = "listJMouv";

    public static String insert(String filename)
    {
        return
                """
                         BEGIN TRY  SET DATEFORMAT ymd;

                         IF OBJECT_ID('F_JMOUV_DEST') IS NOT NULL
                         INSERT INTO [dbo].[F_JMOUV]
                             ([JO_Num],[JM_Date],[JM_Cloture],[JM_Impression],[JM_DateCloture]
                             ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                        SELECT dest.[JO_Num],dest.[JM_Date],[JM_Cloture],[JM_Impression],[JM_DateCloture]
                                ,[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                         FROM F_JMOUV_DEST dest
                         LEFT JOIN (SELECT [JO_Num],[JM_Date] FROM F_JMOUV) src
                         ON dest.[JO_Num] = src.[JO_Num]
                         AND     dest.JM_Date = src.JM_Date
                         WHERE src.[JO_Num] IS NULL ;
                         IF OBJECT_ID('F_JMOUV_DEST') IS NOT NULL
                         DROP TABLE F_JMOUV_DEST;
                         END TRY
                         BEGIN CATCH
                        INSERT INTO config.DB_Errors
                            VALUES
                          (SUSER_SNAME(),
                           ERROR_NUMBER(),
                           ERROR_STATE(),
                           ERROR_SEVERITY(),
                           ERROR_LINE(),
                           ERROR_PROCEDURE(),
                           ERROR_MESSAGE(),
                           'Insert '+ ' %s',
                           'F_JMOUV',
                           GETDATE());
                        END CATCH\
                        """.formatted(filename);
    }

    public static void sendDataElement(Connection sqlCon, String path,int unibase) {
        File dir = new File(path);
        FilenameFilter filter = (dir1, name) -> name.startsWith(file);
        String[] children = dir.list(filter);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children) {
                readOnFile(path, filename, tableName + "_DEST", sqlCon);

                readOnFile(path, "deleteList" + filename, tableName + "_SUPPR", sqlCon);
                updateTableDest(sqlCon, "JO_Num,JM_Date", "'JO_Num','JM_Date'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteArtCompta(sqlCon, path, filename);
            }
        }
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"JO_Num,JM_Date");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void deleteArtCompta(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_JMOUV
                         WHERE EXISTS (SELECT 1 FROM F_JMOUV_SUPPR WHERE F_JMOUV_SUPPR.JO_Num = F_JMOUV.JO_Num   AND F_JMOUV_SUPPR.JM_Date = F_JMOUV.JM_Date)

                         IF OBJECT_ID('F_JMOUV_SUPPR') IS NOT NULL
                         DROP TABLE F_JMOUV_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

}
