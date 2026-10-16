import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class FEnumCond extends Table{

    public static String file = "fenumcond_";
    public static String tableName = "F_ENUMCOND";
    public static String configList = "listFEnumCond";

    public static String insert(String filename)
    {
        return  """
                 BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_ENUMCOND_DEST') IS NOT NULL
                    INSERT INTO F_ENUMCOND (
                    [EC_Champ],[EC_Enumere],[EC_Quantite],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag])

                    SELECT dest.[EC_Champ],dest.[EC_Enumere],dest.[EC_Quantite],dest.[cbProt],dest.[cbCreateur],dest.[cbModification],dest.[cbReplication],dest.[cbFlag]
                    FROM F_ENUMCOND_DEST dest
                    LEFT JOIN (SELECT EC_Champ,EC_Enumere FROM F_ENUMCOND) src
                        ON dest.EC_Champ = src.EC_Champ
                        AND dest.EC_Enumere = src.EC_Enumere
                    WHERE src.EC_Enumere IS NULL
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
                   'F_ENUMCOND',
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
                updateTableDest(sqlCon, "EC_Enumere,EC_Champ", "'EC_Enumere','EC_Champ'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteCondition(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"EC_Enumere,EC_Champ");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

    }
    public static void deleteCondition(Connection sqlCon, String path,String filename)
    {
        String query =

                """
                         DELETE src
                         FROM F_ENUMCOND src
                         INNER JOIN F_ENUMCOND_SUPPR del
                         ON src.EC_Enumere = del.EC_Enumere
                         AND src.EC_Champ = del.EC_Champ ;
                         IF OBJECT_ID('F_ENUMCOND_SUPPR') IS NOT NULL
                         DROP TABLE F_ENUMCOND_SUPPR ;\
                        """;

        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
