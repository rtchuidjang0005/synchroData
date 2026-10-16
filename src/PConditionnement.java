import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class PConditionnement extends Table{

    public static String file = "pconditionnement_";
    public static String tableName = "P_CONDITIONNEMENT";
    public static String configList = "listPConditionnement";

    public static String insert(String filename)
    {
        return  """
                 BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('P_CONDITIONNEMENT_DEST') IS NOT NULL
                    INSERT INTO P_CONDITIONNEMENT (
                    [P_Conditionnement],[cbIndice])

                    SELECT dest.[P_Conditionnement],dest.[cbIndice]
                    FROM P_CONDITIONNEMENT_DEST dest
                    LEFT JOIN (SELECT cbIndice FROM P_CONDITIONNEMENT) src
                        ON dest.cbIndice = src.cbIndice
                    WHERE src.cbIndice IS NULL
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
                   'P_CONDITIONNEMENT',
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
                updateTableDest(sqlCon, "cbIndice", "'cbIndice'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                deleteCondition(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"cbIndice");
        getData(sqlCon, selectSourceTable(tableName,database,false), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

    }
    public static void deleteCondition(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM P_CONDITIONNEMENT
                         WHERE cbIndice IN(SELECT cbIndice FROM P_CONDITIONNEMENT_SUPPR)

                         IF OBJECT_ID('P_CONDITIONNEMENT_SUPPR') IS NOT NULL
                         DROP TABLE P_CONDITIONNEMENT_SUPPR
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
