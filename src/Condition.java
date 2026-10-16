import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class Condition extends Table{

    public static String file = "condition_";
    public static String tableName = "F_CONDITION";
    public static String configList = "listCondition";

    public static String insert(String filename)
    {
        return  """
                 BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_CONDITION_DEST') IS NOT NULL
                    INSERT INTO F_CONDITION (
                    [AR_Ref],[CO_No],[EC_Enumere],[EC_Quantite]
                            ,[CO_Ref],[CO_CodeBarre],[CO_Principal],[cbProt],[cbCreateur]
                            ,[cbModification],[cbReplication],[cbFlag])

                    SELECT dest.[AR_Ref],dest.[CO_No],dest.[EC_Enumere],[EC_Quantite]
                            ,[CO_Ref],[CO_CodeBarre],[CO_Principal],[cbProt],[cbCreateur]
                            ,[cbModification],[cbReplication],[cbFlag]
                    FROM F_CONDITION_DEST dest
                    LEFT JOIN (SELECT AR_Ref,CO_No,EC_Enumere FROM F_CONDITION) src
                        ON dest.CO_No = src.CO_No
                        AND dest.EC_Enumere = src.EC_Enumere
                        AND dest.AR_Ref = src.AR_Ref
                    WHERE src.CO_No IS NULL
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
                   'F_CONDITION',
                   GETDATE());
                END CATCH\
                """.formatted(filename);
    }
    public static void sendDataElement(Connection sqlCon, String path,int unibase)
    {
        File dir = new File(path);
        FilenameFilter filter = (dir1, name) -> name.startsWith(file);
        String[] children = dir.list(filter);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children) {
                readOnFile(path, filename, tableName + "_DEST", sqlCon);
                readOnFile(path, "deleteList" + filename, tableName + "_SUPPR", sqlCon);
                updateTableDest(sqlCon, "CO_No,AR_Ref", "'CO_No','AR_Ref'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteCondition(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"CO_No,AR_Ref");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

    }
    public static void deleteCondition(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_CONDITION
                         WHERE CO_No IN(SELECT CO_No FROM F_CONDITION_SUPPR)

                         IF OBJECT_ID('F_CONDITION_SUPPR') IS NOT NULL
                         DROP TABLE F_CONDITION_SUPPR
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
