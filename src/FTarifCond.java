import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class FTarifCond extends Table{

    public static String file = "ftarifcond_";
    public static String tableName = "F_TARIFCOND";
    public static String configList = "listFTarifCond";

    public static String insert(String filename)
    {
        return  """
                 BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_TARIFCOND_DEST') IS NOT NULL
                    INSERT INTO F_TARIFCOND (
                    [AR_Ref],[TC_RefCF],[CO_No],[TC_Prix],[TC_PrixNouv],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                    SELECT dest.[AR_Ref],dest.[TC_RefCF],dest.[CO_No],dest.[TC_Prix],dest.[TC_PrixNouv],dest.[cbProt],dest.[cbCreateur],dest.[cbModification],dest.[cbReplication],dest.[cbFlag]
                    FROM F_TARIFCOND_DEST dest
                    LEFT JOIN (SELECT AR_Ref,CO_No FROM F_TARIFCOND) src
                        ON dest.AR_Ref = src.AR_Ref
                        AND dest.CO_No = src.CO_No
                    WHERE src.AR_Ref IS NULL
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
                   'F_TARIFCOND',
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
                updateTableDest(sqlCon, "AR_Ref,CO_No", "'AR_Ref','CO_No','TC_RefCF'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteCondition(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"AR_Ref,CO_No");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

    }
    public static void deleteCondition(Connection sqlCon, String path,String filename)
    {
        String query =

                """
                         DELETE src
                         FROM F_TARIFCOND src
                         INNER JOIN F_TARIFCOND_SUPPR del
                         ON src.AR_Ref = del.AR_Ref
                         AND src.CO_No = del.CO_No ;
                         IF OBJECT_ID('F_TARIFCOND_SUPPR') IS NOT NULL
                         DROP TABLE F_TARIFCOND_SUPPR ;\
                        """;

        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
