import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class CatTarif extends Table {

    public static String file ="catTarif_";
    public static String tableName = "P_CATTARIF";
    public static String configList = "listCatTarif";

    public static String insert(String filename)
    {
        return          """
                 BEGIN TRY  SET DATEFORMAT ymd;

                 IF OBJECT_ID('P_CATTARIF_DEST') IS NOT NULL
                 INSERT INTO [dbo].[P_CATTARIF]
                 ([cbIndice],[CT_Intitule],[CT_PrixTTC])

                 SELECT dest.[cbIndice],[CT_PrixTTC],[CT_Intitule]
                 FROM P_CATTARIF_DEST dest
                 LEFT JOIN (SELECT [cbIndice] FROM P_CATTARIF) src
                 ON dest.[cbIndice] = src.[cbIndice]
                 WHERE src.[cbIndice] IS NULL ;
                 IF OBJECT_ID('P_CATTARIF_DEST') IS NOT NULL
                 DROP TABLE P_CATTARIF_DEST; END TRY
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
                   'P_CATTARIF',
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

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteCatTarif(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"cbIndice");//initTable(sqlCon);
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

    }
    public static void initTable(Connection sqlCon)
    {
        String query = """
                 IF NOT EXISTS (SELECT 1 FROM config.SelectTable WHERE tableName='P_CATTARIF')
                     INSERT INTO config.ListCatTarif
                     SELECT cbIndice,cbMarq
                     FROM P_CATTARIF\
                """;
        executeQuery(sqlCon, query);
    }
    public static void deleteCatTarif(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM P_CATTARIF
                         WHERE EXISTS (SELECT 1 FROM P_CATTARIF_SUPPR WHERE P_CATTARIF_SUPPR.cbIndice = P_CATTARIF.cbIndice)

                         IF OBJECT_ID('P_CATTARIF_SUPPR') IS NOT NULL
                         DROP TABLE P_CATTARIF_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
